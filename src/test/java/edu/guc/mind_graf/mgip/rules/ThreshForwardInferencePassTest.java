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

public class ThreshForwardInferencePassTest {

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
    void thresh_fullForwardFlow_thresholdReached_infersRemainingArgumentsTrue() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Thresh rule:
         * thresh = 1
         * threshmax = 3
         *
         * Arguments:
         * SmokeDetected(BuildingA)
         * HeatDetected(BuildingA)
         * AlarmTriggered(BuildingA)
         * SprinklerActivated(BuildingA)
         *
         * Added fact:
         * SmokeDetected(BuildingA)
         *
         * Expected:
         * SmokeDetected(BuildingA) is supported after add().
         * Since thresh = 1, the rule receives enough positive
         * evidence to fire.
         *
         * The remaining arguments should be inferred through the
         * normal forward message-passing flow.
         *
         * Purpose:
         * This test verifies that Thresh works through add(),
         * scheduler reports, RuleInfo creation, and internal
         * mayInfer() execution without manually calling mayInfer().
         * -------------------------------------------------------
         */

        // ── individual ─────────────────────────────────────────
        Node buildingA = Network.createNode("BuildingA", "individualnode");

        // ── warning-sign classes / predicates ──────────────────
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node sprinklerActivated = Network.createNode("SprinklerActivated", "propositionnode");

        // ── Thresh parameters ──────────────────────────────────
        // thresh = 1
        // threshmax = 3
        Node threshOne = Network.createNode("1", "propositionnode");
        Node threshMaxThree = Network.createNode("3", "propositionnode");

        // ── Thresh argument propositions ───────────────────────
        PropositionNode smokeDetectedBuildingA =
                memberClass(buildingA, smokeDetected);

        PropositionNode heatDetectedBuildingA =
                memberClass(buildingA, heatDetected);

        PropositionNode alarmTriggeredBuildingA =
                memberClass(buildingA, alarmTriggered);

        PropositionNode sprinklerActivatedBuildingA =
                memberClass(buildingA, sprinklerActivated);

        // ── Thresh rule:
        //    thresh    = 1
        //    threshmax = 3
        //    arg       = four warning signs
        //
        // Thresh does not use cq here.
        // It reasons over its argument set.
        RuleNode threshRule = (RuleNode) Network.createNode(
                "thresh",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("thresh"),
                                new NodeSet(threshOne)
                        ),
                        new DownCable(
                                Network.getRelations().get("threshmax"),
                                new NodeSet(threshMaxThree)
                        ),
                        new DownCable(
                                Network.getRelations().get("arg"),
                                new NodeSet(
                                        smokeDetectedBuildingA,
                                        heatDetectedBuildingA,
                                        alarmTriggeredBuildingA,
                                        sprinklerActivatedBuildingA
                                )
                        )
                )
        );

        // Support the Thresh rule in the current context and attitude.
        threshRule.setHyp(CONTEXT, BELIEF);

        // Before adding facts, none of the argument propositions are supported.
        assertFalse(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should not be supported before add()."
        );

        assertFalse(
                heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingA) should not be supported before inference."
        );

        assertFalse(
                alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0),
                "AlarmTriggered(BuildingA) should not be supported before inference."
        );

        assertFalse(
                sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SprinklerActivated(BuildingA) should not be supported before inference."
        );

        // ── normal forward-flow trigger ─────────────────────────
        // This should assert SmokeDetected(BuildingA) and send
        // a forward report to the Thresh rule.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        // Capture the latest forward inferred nodes.
        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG THRESH FULL FORWARD FLOW TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());

        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("HeatDetected(BuildingA) supported? "
                + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("AlarmTriggered(BuildingA) supported? "
                + alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("SprinklerActivated(BuildingA) supported? "
                + sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0));

        // ── verify added fact ──────────────────────────────────
        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        // ── verify Thresh inferred remaining arguments ─────────
        boolean inferredHeatArgument = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(heatDetectedBuildingA.getName()));

        boolean inferredAlarmArgument = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(alarmTriggeredBuildingA.getName()));

        boolean inferredSprinklerArgument = forwardNodes.values().stream()
                .anyMatch(node -> node.getName().equals(sprinklerActivatedBuildingA.getName()));

        assertTrue(
                inferredHeatArgument,
                "HeatDetected(BuildingA) should appear in forward asserted nodes as a Thresh inferred argument."
        );

        assertTrue(
                inferredAlarmArgument,
                "AlarmTriggered(BuildingA) should appear in forward asserted nodes as a Thresh inferred argument."
        );

        assertTrue(
                inferredSprinklerArgument,
                "SprinklerActivated(BuildingA) should appear in forward asserted nodes as a Thresh inferred argument."
        );

        System.out.println("✓ Thresh full forward flow worked: threshold was reached and remaining arguments were inferred.");
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
}