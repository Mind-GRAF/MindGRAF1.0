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

public class BasicAndBackwardInferenceTest {

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
    void basicAndBackwardInference_queryDangerInBuilding_returnsAnswer() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * SmokeDetected(BuildingA)
         * PeopleInside(BuildingA)
         *
         * Rule:
         * SmokeDetected(BuildingA) ∧ PeopleInside(BuildingA)
         *      => DangerInBuilding(BuildingA)
         *
         * Query:
         * DangerInBuilding(BuildingA)?
         *
         * Expected:
         * The backward inference process should verify the two
         * antecedents and answer the query.
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

        // ── closed consequent/query: DangerInBuilding(BuildingA)
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

        /*
         * Support the rule and the two known facts.
         *
         * In this test, we are not testing add().
         * We are testing deduce().
         *
         * So the facts are made known using setHyp(),
         * then the query is asked using deduce().
         */
        emergencyRule.setHyp(CONTEXT, BELIEF);
        smokeDetectedBuildingA.setHyp(CONTEXT, BELIEF);
        peopleInsideBuildingA.setHyp(CONTEXT, BELIEF);

        // Before the query, the conclusion should not already be supported.
        assertFalse(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should not be supported before backward inference."
        );

        // ── backward inference trigger ─────────────────────────
        dangerInBuildingA.deduce(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var backwardReplies = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG AFTER deduce(DangerInBuilding(BuildingA)) ===");
        System.out.println("Backward reply nodes: " + backwardReplies.values());
        System.out.println("SmokeDetected(BuildingA) supported? " + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("PeopleInside(BuildingA) supported? " + peopleInsideBuildingA.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingA) supported? " + dangerInBuildingA.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────
        assertFalse(
                backwardReplies.isEmpty(),
                "No backward reply nodes were produced. The query was not answered."
        );

        boolean answeredDanger = backwardReplies.values().stream()
                .anyMatch(node -> node.getName().equals(dangerInBuildingA.getName()));

        assertTrue(
                answeredDanger,
                "Expected DangerInBuilding(BuildingA) to appear in backward reply nodes after deduce()."
        );

        assertTrue(
                dangerInBuildingA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should be supported after backward inference."
        );

        System.out.println("✓ Basic AND backward inference worked: deduce(DangerInBuilding(BuildingA)) verified SmokeDetected(BuildingA) and PeopleInside(BuildingA).");
    }
}