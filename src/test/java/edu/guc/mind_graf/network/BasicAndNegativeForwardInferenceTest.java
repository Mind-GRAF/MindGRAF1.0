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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasicAndNegativeForwardInferenceTest {

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
    void basicAndNegativeForwardInference_missingPeopleInside_doesNotInferDanger() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * SmokeDetected(BuildingB)
         *
         * Missing:
         * PeopleInside(BuildingB)
         *
         * Rule:
         * SmokeDetected(BuildingB) ∧ PeopleInside(BuildingB)
         *      => DangerInBuilding(BuildingB)
         *
         * Expected:
         * DangerInBuilding(BuildingB) should NOT be inferred.
         * -------------------------------------------------------
         */

        // ── individual and classes ─────────────────────────────
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");

        // ── closed antecedent 1: SmokeDetected(BuildingB) ──────
        PropositionNode smokeDetectedBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── closed antecedent 2: PeopleInside(BuildingB) ───────
        // This node is created as part of the rule, but it will NOT be added.
        PropositionNode peopleInsideBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── closed consequent: DangerInBuilding(BuildingB) ─────
        PropositionNode dangerInBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── closed AND rule:
        //    SmokeDetected(BuildingB) ∧ PeopleInside(BuildingB)
        //    → DangerInBuilding(BuildingB)
        RuleNode emergencyRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedBuildingB, peopleInsideBuildingB)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(dangerInBuildingB)
                        )
                )
        );

        // The rule is supported.
        emergencyRule.setHyp(CONTEXT, BELIEF);

        // Before adding anything, the conclusion should not be supported.
        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should not be supported before forward inference."
        );

        // ── true forward inference trigger ─────────────────────
        // Add only ONE antecedent.
        // PeopleInside(BuildingB) is intentionally missing.
        smokeDetectedBuildingB.add(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER add(SmokeDetected(BuildingB)) ONLY ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());
        System.out.println("SmokeDetected(BuildingB) supported? " + smokeDetectedBuildingB.supported(CONTEXT, BELIEF, 0));
        System.out.println("PeopleInside(BuildingB) supported? " + peopleInsideBuildingB.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingB) supported? " + dangerInBuildingB.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────
        boolean inferredDanger = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(dangerInBuildingB.getName()));

        assertFalse(
                inferredDanger,
                "DangerInBuilding(BuildingB) should NOT appear in forward asserted nodes because PeopleInside(BuildingB) was not added."
        );

        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should NOT be supported because the AND rule received only one antecedent."
        );

        System.out.println("✓ Negative AND forward inference worked: rule did not fire when PeopleInside(BuildingB) was missing.");
    }
}