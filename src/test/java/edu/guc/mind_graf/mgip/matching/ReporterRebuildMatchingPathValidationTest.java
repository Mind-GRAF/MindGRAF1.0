package edu.guc.mind_graf.mgip.rules;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReporterRebuildMatchingPathValidationTest {

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
    void reporterRebuildShouldPreserveMatchedBindingAndNotMixOtherInstances() throws Exception {

        /*
         * Focused validation for Change 1:
         *
         * Rule:
         * SmokeDetected(X) ∧ PeopleInside(X) -> DangerInBuilding(X)
         *
         * Known:
         * SmokeDetected(BuildingA)
         * PeopleInside(BuildingA)
         * SmokeDetected(BuildingB)
         *
         * Missing:
         * PeopleInside(BuildingB)
         *
         * Expected:
         * DangerInBuilding(BuildingA) should be inferred.
         * DangerInBuilding(BuildingB) should NOT be inferred.
         *
         * Why this validates Change 1:
         * The closed facts must satisfy the open antecedents through matching.
         * The forwarded report must be rebuilt as coming from the open
         * antecedent pattern, while keeping the correct substitution.
         */

        // ── individuals ────────────────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        // ── predicates/classes ─────────────────────────────────
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");

        // ── variable ───────────────────────────────────────────
        Node x = Network.createVariableNode("X", "individualnode");

        // ── open pattern: SmokeDetected(X) ─────────────────────
        PropositionNode smokeDetectedX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── open pattern: PeopleInside(X) ──────────────────────
        PropositionNode peopleInsideX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── open consequent: DangerInBuilding(X) ───────────────
        PropositionNode dangerInBuildingX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── closed fact: SmokeDetected(BuildingA) ──────────────
        PropositionNode smokeDetectedBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── closed fact: PeopleInside(BuildingA) ───────────────
        PropositionNode peopleInsideBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── closed fact: SmokeDetected(BuildingB) ──────────────
        PropositionNode smokeDetectedBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── closed conclusion for BuildingA expected to be inferred ─
        PropositionNode dangerInBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── closed conclusion for BuildingB must NOT be inferred ─
        PropositionNode dangerInBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── open AND rule ──────────────────────────────────────
        RuleNode dangerRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedX, peopleInsideX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(dangerInBuildingX)
                        )
                )
        );

        dangerRule.setHyp(CONTEXT, BELIEF);

        assertFalse(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should not be supported before inference."
        );

        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should not be supported before inference."
        );

        /*
         * Complete the rule for BuildingA.
         * This should infer DangerInBuilding(BuildingA).
         */
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        peopleInsideBuildingA.add(CONTEXT, BELIEF);

        var forwardEntriesAfterBuildingA =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().entrySet());

        List<Node> forwardNodesAfterBuildingA =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        boolean inferredDangerForBuildingA = forwardNodesAfterBuildingA.stream()
                .anyMatch(node ->
                        node.toString().contains("BuildingA")
                                && node.toString().contains("DangerInBuilding")
                );

        PropositionNode inferredDangerNode =
                findForwardNode(forwardNodesAfterBuildingA, "BuildingA", "DangerInBuilding");

        assertNotNull(
                inferredDangerNode,
                "The forward asserted node should be the inferred DangerInBuilding(BuildingA) node."
        );

        Map.Entry<Report, PropositionNode> inferredDangerEntry =
                findForwardEntry(forwardEntriesAfterBuildingA, "BuildingA", "DangerInBuilding");

        assertNotNull(
                inferredDangerEntry,
                "The scheduler should keep the RuleCons report that inferred DangerInBuilding(BuildingA)."
        );

        Report inferenceReport = inferredDangerEntry.getKey();

        assertEquals(
                ReportType.RuleCons,
                inferenceReport.getReportType(),
                "The inferred danger node should be produced by a RuleCons report."
        );

        assertEquals(
                InferenceType.FORWARD,
                inferenceReport.getInferenceType(),
                "The inferred danger node should be produced by forward inference."
        );

        assertEquals(
                dangerRule,
                inferenceReport.getReporterNode(),
                "The rule should be the reporter of the inferred consequent."
        );

        assertEquals(
                dangerInBuildingX,
                inferenceReport.getRequesterNode(),
                "The open consequent pattern should be the requester of the RuleCons report."
        );

        assertTrue(
                inferenceReport.getSubstitutions().contains(x),
                "The RuleCons report should preserve variable X."
        );

        assertEquals(
                buildingA,
                inferenceReport.getSubstitutions().get(x),
                "The RuleCons report should preserve the binding X = BuildingA."
        );

        assertSupportContainsOrigins(
                inferredDangerNode,
                BELIEF,
                smokeDetectedBuildingA,
                peopleInsideBuildingA,
                dangerRule
        );

        /*
         * Add only one antecedent for BuildingB.
         * This must not mix with PeopleInside(BuildingA).
         */
        smokeDetectedBuildingB.add(CONTEXT, BELIEF);

        List<Node> forwardNodesAfterBuildingB =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        System.out.println("\n=== DEBUG REPORTER REBUILD MATCHING PATH VALIDATION ===");
        System.out.println("Forward nodes after BuildingA completed the rule: "
                + forwardNodesAfterBuildingA);
        System.out.println("Forward nodes after adding SmokeDetected(BuildingB) only: "
                + forwardNodesAfterBuildingB);
        System.out.println("DangerInBuilding(BuildingA) supported? "
                + dangerInBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingB) supported? "
                + dangerInBuildingB.supported(CONTEXT, BELIEF, 0));

        assertTrue(
                inferredDangerForBuildingA,
                "DangerInBuilding(BuildingA) should be inferred when both matched antecedents use X = BuildingA."
        );

        assertTrue(
                forwardNodesAfterBuildingB.isEmpty(),
                "No new forward node should be inferred after only SmokeDetected(BuildingB)."
        );

        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should NOT be supported because PeopleInside(BuildingB) was never added."
        );

        System.out.println("✓ Passed: reporter rebuild preserved the correct matched binding and did not mix BuildingB with BuildingA.");
    }

    private PropositionNode findForwardNode(Collection<? extends Node> nodes, String... requiredText) {
        return nodes.stream()
                .filter(PropositionNode.class::isInstance)
                .map(PropositionNode.class::cast)
                .filter(node -> {
                    String text = node.toString();
                    for (String required : requiredText) {
                        if (!text.contains(required)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    private Map.Entry<Report, PropositionNode> findForwardEntry(
            Collection<Map.Entry<Report, PropositionNode>> entries,
            String... requiredText
    ) {
        return entries.stream()
                .filter(entry -> {
                    String text = entry.getValue().toString();
                    for (String required : requiredText) {
                        if (!text.contains(required)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    private void assertSupportContainsOrigins(
            PropositionNode supportedNode,
            int attitude,
            PropositionNode... expectedOrigins
    ) {
        HashSet<Integer> actualOriginIds = new HashSet<>();

        var supportByLevel = supportedNode.getSupport().getJustificationSupport();

        for (var levelEntry : supportByLevel.entrySet()) {
            var supportsForAttitude = levelEntry.getValue().get(attitude);

            if (supportsForAttitude == null) {
                continue;
            }

            for (var supportEntry : supportsForAttitude) {
                for (var attitudeSupport : supportEntry.getFirst().values()) {
                    actualOriginIds.addAll(attitudeSupport.getFirst().getValues());
                }

                actualOriginIds.addAll(supportEntry.getSecond().getValues());
            }
        }

        for (PropositionNode expectedOrigin : expectedOrigins) {
            assertTrue(
                    actualOriginIds.contains(expectedOrigin.getId()),
                    "Expected support for " + supportedNode.getName()
                            + " to include origin node " + expectedOrigin.getName()
                            + " with id " + expectedOrigin.getId()
                            + ", but actual origin ids were " + actualOriginIds
            );
        }
    }
}
