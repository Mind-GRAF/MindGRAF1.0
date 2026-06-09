// package edu.guc.mind_graf.network;

// import edu.guc.mind_graf.cables.DownCable;
// import edu.guc.mind_graf.cables.DownCableSet;  
// import edu.guc.mind_graf.context.ContextController;
// import edu.guc.mind_graf.mgip.Scheduler;
// import edu.guc.mind_graf.nodes.Node;
// import edu.guc.mind_graf.nodes.PropositionNode;
// import edu.guc.mind_graf.nodes.RuleNode;
// import edu.guc.mind_graf.set.NodeSet;
// import edu.guc.mind_graf.set.Set;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import java.util.ArrayList;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// public class AndOrForwardInferencePassTest {

//     private static final String CONTEXT = "test";
//     private static final int BELIEF = 0;

//     @BeforeEach
//     void setUp() throws Exception {
//         Scheduler.initiate();

//         Set<String, Integer> attitudeNames = new Set<>();
//         attitudeNames.add("beliefs", BELIEF);

//         ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
//         consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

//         NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

//         ContextController.createNewContext(CONTEXT);
//         ContextController.setCurrContext(CONTEXT);
//     }

//     @Test
//     void andOr_fullForwardFlow_maxReached_infersRemainingArgumentsFalse() throws Exception {

//         /*
//          * -------------------------------------------------------
//          * Test idea:
//          *
//          * AndOr constraint:
//          * At least 2 and at most 2 warning signs may hold.
//          *
//          * Arguments:
//          * SmokeDetected(BuildingA)
//          * HeatDetected(BuildingA)
//          * AlarmTriggered(BuildingA)
//          * SprinklerActivated(BuildingA)
//          *
//          * Added facts:
//          * SmokeDetected(BuildingA)
//          * HeatDetected(BuildingA)
//          *
//          * Expected:
//          * The two added facts are supported positively.
//          * Since max=2 is reached, the remaining arguments are
//          * inferred by AndOr with sign=false.
//          *
//          * Important:
//          * supported(...) does not distinguish positive support
//          * from negative support in this test. Therefore, we do not
//          * assert that AlarmTriggered or SprinklerActivated are
//          * unsupported. Instead, we check that they appear in the
//          * forward inferred nodes, and the log confirms sign=false.
//          * -------------------------------------------------------
//          */

//         // ── individual ─────────────────────────────────────────
//         Node buildingA = Network.createNode("BuildingA", "individualnode");

//         // ── warning-sign classes / predicates ──────────────────
//         Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
//         Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
//         Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
//         Node sprinklerActivated = Network.createNode("SprinklerActivated", "propositionnode");

//         // ── AndOr parameters: exactly 2 warning signs may hold ──
//         Node minTwo = Network.createNode("2", "propositionnode");
//         Node maxTwo = Network.createNode("2", "propositionnode");

//         // ── AndOr argument propositions ────────────────────────
//         PropositionNode smokeDetectedBuildingA =
//                 memberClass(buildingA, smokeDetected);

//         PropositionNode heatDetectedBuildingA =
//                 memberClass(buildingA, heatDetected);

//         PropositionNode alarmTriggeredBuildingA =
//                 memberClass(buildingA, alarmTriggered);

//         PropositionNode sprinklerActivatedBuildingA =
//                 memberClass(buildingA, sprinklerActivated);

//         // ── AndOr rule:
//         //    min = 2
//         //    max = 2
//         //    arg = four warning signs
//         //
//         // This does NOT use cq.
//         // AndOr reasons over its arg nodes.
//         RuleNode andOrRule = (RuleNode) Network.createNode(
//                 "AndOr",
//                 new DownCableSet(
//                         new DownCable(
//                                 Network.getRelations().get("min"),
//                                 new NodeSet(minTwo)
//                         ),
//                         new DownCable(
//                                 Network.getRelations().get("max"),
//                                 new NodeSet(maxTwo)
//                         ),
//                         new DownCable(
//                                 Network.getRelations().get("arg"),
//                                 new NodeSet(
//                                         smokeDetectedBuildingA,
//                                         heatDetectedBuildingA,
//                                         alarmTriggeredBuildingA,
//                                         sprinklerActivatedBuildingA
//                                 )
//                         )
//                 )
//         );

//         // Support the AndOr rule in the current context and attitude.
//         andOrRule.setHyp(CONTEXT, BELIEF);

//         // Before adding facts, none of the argument propositions are supported.
//         assertFalse(
//                 smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "SmokeDetected(BuildingA) should not be supported before add()."
//         );

//         assertFalse(
//                 heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "HeatDetected(BuildingA) should not be supported before add()."
//         );

//         assertFalse(
//                 alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "AlarmTriggered(BuildingA) should not be supported before inference."
//         );

//         assertFalse(
//                 sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "SprinklerActivated(BuildingA) should not be supported before inference."
//         );

