package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
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

public class OrEntailmentForwardInferenceTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() throws Exception {
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
    void orEntailment_oneAntecedentIsEnoughToInferPossibleFire() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * SmokeDetected(BuildingA)
         *
         * Missing:
         * HeatDetected(BuildingA)
         *
         * Rule:
         * SmokeDetected(BuildingA) ∨ HeatDetected(BuildingA)
         *      => PossibleFire(BuildingA)
         *
         * Expected:
         * PossibleFire(BuildingA) should be inferred.
         *
         * Purpose:
         * This verifies that Or-Entailment fires when at least one
         * antecedent is supported.
         * -------------------------------------------------------
         */

        // ── individual and classes ─────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node possibleFire = Network.createNode("PossibleFire", "propositionnode");

        // ── closed antecedent 1: SmokeDetected(BuildingA) ──────
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);

        // ── closed antecedent 2: HeatDetected(BuildingA) ───────
        // This node is part of the rule, but it will NOT be added.
        PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);

        // ── closed consequent: PossibleFire(BuildingA) ─────────
        PropositionNode possibleFireBuildingA = memberClass(buildingA, possibleFire);

        // ── OR rule:
        //    SmokeDetected(BuildingA) ∨ HeatDetected(BuildingA)
        //    → PossibleFire(BuildingA)
        RuleNode possibleFireRule = (RuleNode) Network.createNode(
                "orentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedBuildingA, heatDetectedBuildingA)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(possibleFireBuildingA)
                        )
                )
        );

        // Support the rule.
        possibleFireRule.setHyp(CONTEXT, BELIEF);

        // Before inference, the conclusion should not be supported.
        assertFalse(
                possibleFireBuildingA.supported(CONTEXT, BELIEF, 0),
                "PossibleFire(BuildingA) should not be supported before forward inference."
        );

        // ── true forward inference trigger ─────────────────────
        // Add only one antecedent.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER OR-ENTAILMENT FORWARD TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());
        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("HeatDetected(BuildingA) supported? "
                + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("PossibleFire(BuildingA) supported? "
                + possibleFireBuildingA.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        assertFalse(
                heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingA) should remain unsupported because it was not added."
        );

        assertTrue(
                possibleFireBuildingA.supported(CONTEXT, BELIEF, 0),
                "PossibleFire(BuildingA) should be supported because OR-entailment needs only one antecedent."
        );

        boolean inferredPossibleFire = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(possibleFireBuildingA.getName()));

        assertTrue(
                inferredPossibleFire,
                "Expected PossibleFire(BuildingA) to appear in forward asserted nodes after adding SmokeDetected(BuildingA)."
        );

        System.out.println("✓ OR-entailment forward inference worked: one supported antecedent inferred PossibleFire(BuildingA).");
    }

    /*
     * -------------------------------------------------------
     * Helper:
     * Builds a proposition using member/class.
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
                        new DownCable(Network.getRelations().get("member"), new NodeSet(memberNode)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(classNode))
                )
        );
    }
}