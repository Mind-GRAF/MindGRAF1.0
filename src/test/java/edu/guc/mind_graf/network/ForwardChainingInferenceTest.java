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

public class ForwardChainingInferenceTest {

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
    void forwardChaining_inferredDangerTriggersEvacuationPossible() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * SmokeDetected(BuildingA)
         * PeopleInside(BuildingA)
         * EmergencyExitOpen(BuildingA)
         *
         * Rule 1:
         * SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA)
         *      => DangerInBuilding(BuildingA)
         *
         * Rule 2:
         * DangerInBuilding(BuildingA) ∧ EmergencyExitOpen(BuildingA)
         *      => EvacuationPossible(BuildingA)
         *
         * Expected:
         * DangerInBuilding(BuildingA)
         * EvacuationPossible(BuildingA)
         * -------------------------------------------------------
         */

        // ── individual and classes ─────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");
        Node emergencyExitOpen = Network.createNode("EmergencyExitOpen", "propositionnode");
        Node evacuationPossible = Network.createNode("EvacuationPossible", "propositionnode");

        // ── known fact 1: SmokeDetected(BuildingA) ─────────────
        PropositionNode smokeDetectedBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── known fact 2: PeopleInside(BuildingA) ──────────────
        PropositionNode peopleInsideBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── inferred by Rule 1: DangerInBuilding(BuildingA) ────
        PropositionNode dangerInBuildingA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── known fact 3: EmergencyExitOpen(BuildingA) ─────────
        PropositionNode emergencyExitOpenA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(emergencyExitOpen))
                )
        );

        // ── final inferred result: EvacuationPossible(BuildingA)
        PropositionNode evacuationPossibleA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingA)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(evacuationPossible))
                )
        );

        // ── Rule 1:
        //    SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA)
        //    → DangerInBuilding(BuildingA)
        RuleNode dangerRule = (RuleNode) Network.createNode(
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

        // ── Rule 2:
        //    DangerInBuilding(BuildingA) ∧ EmergencyExitOpen(BuildingA)
        //    → EvacuationPossible(BuildingA)
        RuleNode evacuationRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(dangerInBuildingA, emergencyExitOpenA)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(evacuationPossibleA)
                        )
                )
        );

        // Support both rules.
        dangerRule.setHyp(CONTEXT, BELIEF);
        evacuationRule.setHyp(CONTEXT, BELIEF);

        // Before inference, neither inferred result should be supported.
        assertFalse(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should not be supported before forward chaining."
        );

        assertFalse(
                evacuationPossibleA.supported(CONTEXT, BELIEF, 0),
                "EvacuationPossible(BuildingA) should not be supported before forward chaining."
        );

        // ── true forward inference trigger ─────────────────────
        // Add the original known facts.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        peopleInsideBuildingA.add(CONTEXT, BELIEF);
        emergencyExitOpenA.add(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER FORWARD CHAINING TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());
        System.out.println("SmokeDetected(BuildingA) supported? " + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("PeopleInside(BuildingA) supported? " + peopleInsideBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("EmergencyExitOpen(BuildingA) supported? " + emergencyExitOpenA.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingA) supported? " + dangerInBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("EvacuationPossible(BuildingA) supported? " + evacuationPossibleA.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────
        // DangerInBuilding(BuildingA) may not remain in the latest
        // forward asserted nodes batch, because the latest batch can
        // be overwritten by the later inference of EvacuationPossible.
        // Therefore, support is the correct verification for Rule 1.
        assertTrue(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should be supported after Rule 1 fires."
        );

        assertTrue(
                evacuationPossibleA.supported(CONTEXT, BELIEF, 0),
                "EvacuationPossible(BuildingA) should be supported after Rule 2 fires."
        );

        // The latest forward asserted nodes should contain the final
        // chained conclusion produced by Rule 2.
        boolean inferredEvacuation = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(evacuationPossibleA.getName()));

        assertTrue(
                inferredEvacuation,
                "Expected EvacuationPossible(BuildingA) to appear in the latest forward asserted nodes after Rule 2 fires."
        );

        System.out.println("✓ Forward chaining worked: DangerInBuilding(BuildingA) triggered EvacuationPossible(BuildingA).");
    }
}