//         // ── normal forward-flow trigger ─────────────────────────
//         // These calls should assert the facts and send reports
//         // to the AndOr rule through the scheduler.
//         smokeDetectedBuildingA.add(CONTEXT, BELIEF);
//         heatDetectedBuildingA.add(CONTEXT, BELIEF);

//         // Capture the latest inferred forward nodes.
//         var forwardNodes = Scheduler.getForwardAssertedNodes();

//         System.out.println("\n=== DEBUG ANDOR FULL FORWARD FLOW TEST ===");
//         System.out.println("Forward asserted nodes: " + forwardNodes.values());

//         System.out.println("SmokeDetected(BuildingA) supported? "
//                 + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

//         System.out.println("HeatDetected(BuildingA) supported? "
//                 + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

//         System.out.println("AlarmTriggered(BuildingA) supported? "
//                 + alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0)
//                 + "  NOTE: log should show this was inferred with sign=false");

//         System.out.println("SprinklerActivated(BuildingA) supported? "
//                 + sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0)
//                 + "  NOTE: log should show this was inferred with sign=false");

//         // ── verify added facts ─────────────────────────────────
//         assertTrue(
//                 smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "SmokeDetected(BuildingA) should be supported after add()."
//         );

//         assertTrue(
//                 heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
//                 "HeatDetected(BuildingA) should be supported after add()."
//         );

//         // ── verify AndOr inferred the remaining arguments ───────
//         // These are inferred because max=2 is reached.
//         // The log shows the rule fires with sign=false.
//         boolean inferredAlarmArgument = forwardNodes.values().stream()
//                 .anyMatch(node -> node.getName().equals(alarmTriggeredBuildingA.getName()));

//         boolean inferredSprinklerArgument = forwardNodes.values().stream()
//                 .anyMatch(node -> node.getName().equals(sprinklerActivatedBuildingA.getName()));

//         assertTrue(
//                 inferredAlarmArgument,
//                 "AlarmTriggered(BuildingA) should appear in forward asserted nodes as an AndOr inferred argument."
//         );

//         assertTrue(
//                 inferredSprinklerArgument,
//                 "SprinklerActivated(BuildingA) should appear in forward asserted nodes as an AndOr inferred argument."
//         );

//         System.out.println("✓ AndOr full forward flow worked: max=2 was reached, so remaining arguments were inferred with sign=false.");
//     }

