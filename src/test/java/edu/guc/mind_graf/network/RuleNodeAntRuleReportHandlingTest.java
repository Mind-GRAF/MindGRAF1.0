package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * Tests RuleNode.processSingleReports() AntRule handling.
 *
 * Change 3:
 *   Backward AntRule reports must be inserted into the rule handler.
 *
 * Change 4:
 *   Forward AntRule reports must be inserted into the rule handler.
 */
public class RuleNodeAntRuleReportHandlingTest {

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
    public void change3_backwardAntRuleReports_shouldEnterRuleHandlerAndFireRule()
            throws Exception {

        /*
         * Rule:
         *   Fish(X) ∧ HasGills(X) -> Aquatic(X)
         *
         * Facts:
         *   Fish(nemo)
         *   HasGills(nemo)
         *
         * Query:
         *   Aquatic(X)?
         *
         * Expected:
         *   Rule asks both antecedents.
         *   Both antecedents report back with X = nemo.
         *   Rule handler receives both AntRule reports.
         *   Rule fires.
         */

        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node hasGills = Network.createNode("HasGills", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishX = memberClass(x, fish);
        PropositionNode hasGillsX = memberClass(x, hasGills);
        PropositionNode aquaticX = memberClass(x, aquatic);

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode hasGillsNemo = memberClass(nemo, hasGills);

        fishNemo.setHyp(CONTEXT, BELIEF);
        hasGillsNemo.setHyp(CONTEXT, BELIEF);

        RuleNode rule = (RuleNode) Network.createNode(
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

        rule.setHyp(CONTEXT, BELIEF);

        System.out.println("\n====================================================");
        System.out.println("CHANGE 3 TEST: Backward AntRule reports");
        System.out.println("Query: Aquatic(X)?");
        System.out.println("Facts: Fish(nemo), HasGills(nemo)");
        System.out.println("Rule: Fish(X) ∧ HasGills(X) -> Aquatic(X)");
        System.out.println("Expected: AntRule reports enter rule handler and rule fires");
        System.out.println("====================================================\n");

        aquaticX.deduce(CONTEXT, BELIEF);

        var answers = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG CHANGE 3 ===");
        System.out.println("Backward answers: " + answers.values());
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("HasGills(nemo) supported? " + hasGillsNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(X) supported? " + aquaticX.supported(CONTEXT, BELIEF, 0));
        System.out.println("======================\n");

        assertFalse(
                answers.isEmpty(),
                "Backward AntRule reports should make the rule produce a backward answer."
        );

        Map.Entry<Report, PropositionNode> aquaticAnswer =
                findSchedulerEntry(answers, "Aquatic", "nemo");

        assertNotNull(
                aquaticAnswer,
                "Aquatic(nemo) should appear in backward reply nodes after both antecedents answer."
        );

        Report answerReport = aquaticAnswer.getKey();

        assertEquals(
                ReportType.RuleCons,
                answerReport.getReportType(),
                "The backward answer must be carried by the rule consequent report."
        );
        assertEquals(
                InferenceType.BACKWARD,
                answerReport.getInferenceType(),
                "The rule consequent report must preserve backward inference."
        );
        assertSame(
                aquaticX,
                answerReport.getRequesterNode(),
                "The RuleCons reply should be addressed to the queried consequent pattern."
        );
        assertSame(
                rule,
                answerReport.getReporterNode(),
                "The RuleCons reply should be reported by the rule node."
        );
        assertTrue(
                answerReport.getSubstitutions().contains(x),
                "The backward RuleCons report should preserve the query variable X."
        );
        assertSame(
                nemo,
                answerReport.getSubstitutions().get(x),
                "The backward RuleCons report should bind X to nemo."
        );
        assertNotNull(
                answerReport.getSupport(),
                "The backward RuleCons report should carry rule support."
        );
        assertTrue(
                Scheduler.getHighQueue().isEmpty() && Scheduler.getLowQueue().isEmpty(),
                "Backward AntRule handling should leave no pending scheduler work after deduce() completes."
        );
    }

    @Test
    public void change4_forwardAntRuleReports_shouldEnterRuleHandlerAndInferConsequent()
            throws Exception {

        /*
         * Rule:
         *   Fish(X) -> Aquatic(X)
         *
         * Forward fact:
         *   Fish(nemo)
         *
         * Expected:
         *   Fish(nemo).add(...) sends a FORWARD AntRule report to the rule.
         *   The rule inserts the report into applyRuleHandler.
         *   The rule fires forward.
         *   Aquatic(nemo) becomes inferred.
         */

        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishX = memberClass(x, fish);
        PropositionNode aquaticX = memberClass(x, aquatic);

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode aquaticNemo = memberClass(nemo, aquatic);

        RuleNode rule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        )
                )
        );

        rule.setHyp(CONTEXT, BELIEF);

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "Aquatic(nemo) should not be supported before forward inference."
        );

        System.out.println("\n====================================================");
        System.out.println("CHANGE 4 TEST: Forward AntRule reports");
        System.out.println("Forward add: Fish(nemo)");
        System.out.println("Rule: Fish(X) -> Aquatic(X)");
        System.out.println("Expected: forward AntRule report enters rule handler and rule fires");
        System.out.println("====================================================\n");

        /*
         * This starts forward inference.
         * It should send a FORWARD AntRule report from Fish(nemo)/Fish(X) to the rule.
         */
        fishNemo.add(CONTEXT, BELIEF);

        var forwardAnswers = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG CHANGE 4 ===");
        System.out.println("Forward inferred nodes: " + forwardAnswers.values());
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(nemo) supported? " + aquaticNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("======================\n");

        assertFalse(
                forwardAnswers.isEmpty(),
                "Forward AntRule reports should make the rule infer a consequent."
        );

        Map.Entry<Report, PropositionNode> aquaticInference =
                findSchedulerEntry(forwardAnswers, "Aquatic", "nemo");

        assertNotNull(
                aquaticInference,
                "Aquatic(nemo) should appear in forward asserted nodes."
        );

        Report inferenceReport = aquaticInference.getKey();

        assertEquals(
                ReportType.RuleCons,
                inferenceReport.getReportType(),
                "The forward inference must be carried by a RuleCons report."
        );
        assertEquals(
                InferenceType.FORWARD,
                inferenceReport.getInferenceType(),
                "The rule consequent report must preserve forward inference."
        );
        assertSame(
                aquaticX,
                inferenceReport.getRequesterNode(),
                "The RuleCons report should be addressed to the consequent pattern."
        );
        assertSame(
                rule,
                inferenceReport.getReporterNode(),
                "The RuleCons report should be reported by the rule node."
        );
        assertTrue(
                inferenceReport.getSubstitutions().contains(x),
                "The forward RuleCons report should preserve variable X."
        );
        assertSame(
                nemo,
                inferenceReport.getSubstitutions().get(x),
                "The forward RuleCons report should bind X to nemo."
        );
        assertNotNull(
                inferenceReport.getSupport(),
                "The forward RuleCons report should carry rule support."
        );
        assertTrue(
                Scheduler.getHighQueue().isEmpty() && Scheduler.getLowQueue().isEmpty(),
                "Forward AntRule handling should leave no pending scheduler work after add() completes."
        );
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

    private Map.Entry<Report, PropositionNode> findSchedulerEntry(
            Map<Report, PropositionNode> entries,
            String... expectedFragments
    ) {
        return entries.entrySet().stream()
                .filter(entry -> {
                    String nodeText = entry.getValue().toString();
                    for (String expectedFragment : expectedFragments) {
                        if (!nodeText.contains(expectedFragment)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }
}
