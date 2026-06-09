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

public class BasicAndForwardInferenceTest {

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
    void basicAndForwardInference_smokeAndPeopleInside_infersDangerInBuilding() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * SmokeDetected(BuildingA)
         * PeopleInside(BuildingA)
         *
         * Rule:
         * SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA)
         *      => DangerInBuilding(BuildingA)
         *
         * Expected:
         * DangerInBuilding(BuildingA)
         * -------------------------------------------------------
         */

        // ── individual and classes ─────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");

        // ── closed antecedent 1: SmokeDetected(BuildingA) ──────
        PropositionNode smokeDetectedBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── closed antecedent 2: PeopleInside(BuildingA) ───────
        PropositionNode peopleInsideBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── closed consequent: DangerInBuilding(BuildingA) ─────
        PropositionNode dangerInBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── closed AND rule:
        //    SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA)
        //    → DangerInBuilding(BuildingA)
        RuleNode emergencyRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedBuildingA, peopleInsideBuildingA)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(dangerInBuildingA)
                        )
                )
        );

        // Only the rule is supported first.
        emergencyRule.setHyp(CONTEXT, BELIEF);

        // Before adding the antecedents, the conclusion should NOT be supported.
        assertFalse(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should not be supported before adding the antecedents."
        );

        // ── true forward inference trigger ─────────────────────
        // Add/assert the antecedents, then let the system forward reports.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        peopleInsideBuildingA.add(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER add(SmokeDetected(BuildingA)) and add(PeopleInside(BuildingA)) ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());
        System.out.println("SmokeDetected(BuildingA) supported? " + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("PeopleInside(BuildingA) supported? " + peopleInsideBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingA) supported? " + dangerInBuildingA.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────
        assertFalse(
                forwardNodes.isEmpty(),
                "No forward asserted nodes were produced. The AND rule did not fire."
        );

        boolean inferredDanger = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(dangerInBuildingA.getName()));

        assertTrue(
                inferredDanger,
                "Expected DangerInBuilding(BuildingA) to appear in forward asserted nodes after adding both antecedents."
        );

        assertTrue(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should be supported after forward inference."
        );

        System.out.println("✓ Basic AND forward inference worked: SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA) → DangerInBuilding(BuildingA)");
    }
}