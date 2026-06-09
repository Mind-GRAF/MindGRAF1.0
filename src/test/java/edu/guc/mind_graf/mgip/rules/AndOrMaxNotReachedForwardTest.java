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

public class AndOrMaxNotReachedForwardTest {

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
    void andOr_maxNotReached_doesNotInferRemainingArgumentsFalse() throws Exception {

        /* 
         * -------------------------------------------------------
         * Test idea:
         *
         * AndOr constraint:
         * At least 2 and at most 3 warning signs may hold.
         *
         * Arguments:
         * SmokeDetected(BuildingA)
         * HeatDetected(BuildingA)
         * AlarmTriggered(BuildingA)
         * SprinklerActivated(BuildingA)
         *
         * Added facts:
         * SmokeDetected(BuildingA)
         * HeatDetected(BuildingA)
         *
         * Expected:
         * The two added facts are supported.
         * The remaining arguments should NOT be inferred yet.
         *
         * Purpose:
         * This verifies that AndOr does not infer sign=false
         * just because the minimum is reached. It should only infer
         * remaining arguments as false when max is reached.
         * -------------------------------------------------------
         */

        Node buildingA = Network.createNode("BuildingA", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node sprinklerActivated = Network.createNode("SprinklerActivated", "propositionnode");

        Node minTwo = Network.createNode("2", "propositionnode");
        Node maxThree = Network.createNode("3", "propositionnode");

        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);
        PropositionNode alarmTriggeredBuildingA = memberClass(buildingA, alarmTriggered);
        PropositionNode sprinklerActivatedBuildingA = memberClass(buildingA, sprinklerActivated);

        RuleNode andOrRule = (RuleNode) Network.createNode(
                "AndOr",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("min"),
                                new NodeSet(minTwo)
                        ),
                        new DownCable(
                                Network.getRelations().get("max"),
                                new NodeSet(maxThree)
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

        andOrRule.setHyp(CONTEXT, BELIEF);

        assertFalse(smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        assertFalse(heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        assertFalse(alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0));
        assertFalse(sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0));

        // Two warning signs are added.
        // min=2 is reached, but max=3 is NOT reached.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        heatDetectedBuildingA.add(CONTEXT, BELIEF);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG ANDOR MAX NOT REACHED FULL FLOW TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());

        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("HeatDetected(BuildingA) supported? "
                + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("AlarmTriggered(BuildingA) supported? "
                + alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("SprinklerActivated(BuildingA) supported? "
                + sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0));

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        assertTrue(
                heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingA) should be supported after add()."
        );

        assertFalse(
                alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0),
                "AlarmTriggered(BuildingA) should not be inferred because max=3 is not reached."
        );

        assertFalse(
                sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SprinklerActivated(BuildingA) should not be inferred because max=3 is not reached."
        );

        assertTrue(
                forwardNodes.isEmpty(),
                "No remaining arguments should be inferred because max=3 is not reached."
        );

        System.out.println("✓ AndOr max-not-reached test passed: min was reached, but no sign=false inference happened before max.");
    }

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