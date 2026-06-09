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

public class VariableBindingForwardInferenceTest {

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
    void variableBinding_onlyBuildingWithBothAntecedentsInfersDanger() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * SmokeDetected(BuildingA)
         * PeopleInside(BuildingA)
         * SmokeDetected(BuildingB)
         *
         * Missing:
         * PeopleInside(BuildingB)
         *
         * Open Rule:
         * SmokeDetected(x) ∧ PeopleInside(x)
         *      => DangerInBuilding(x)
         *
         * Expected:
         * A forward inference should be produced for BuildingA.
         * No forward inference should be produced for BuildingB.
         *
         * Purpose:
         * The system must preserve the binding x = BuildingA.
         * It must not combine SmokeDetected(BuildingB)
         * with PeopleInside(BuildingA).
         * -------------------------------------------------------
         */

        // ── individuals ────────────────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        // ── classes / predicates ───────────────────────────────
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");

        // ── variable X ─────────────────────────────────────────
        Node x = Network.createVariableNode("X", "individualnode");

        // ── open antecedent pattern: SmokeDetected(X) ──────────
        PropositionNode smokeDetectedX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(smokeDetected))
                )
        );

        // ── open antecedent pattern: PeopleInside(X) ───────────
        PropositionNode peopleInsideX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(peopleInside))
                )
        );

        // ── open consequent pattern: DangerInBuilding(X) ───────
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

        // ── closed proposition for BuildingB that should NOT be inferred ─────
        PropositionNode dangerInBuildingB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(buildingB)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(dangerInBuilding))
                )
        );

        // ── open AND rule:
        //    SmokeDetected(X) ∧ PeopleInside(X)
        //    → DangerInBuilding(X)
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

        // Support the open rule.
        dangerRule.setHyp(CONTEXT, BELIEF);

        // Before inference, BuildingB conclusion should not be supported.
        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should not be supported before inference."
        );

        /*
         * -------------------------------------------------------
         * First: complete the rule for BuildingA.
         * -------------------------------------------------------
         */

        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        peopleInsideBuildingA.add(CONTEXT, BELIEF);

        /*
         * Important:
         * Copy the values immediately.
         * Do not keep the Scheduler map directly, because the next add()
         * can replace the latest forward asserted nodes.
         */
        List<Node> forwardNodesAfterBuildingA =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        boolean inferredDangerForBuildingA = forwardNodesAfterBuildingA.stream()
                .anyMatch(node ->
                        node.toString().contains("BuildingA")
                                && node.toString().contains("DangerInBuilding")
                );

        assertTrue(
                inferredDangerForBuildingA,
                "A forward inferred node for DangerInBuilding(BuildingA) should be produced after BuildingA satisfies both antecedents."
        );

        /*
         * -------------------------------------------------------
         * Second: add only SmokeDetected(BuildingB).
         * PeopleInside(BuildingB) is intentionally missing.
         * -------------------------------------------------------
         */

        smokeDetectedBuildingB.add(CONTEXT, BELIEF);

        List<Node> forwardNodesAfterBuildingB =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        // ── debug output ───────────────────────────────────────
        System.out.println("\n=== DEBUG AFTER VARIABLE BINDING TEST ===");

        System.out.println("Forward nodes after BuildingA completed the rule: "
                + forwardNodesAfterBuildingA);

        System.out.println("Forward nodes after adding SmokeDetected(BuildingB) only: "
                + forwardNodesAfterBuildingB);

        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("PeopleInside(BuildingA) supported? "
                + peopleInsideBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("SmokeDetected(BuildingB) supported? "
                + smokeDetectedBuildingB.supported(CONTEXT, BELIEF, 0));

        System.out.println("DangerInBuilding(BuildingB) supported? "
                + dangerInBuildingB.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        assertTrue(
                peopleInsideBuildingA.supported(CONTEXT, BELIEF, 0),
                "PeopleInside(BuildingA) should be supported after add()."
        );

        assertTrue(
                smokeDetectedBuildingB.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingB) should be supported after add()."
        );

        assertTrue(
                forwardNodesAfterBuildingB.isEmpty(),
                "No new forward nodes should be inferred after adding only SmokeDetected(BuildingB)."
        );

        assertFalse(
                dangerInBuildingB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should NOT be supported because PeopleInside(BuildingB) was never added."
        );

        System.out.println("✓ Variable binding worked: BuildingA completed the rule, while BuildingB did not mix with BuildingA.");
    }
}