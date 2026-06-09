package edu.guc.mind_graf.mgip.rules;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class FishAquaticRoutingBugTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void backwardInference_shouldNotInferAquaticWhenOneAntecedentIsMissing() throws Exception {

        /*
         * Meaning:
         *
         * Fish(nemo)
         * HasGills(nemo) is NOT asserted
         *
         * Rule:
         * Fish(nemo) ∧ HasGills(nemo) -> Aquatic(nemo)
         *
         * Query:
         * Aquatic(nemo)?
         *
         * Correct result:
         * Aquatic(nemo) should NOT be inferred because HasGills(nemo) is missing.
         *
         * With the bug:
         * If RuleNode.processSingleRequests contains:
         *
         *     || currentChannel instanceof Channel
         *
         * the rule behaves like a normal PropositionNode.
         * Since the rule itself is supported, it sends a RuleCons report back
         * without proving both antecedents. Therefore, the test fails.
         */

        Node nemo = Network.createNode("nemo", "individualnode");

        Node Fish = Network.createNode("Fish", "propositionnode");
        Node HasGills = Network.createNode("HasGills", "propositionnode");
        Node Aquatic = Network.createNode("Aquatic", "propositionnode");

        PropositionNode nemoIsFish = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(Fish))
                )
        );

        PropositionNode nemoHasGills = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(HasGills))
                )
        );

        PropositionNode nemoIsAquatic = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(Aquatic))
                )
        );

        /*
         * Assert only Fish(nemo).
         * Do NOT assert HasGills(nemo).
         */
        nemoIsFish.setHyp(CONTEXT, BELIEF);

        RuleNode fishAndGillsImpliesAquatic = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(nemoIsFish, nemoHasGills)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(nemoIsAquatic)
                        )
                )
        );

        /*
         * The rule itself is asserted.
         * This is important because it exposes the routing bug:
         * with the bad condition, the supported rule node replies directly.
         */
        fishAndGillsImpliesAquatic.setHyp(CONTEXT, BELIEF);

        assertFalse(
                nemoIsAquatic.supported(CONTEXT, BELIEF, 0),
                "Aquatic(nemo) should not be directly supported before inference"
        );

        System.out.println("\n====================================================");
        System.out.println("QUERY: Can MindGRAF infer Aquatic(nemo)?");
        System.out.println("Fact: Fish(nemo)");
        System.out.println("Missing fact: HasGills(nemo)");
        System.out.println("Rule: Fish(nemo) ∧ HasGills(nemo) -> Aquatic(nemo)");
        System.out.println("Expected result AFTER FIX: no answer");
        System.out.println("Expected result WITH BUG: wrong answer is produced");
        System.out.println("====================================================\n");

        nemoIsAquatic.deduce(CONTEXT, BELIEF);

        var answers = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG AFTER deduce(Aquatic(nemo)) ===");
        System.out.println("Backward answers: " + answers.values());
        System.out.println("nemoIsFish supported? " + nemoIsFish.supported(CONTEXT, BELIEF, 0));
        System.out.println("nemoHasGills supported? " + nemoHasGills.supported(CONTEXT, BELIEF, 0));
        System.out.println("nemoIsAquatic supported? " + nemoIsAquatic.supported(CONTEXT, BELIEF, 0));
        System.out.println("=========================================\n");

        assertTrue(
                answers.isEmpty(),
                "Aquatic(nemo) was inferred even though HasGills(nemo) is missing. " +
                "If you re-added '|| currentChannel instanceof Channel', this failure shows the routing bug."
        );

        assertFalse(
                nemoIsAquatic.supported(CONTEXT, BELIEF, 0),
                "Aquatic(nemo) should remain unsupported because one antecedent is missing."
        );

        System.out.println("✓ Correct: Aquatic(nemo) was not inferred because HasGills(nemo) is missing.");
    }

    @Test
    void consequentRequest_shouldQueueAntRuleRequestsInsteadOfRuleConsReply() throws Exception {
        Node nemo = Network.createNode("nemo", "individualnode");

        Node Fish = Network.createNode("Fish", "propositionnode");
        Node HasGills = Network.createNode("HasGills", "propositionnode");
        Node Aquatic = Network.createNode("Aquatic", "propositionnode");

        PropositionNode nemoIsFish = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(Fish))
                )
        );

        PropositionNode nemoHasGills = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(HasGills))
                )
        );

        PropositionNode nemoIsAquatic = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(nemo)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(Aquatic))
                )
        );

        RuleNode fishAndGillsImpliesAquatic = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(nemoIsFish, nemoHasGills)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(nemoIsAquatic)
                        )
                )
        );
        fishAndGillsImpliesAquatic.setHyp(CONTEXT, BELIEF);

        edu.guc.mind_graf.components.Substitutions filter =
                new edu.guc.mind_graf.components.Substitutions();
        edu.guc.mind_graf.components.Substitutions switcher =
                new edu.guc.mind_graf.components.Substitutions();
        edu.guc.mind_graf.mgip.requests.Request consequentRequest =
                new edu.guc.mind_graf.mgip.requests.Request(
                        new edu.guc.mind_graf.mgip.requests.RuleToConsequentChannel(
                                switcher,
                                filter,
                                CONTEXT,
                                BELIEF,
                                nemoIsAquatic
                        ),
                        fishAndGillsImpliesAquatic
                );

        invokeRuleRequest(fishAndGillsImpliesAquatic, consequentRequest);

        Queue<edu.guc.mind_graf.mgip.requests.Request> queuedRequests = Scheduler.getLowQueue();
        assertEquals(
                2,
                queuedRequests.size(),
                "A consequent request to a supported rule should be routed to both antecedents."
        );
        assertTrue(
                Scheduler.getHighQueue().isEmpty(),
                "The rule must not answer the consequent request as a supported proposition."
        );

        HashSet<Integer> requestedAntecedents = new HashSet<>();
        for (edu.guc.mind_graf.mgip.requests.Request request : queuedRequests) {
            assertEquals(
                    edu.guc.mind_graf.mgip.requests.ChannelType.AntRule,
                    request.getChannel().getChannelType(),
                    "Consequent requests must become AntRule requests to antecedents."
            );
            assertSame(
                    fishAndGillsImpliesAquatic,
                    request.getChannel().getRequesterNode(),
                    "The rule should be the requester of each antecedent request."
            );
            assertEquals(CONTEXT, request.getChannel().getContextName());
            assertEquals(BELIEF, request.getChannel().getAttitudeID());
            assertTrue(request.getChannel().getFilterSubstitutions().isEmpty());
            assertTrue(request.getChannel().getSwitcherSubstitutions().isEmpty());
            requestedAntecedents.add(request.getReporterNode().getId());
        }

        assertTrue(requestedAntecedents.contains(nemoIsFish.getId()));
        assertTrue(requestedAntecedents.contains(nemoHasGills.getId()));
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
