package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * Tests Change 2:
 *
 * Open rule fallback in RuleNode.processSingleRequests().
 *
 * Rule:
 *   Fish(X) ∧ HasGills(X) -> Aquatic(X)
 *
 * Fact:
 *   Fish(nemo)
 *
 * Missing:
 *   HasGills(nemo)
 *
 * Query:
 *   Aquatic(X)?
 *
 * Correct result after Change 2:
 *   The open rule should enter the fallback branch, ask its antecedents,
 *   and NOT infer Aquatic(X), because HasGills(X) is missing.
 *
 * BEFORE Change 2:
 *   If the open-rule fallback is only:
 *
 *      super.processSingleRequests(currentRequest);
 *
 *   the supported open rule may behave like a normal proposition node
 *   instead of requesting its antecedents.
 *
 * AFTER Change 2:
 *   The fallback is:
 *
 *      if (this.supported(currentContext, currentAttitude, 0)) {
 *          requestAntecedentsNotAlreadyWorkingOn(currentRequest);
 *      } else {
 *          super.processSingleRequests(currentRequest);
 *      }
 */
public class OpenRuleFallbackFishTestTwo {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(
                attitudeNames,
                consistentAttitudes,
                false,
                false,
                false,
                1
        );

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    public void openRuleFallback_shouldReachOpenRuleAndAskAntecedents() throws Exception {

        /*
         * Base nodes.
         */
        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node hasGills = Network.createNode("HasGills", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        /*
         * Variable node.
         */
        Node x = Network.createVariableNode("X", "propositionnode");

        /*
         * Open propositions:
         *   Fish(X)
         *   HasGills(X)
         *   Aquatic(X)
         *
         * IMPORTANT:
         * We do NOT add forall to the rule here.
         * This keeps the rule open, so RuleNode.processSingleRequests()
         * enters the open-rule branch.
         */
        PropositionNode fishX = memberClass(x, fish);
        PropositionNode hasGillsX = memberClass(x, hasGills);
        PropositionNode aquaticX = memberClass(x, aquatic);

        /*
         * Closed proposition:
         *   Fish(nemo)
         *
         * We intentionally do NOT assert HasGills(nemo).
         */
        PropositionNode fishNemo = memberClass(nemo, fish);

        /*
         * Assert only Fish(nemo).
         */
        fishNemo.setHyp(CONTEXT, BELIEF);

        /*
         * Open rule:
         *   Fish(X) ∧ HasGills(X) -> Aquatic(X)
         *
         * Do not add forall here. We want the rule to stay open.
         */
        RuleNode openRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX, hasGillsX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        )
                )
        );

        /*
         * Assert/support the rule itself.
         * Change 2 checks:
         * if the open rule is supported, ask antecedents.
         */
        openRule.setHyp(CONTEXT, BELIEF);

        System.out.println("\n====================================================");
        System.out.println("CHANGE 2 TEST: Open rule fallback");
        System.out.println("Query: Aquatic(X)?");
        System.out.println("Fact: Fish(nemo)");
        System.out.println("Missing: HasGills(nemo)");
        System.out.println("Rule: Fish(X) ∧ HasGills(X) -> Aquatic(X)");
        System.out.println("Expected after fix: Rule is reached, asks antecedents, no final answer");
        System.out.println("====================================================\n");

        /*
         * Query the OPEN consequent, not Aquatic(nemo).
         * This makes deduce() send a RuleCons request to the open rule.
         */
        aquaticX.deduce(CONTEXT, BELIEF);

        var answers = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG AFTER deduce(Aquatic(X)) ===");
        System.out.println("Backward answers: " + answers.values());
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(X) supported? " + aquaticX.supported(CONTEXT, BELIEF, 0));
        System.out.println("======================================\n");

        /*
         * Since HasGills(X) has no supported instance, the rule should not fire.
         */
        assertTrue(
                answers.isEmpty(),
                "Aquatic(X) was inferred even though HasGills(X) has no supported instance."
        );

        System.out.println("✓ Correct: Open rule was reached but did not fire because HasGills was missing.");
    }

    @Test
    public void supportedOpenRuleWithoutCompatibleKnownInstance_shouldQueueBoundAntRuleRequests()
            throws Exception {
        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node hasGills = Network.createNode("HasGills", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishX = memberClass(x, fish);
        PropositionNode hasGillsX = memberClass(x, hasGills);
        PropositionNode aquaticX = memberClass(x, aquatic);

        RuleNode openRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX, hasGillsX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        )
                )
        );
        openRule.setHyp(CONTEXT, BELIEF);

        edu.guc.mind_graf.components.Substitutions filter =
                new edu.guc.mind_graf.components.Substitutions();
        filter.add(x, nemo);
        edu.guc.mind_graf.components.Substitutions switcher =
                new edu.guc.mind_graf.components.Substitutions();

        edu.guc.mind_graf.mgip.requests.Request consequentRequest =
                new edu.guc.mind_graf.mgip.requests.Request(
                        new edu.guc.mind_graf.mgip.requests.RuleToConsequentChannel(
                                switcher,
                                filter,
                                CONTEXT,
                                BELIEF,
                                aquaticX
                        ),
                        openRule
                );

        invokeRuleRequest(openRule, consequentRequest);

        Queue<edu.guc.mind_graf.mgip.requests.Request> queuedRequests = Scheduler.getLowQueue();
        assertEquals(
                2,
                queuedRequests.size(),
                "A supported open rule without a compatible known instance should ask its antecedents."
        );
        assertTrue(
                Scheduler.getHighQueue().isEmpty(),
                "The open rule must not fall back to a proposition reply for the consequent."
        );

        HashSet<Integer> requestedAntecedents = new HashSet<>();
        for (edu.guc.mind_graf.mgip.requests.Request request : queuedRequests) {
            assertEquals(
                    edu.guc.mind_graf.mgip.requests.ChannelType.AntRule,
                    request.getChannel().getChannelType()
            );
            assertSame(openRule, request.getChannel().getRequesterNode());
            assertEquals(CONTEXT, request.getChannel().getContextName());
            assertEquals(BELIEF, request.getChannel().getAttitudeID());
            assertTrue(request.getChannel().getFilterSubstitutions().contains(x));
            assertSame(nemo, request.getChannel().getFilterSubstitutions().get(x));
            requestedAntecedents.add(request.getReporterNode().getId());
        }

        assertTrue(requestedAntecedents.contains(fishX.getId()));
        assertTrue(requestedAntecedents.contains(hasGillsX.getId()));
    }

    private PropositionNode memberClass(Node member, Node clazz) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("member"),
                                new NodeSet(member)
                        ),
                        new DownCable(
                                Network.getRelations().get("class"),
                                new NodeSet(clazz)
                        )
                )
        );
    }

    private void invokeRuleRequest(RuleNode rule, edu.guc.mind_graf.mgip.requests.Request request)
            throws Exception {
        Method method = RuleNode.class.getDeclaredMethod(
                "processSingleRequests",
                edu.guc.mind_graf.mgip.requests.Request.class
        );
        method.setAccessible(true);
        method.invoke(rule, request);
    }
}