//     /*
//      * -------------------------------------------------------
//      * Helper:
//      * Builds Class(Member) using member/class cables.
//      *
//      * Example:
//      * memberClass(BuildingA, SmokeDetected)
//      * represents SmokeDetected(BuildingA).
//      * -------------------------------------------------------
//      */
//     private PropositionNode memberClass(Node memberNode, Node classNode) throws Exception {
//         return (PropositionNode) Network.createNode(
//                 "propositionnode",
//                 new DownCableSet(
//                         new DownCable(
//                                 Network.getRelations().get("member"),
//                                 new NodeSet(memberNode)
//                         ),
//                         new DownCable(
//                                 Network.getRelations().get("class"),
//                                 new NodeSet(classNode)
//                         )
//                 )
//         );
//     }
// }
// made it open
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AndOrForwardInferencePassTest {

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
    void andOr_openFullForwardFlow_maxReached_infersRemainingArgumentsFalse() throws Exception {

        /*
         * -------------------------------------------------------
         * Open test idea:
         *
         * AndOr constraint:
         * For any X:
         * At least 2 and at most 2 warning signs may hold.
         *
         * Open arguments:
         * SmokeDetected(X)
         * HeatDetected(X)
         * AlarmTriggered(X)
         * SprinklerActivated(X)
         *
         * Added closed facts:
         * SmokeDetected(BuildingA)
         * HeatDetected(BuildingA)
         *
         * Expected:
         * X should match BuildingA.
         * The two added facts are supported positively.
         * Since max=2 is reached, the remaining arguments are
         * inferred by AndOr with sign=false:
         *
         * AlarmTriggered(BuildingA) = false
         * SprinklerActivated(BuildingA) = false
         *
         * Important:
         * supported(...) does not distinguish positive support
         * from negative support in this test. Therefore, we check
         * that the remaining closed arguments appear in the forward
         * inferred nodes, while the log confirms sign=false.
         * -------------------------------------------------------
         */

        // IMPORTANT for open matching:
        // BuildingA should be created as a propositionnode.
        Node buildingA = Network.createNode("BuildingA", "propositionnode");

        // Variable used in the open AndOr rule.
        Node x = Network.createVariableNode("X", "propositionnode");

        // Warning-sign classes / predicates.
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node sprinklerActivated = Network.createNode("SprinklerActivated", "propositionnode");

        // AndOr parameters: exactly 2 warning signs may hold.
        Node minTwo = Network.createNode("2", "propositionnode");
        Node maxTwo = Network.createNode("2", "propositionnode");

        // Closed facts / closed expected nodes.
        PropositionNode smokeDetectedBuildingA =
                memberClass(buildingA, smokeDetected);

        PropositionNode heatDetectedBuildingA =
                memberClass(buildingA, heatDetected);

        PropositionNode alarmTriggeredBuildingA =
                memberClass(buildingA, alarmTriggered);

        PropositionNode sprinklerActivatedBuildingA =
                memberClass(buildingA, sprinklerActivated);

        // Open argument patterns.
        PropositionNode smokeDetectedX =
                memberClass(x, smokeDetected);

        PropositionNode heatDetectedX =
                memberClass(x, heatDetected);

        PropositionNode alarmTriggeredX =
                memberClass(x, alarmTriggered);

        PropositionNode sprinklerActivatedX =
                memberClass(x, sprinklerActivated);

        // Open AndOr rule:
        //
        // Forall X:
        // min = 2
        // max = 2
        // arg = SmokeDetected(X), HeatDetected(X),
        //       AlarmTriggered(X), SprinklerActivated(X)
        //
        // This does NOT use cq.
        // AndOr reasons over its arg nodes.
        RuleNode andOrRule = (RuleNode) Network.createNode(
                "AndOr",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("min"),
                                new NodeSet(minTwo)
                        ),
                        new DownCable(
                                Network.getRelations().get("max"),
                                new NodeSet(maxTwo)
                        ),
                        new DownCable(
                                Network.getRelations().get("forall"),
                                new NodeSet(x)
                        ),
                        new DownCable(
                                Network.getRelations().get("arg"),
                                new NodeSet(
                                        smokeDetectedX,
                                        heatDetectedX,
                                        alarmTriggeredX,
                                        sprinklerActivatedX
                                )
                        )
                )
        );

        // Support the open AndOr rule.
        andOrRule.setHyp(CONTEXT, BELIEF);

        // Before adding facts, none of the closed propositions are supported.
        assertFalse(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should not be supported before add()."
        );

        assertFalse(
                heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingA) should not be supported before add()."
        );

        assertFalse(
                alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0),
                "AlarmTriggered(BuildingA) should not be supported before inference."
        );

        assertFalse(
                sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SprinklerActivated(BuildingA) should not be supported before inference."
        );

        // Normal forward-flow trigger.
        // These closed facts should match the open patterns using X = BuildingA.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        heatDetectedBuildingA.add(CONTEXT, BELIEF);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG OPEN ANDOR FULL FORWARD FLOW TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());

        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("HeatDetected(BuildingA) supported? "
                + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("AlarmTriggered(BuildingA) supported? "
                + alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0)
                + "  NOTE: log should show this was inferred with sign=false");

        System.out.println("SprinklerActivated(BuildingA) supported? "
                + sprinklerActivatedBuildingA.supported(CONTEXT, BELIEF, 0)
                + "  NOTE: log should show this was inferred with sign=false");

        // Verify added facts.
        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        assertTrue(
                heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingA) should be supported after add()."
        );

        // Verify AndOr inferred the remaining arguments with sign=false.
        // Inferred grounded nodes may be generated/temp nodes, so inspect the
        // reported node text and the report sign instead of comparing node names.
        Map.Entry<edu.guc.mind_graf.mgip.reports.Report, PropositionNode> alarmInference =
                findForwardInference(forwardNodes, "BuildingA", "AlarmTriggered");

        Map.Entry<edu.guc.mind_graf.mgip.reports.Report, PropositionNode> sprinklerInference =
                findForwardInference(forwardNodes, "BuildingA", "SprinklerActivated");

        assertNotNull(
                alarmInference,
                "AlarmTriggered(BuildingA) should appear in forward asserted nodes as an AndOr inferred argument."
        );

        assertFalse(
                alarmInference.getKey().isSign(),
                "AlarmTriggered(BuildingA) should be inferred with sign=false because max=2 was reached."
        );

        assertSame(
                andOrRule,
                alarmInference.getKey().getReporterNode(),
                "AlarmTriggered(BuildingA) should be reported by the AndOr rule."
        );

        assertNotNull(
                sprinklerInference,
                "SprinklerActivated(BuildingA) should appear in forward asserted nodes as an AndOr inferred argument."
        );

        assertFalse(
                sprinklerInference.getKey().isSign(),
                "SprinklerActivated(BuildingA) should be inferred with sign=false because max=2 was reached."
        );

        assertSame(
                andOrRule,
                sprinklerInference.getKey().getReporterNode(),
                "SprinklerActivated(BuildingA) should be reported by the AndOr rule."
        );

        System.out.println("✓ Open AndOr full forward flow worked: X matched BuildingA, max=2 was reached, so remaining arguments were inferred with sign=false.");
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

    private Map.Entry<edu.guc.mind_graf.mgip.reports.Report, PropositionNode> findForwardInference(
            Map<edu.guc.mind_graf.mgip.reports.Report, PropositionNode> forwardNodes,
            String memberName,
            String className
    ) {
        return forwardNodes.entrySet().stream()
                .filter(entry -> {
                    String text = String.valueOf(entry.getValue());
                    return text.contains(memberName) && text.contains(className);
                })
                .findFirst()
                .orElse(null);
    }
}
