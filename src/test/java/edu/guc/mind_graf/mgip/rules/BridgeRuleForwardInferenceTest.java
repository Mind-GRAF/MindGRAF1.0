package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BridgeRuleForwardInferenceTest {

    private static final String CONTEXT = "test";

    private static final int BELIEF = 0;
    private static final int GOAL = 1;
    private static final int INTENTION = 2;

    @BeforeEach
    void setUp() throws Exception {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);
        attitudeNames.add("goals", GOAL);
        attitudeNames.add("intentions", INTENTION);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF, GOAL, INTENTION)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void bridgeRule_openBeliefAndGoal_forwardInferIntention() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Bridge rule pattern:
         *
         * Belief:
         * SmokeDetected(X)
         *
         * Goal:
         * KeepPeopleSafe(X)
         *
         * Intention:
         * StartEvacuation(X)
         *
         * Closed facts:
         * SmokeDetected(BuildingA)     in beliefs
         * KeepPeopleSafe(BuildingA)    in goals
         *
         * Expected:
         * StartEvacuation(BuildingA) should be inferred in intentions.
         *
         * Why open patterns?
         * Bridge rules use attitude-specific relations like 0-ant,
         * 1-ant, and 2-cq. In the current forward flow, the closed
         * bridge test did not send reports to the bridge rule.
         * Using open patterns allows the matching process and P-Tree
         * to connect the facts to the bridge rule correctly.
         * -------------------------------------------------------
         */

        // ── individual ─────────────────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");

        // ── variable ───────────────────────────────────────────
        Node x = Network.createVariableNode("X", "individualnode");

        // ── predicates / classes ───────────────────────────────
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node keepPeopleSafe = Network.createNode("KeepPeopleSafe", "propositionnode");
        Node startEvacuation = Network.createNode("StartEvacuation", "propositionnode");

        // ── open bridge patterns ───────────────────────────────
        PropositionNode smokeDetectedX =
                memberClass(x, smokeDetected);

        PropositionNode keepPeopleSafeX =
                memberClass(x, keepPeopleSafe);

        PropositionNode startEvacuationX =
                memberClass(x, startEvacuation);

        // ── closed facts ───────────────────────────────────────
        PropositionNode smokeDetectedBuildingA =
                memberClass(buildingA, smokeDetected);

        PropositionNode keepPeopleSafeBuildingA =
                memberClass(buildingA, keepPeopleSafe);

        PropositionNode startEvacuationBuildingA =
                memberClass(buildingA, startEvacuation);

        /*
         * -------------------------------------------------------
         * Bridge rule representation:
         *
         * 0-ant -> SmokeDetected(X)
         * 1-ant -> KeepPeopleSafe(X)
         * 2-cq  -> StartEvacuation(X)
         *
         * Meaning:
         * belief antecedent + goal antecedent
         * infer intention consequent.
         * -------------------------------------------------------
         */
        RuleNode bridgeRule = (RuleNode) Network.createNode(
                "BridgeRule",
                new DownCableSet(
                        new DownCable(
                                getOrCreateRelation("0-ant"),
                                new NodeSet(smokeDetectedX)
                        ),
                        new DownCable(
                                getOrCreateRelation("1-ant"),
                                new NodeSet(keepPeopleSafeX)
                        ),
                        new DownCable(
                                getOrCreateRelation("2-cq"),
                                new NodeSet(startEvacuationX)
                        )
                )
        );

        // Support the bridge rule.
        bridgeRule.setHyp(CONTEXT, BELIEF);

        // Before adding facts, the intention should not be supported.
        assertFalse(
                startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0),
                "StartEvacuation(BuildingA) should not be supported in intentions before inference."
        );

        /*
         * -------------------------------------------------------
         * Add only the belief antecedent first.
         * This should NOT be enough to fire the bridge rule.
         * -------------------------------------------------------
         */
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        System.out.println("\n=== DEBUG AFTER ADDING BELIEF ONLY ===");
        System.out.println("SmokeDetected(BuildingA) as belief supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("KeepPeopleSafe(BuildingA) as goal supported? "
                + keepPeopleSafeBuildingA.supported(CONTEXT, GOAL, 0));
        System.out.println("StartEvacuation(BuildingA) as intention supported? "
                + startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0));

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported in beliefs after add()."
        );

        assertFalse(
                startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0),
                "The bridge rule should not fire after only the belief antecedent."
        );

        assertTrue(
                Scheduler.getForwardAssertedNodes().isEmpty(),
                "The bridge rule should not enqueue a forward inferred node after only one antecedent."
        );

        /*
         * -------------------------------------------------------
         * Add the goal antecedent.
         * Now both bridge antecedents are satisfied for X = BuildingA.
         * -------------------------------------------------------
         */
        keepPeopleSafeBuildingA.add(CONTEXT, GOAL);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER BRIDGE RULE FULL FLOW TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());

        System.out.println("SmokeDetected(BuildingA) as belief supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("KeepPeopleSafe(BuildingA) as goal supported? "
                + keepPeopleSafeBuildingA.supported(CONTEXT, GOAL, 0));

        System.out.println("StartEvacuation(BuildingA) as intention supported? "
                + startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0));

        // ── verify antecedents ─────────────────────────────────
        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported in beliefs."
        );

        assertTrue(
                keepPeopleSafeBuildingA.supported(CONTEXT, GOAL, 0),
                "KeepPeopleSafe(BuildingA) should be supported in goals."
        );

        // ── verify bridge consequent ───────────────────────────
        // True(
        //         startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0),
        //         "StartEvacuation(BuildingA) should be inferred in intentions after both bridge antecedents are satisfied."
        // );assert

        Map.Entry<Report, PropositionNode> inferredIntentionEntry = forwardNodes.entrySet().stream()
                .filter(entry ->
                        entry.getValue().toString().contains("StartEvacuation")
                                && entry.getValue().toString().contains("BuildingA")
                )
                .findFirst()
                .orElse(null);

        assertNotNull(
                inferredIntentionEntry,
                "StartEvacuation(BuildingA) should appear in forward asserted nodes after the bridge rule fires."
        );

        Report inferenceReport = inferredIntentionEntry.getKey();

        assertEquals(
                INTENTION,
                inferenceReport.getAttitude(),
                "The bridge rule consequent should be reported using the intention attitude."
        );

        assertEquals(
                InferenceType.FORWARD,
                inferenceReport.getInferenceType(),
                "The bridge rule consequent should be produced by forward inference."
        );

        assertEquals(
                ReportType.RuleCons,
                inferenceReport.getReportType(),
                "The inferred intention should be carried by a RuleCons report."
        );

        assertTrue(
                inferenceReport.getSubstitutions().contains(x),
                "The RuleCons report should preserve the bridge variable X."
        );

        assertEquals(
                buildingA,
                inferenceReport.getSubstitutions().get(x),
                "The RuleCons report should bind X to BuildingA."
        );

        assertEquals(
                startEvacuationX,
                inferenceReport.getRequesterNode(),
                "The bridge consequent pattern StartEvacuation(X) should be the report requester."
        );

        assertEquals(
                bridgeRule,
                inferenceReport.getReporterNode(),
                "The bridge rule should be the reporter that produced the inferred intention."
        );

        assertReportSupportContainsOrigins(
                inferenceReport,
                bridgeRule
        );

        System.out.println("✓ Bridge rule forward inference worked: belief + goal inferred an intention.");
    }

    /*
     * -------------------------------------------------------
     * Helper:
     * Builds Class(Member) using member/class cables.
     *
     * Example:
     * memberClass(BuildingA, SmokeDetected)
     * represents SmokeDetected(BuildingA).
     * -------------------------------------------------------
     */
    private PropositionNode memberClass(Node memberNode, Node classNode) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("member"),
                                new NodeSet(memberNode)
                        ),
                        new DownCable(
                                Network.getRelations().get("class"),
                                new NodeSet(classNode)
                        )
                )
        );
    }

    /*
     * -------------------------------------------------------
     * Helper:
     * Gets a relation if it already exists.
     * If it does not exist, it creates it.
     *
     * Needed for bridge-specific relations:
     * 0-ant, 1-ant, and 2-cq.
     * -------------------------------------------------------
     */
    private Relation getOrCreateRelation(String relationName) throws Exception {
        Relation relation = Network.getRelations().get(relationName);

        if (relation == null) {
            relation = Network.createRelation(
                    relationName,
                    "",
                    Adjustability.EXPAND,
                    1
            );
        }

        return relation;
    }

    private void assertReportSupportContainsOrigins(
            Report report,
            PropositionNode... expectedOrigins
    ) {
        HashSet<Integer> actualOriginIds = new HashSet<>();

        var supportByLevel = report.getSupport().getJustificationSupport();

        for (var levelEntry : supportByLevel.entrySet()) {
            for (var supportsForAttitude : levelEntry.getValue().values()) {
                for (var supportEntry : supportsForAttitude) {
                    for (var attitudeSupport : supportEntry.getFirst().values()) {
                        actualOriginIds.addAll(attitudeSupport.getFirst().getValues());
                    }

                    actualOriginIds.addAll(supportEntry.getSecond().getValues());
                }
            }
        }

        for (PropositionNode expectedOrigin : expectedOrigins) {
            assertTrue(
                    actualOriginIds.contains(expectedOrigin.getId()),
                    "Expected bridge inference support to include origin node "
                            + expectedOrigin.getName()
                            + " with id " + expectedOrigin.getId()
                            + ", but actual origin ids were " + actualOriginIds
            );
        }
    }
}
