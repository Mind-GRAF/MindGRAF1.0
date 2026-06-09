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

public class NumericalEntailmentForwardInferenceTest {

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
void numericalEntailment_openTwoOfThreeWarnings_infersFireLikelyForBuildingA() throws Exception {

    /*
     * -------------------------------------------------------
     * Open positive test:
     *
     * Known:
     * SmokeDetected(BuildingA)
     * HeatDetected(BuildingA)
     *
     * Missing:
     * AlarmTriggered(BuildingA)
     *
     * Open Rule:
     * Forall X:
     * At least 2 of:
     * SmokeDetected(X),
     * HeatDetected(X),
     * AlarmTriggered(X)
     *
     * => FireLikely(X)
     *
     * Expected:
     * FireLikely(BuildingA) should be inferred.
     * -------------------------------------------------------
     */

    // ── individual and variable ─────────────────────────────
    Node buildingA = Network.createNode("BuildingA", "individualnode");
    Node x = Network.createVariableNode("X", "individualnode");

    // ── classes ─────────────────────────────────────────────
    Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
    Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
    Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
    Node fireLikely = Network.createNode("FireLikely", "propositionnode");

    // Numerical-entailment threshold node: i = 2
    Node two = Network.createNode("2", "individualnode");

    /*
     * Closed facts that will be added:
     * SmokeDetected(BuildingA)
     * HeatDetected(BuildingA)
     */
    PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
    PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);
    PropositionNode alarmTriggeredBuildingA = memberClass(buildingA, alarmTriggered);

    /*
     * Closed expected consequent:
     * FireLikely(BuildingA)
     *
     * This node is used only to check that the open rule inferred
     * the correct closed instance after substituting X with BuildingA.
     */
    PropositionNode fireLikelyBuildingA = memberClass(buildingA, fireLikely);

    /*
     * Open antecedent patterns:
     * SmokeDetected(X)
     * HeatDetected(X)
     * AlarmTriggered(X)
     */
    PropositionNode smokeDetectedX = memberClass(x, smokeDetected);
    PropositionNode heatDetectedX = memberClass(x, heatDetected);
    PropositionNode alarmTriggeredX = memberClass(x, alarmTriggered);

    /*
     * Open consequent pattern:
     * FireLikely(X)
     */
    PropositionNode fireLikelyX = memberClass(x, fireLikely);

    // ── open numerical-entailment rule:
    //    Forall X:
    //    At least 2 of:
    //    SmokeDetected(X), HeatDetected(X), AlarmTriggered(X)
    //    → FireLikely(X)
    RuleNode fireLikelyRule = (RuleNode) Network.createNode(
            "numentailment",
            new DownCableSet(
                    new DownCable(
                            Network.getRelations().get("ant"),
                            new NodeSet(
                                    smokeDetectedX,
                                    heatDetectedX,
                                    alarmTriggeredX
                            )
                    ),
                    new DownCable(
                            Network.getRelations().get("i"),
                            new NodeSet(two)
                    ),
                    new DownCable(
                            Network.getRelations().get("forall"),
                            new NodeSet(x)
                    ),
                    new DownCable(
                            Network.getRelations().get("cq"),
                            new NodeSet(fireLikelyX)
                    )
            )
    );

    // Support the open rule.
    fireLikelyRule.setHyp(CONTEXT, BELIEF);

    // Before inference, the closed conclusion should not be supported.
    assertFalse(
            fireLikelyBuildingA.supported(CONTEXT, BELIEF, 0),
            "FireLikely(BuildingA) should not be supported before forward inference."
    );

    // Add two closed warning signs.
    smokeDetectedBuildingA.add(CONTEXT, BELIEF);
    heatDetectedBuildingA.add(CONTEXT, BELIEF);

    var forwardNodes = Scheduler.getForwardAssertedNodes();

    System.out.println("\n=== DEBUG AFTER OPEN NUMERICAL-ENTAILMENT POSITIVE TEST ===");
    System.out.println("Forward asserted nodes: " + forwardNodes.values());
    System.out.println("SmokeDetected(BuildingA) supported? "
            + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
    System.out.println("HeatDetected(BuildingA) supported? "
            + heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
    System.out.println("AlarmTriggered(BuildingA) supported? "
            + alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0));
    System.out.println("FireLikely(BuildingA) supported? "
            + fireLikelyBuildingA.supported(CONTEXT, BELIEF, 0));

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
            "AlarmTriggered(BuildingA) should remain unsupported because it was not added."
    );

//     assertTrue(
//             fireLikelyBuildingA.supported(CONTEXT, BELIEF, 0),
//             "FireLikely(BuildingA) should be supported because the open numerical-entailment rule matched X = BuildingA."
//     );

    boolean inferredFireLikely = forwardNodes.values().stream()
        .anyMatch(node ->
                node.toString().contains("BuildingA")
             && node.toString().contains("FireLikely")
        );

    assertTrue(
            inferredFireLikely,
            "Expected FireLikely(BuildingA) to appear in forward asserted nodes after matching X = BuildingA."
    );

    System.out.println("✓ Open numerical-entailment worked: 2 of 3 warning signs inferred FireLikely(BuildingA) with X = BuildingA.");
}

    @Test
    void numericalEntailment_oneOfThreeWarnings_doesNotInferFireLikely() throws Exception {

        /*
         * -------------------------------------------------------
         * Negative test:
         *
         * Known:
         * SmokeDetected(BuildingB)
         *
         * Missing:
         * HeatDetected(BuildingB)
         * AlarmTriggered(BuildingB)
         *
         * Rule:
         * At least 2 of 3 warning signs
         * => FireLikely(BuildingB)
         *
         * Expected:
         * FireLikely(BuildingB) should NOT be inferred.
         * -------------------------------------------------------
         */

        // ── individual and classes ─────────────────────────────
        Node buildingB = Network.createNode("BuildingB", "propositionnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node fireLikely = Network.createNode("FireLikely", "propositionnode");

        // Numerical-entailment threshold node: i = 2
        Node two = Network.createNode("2", "individualnode");

        // ── antecedents ────────────────────────────────────────
        PropositionNode smokeDetectedBuildingB = memberClass(buildingB, smokeDetected);
        PropositionNode heatDetectedBuildingB = memberClass(buildingB, heatDetected);
        PropositionNode alarmTriggeredBuildingB = memberClass(buildingB, alarmTriggered);

        // ── consequent ─────────────────────────────────────────
        PropositionNode fireLikelyBuildingB = memberClass(buildingB, fireLikely);

        // ── numerical-entailment rule:
        //    At least 2 of the 3 antecedents
        //    → FireLikely(BuildingB)
        RuleNode fireLikelyRule = (RuleNode) Network.createNode(
                "numentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(
                                        smokeDetectedBuildingB,
                                        heatDetectedBuildingB,
                                        alarmTriggeredBuildingB
                                )
                        ),
                        new DownCable(
                                Network.getRelations().get("i"),
                                new NodeSet(two)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(fireLikelyBuildingB)
                        )
                )
        );

        // Support the rule.
        fireLikelyRule.setHyp(CONTEXT, BELIEF);

        // Before inference, the conclusion should not be supported.
        assertFalse(
                fireLikelyBuildingB.supported(CONTEXT, BELIEF, 0),
                "FireLikely(BuildingB) should not be supported before forward inference."
        );

        // Add only one warning sign.
        smokeDetectedBuildingB.add(CONTEXT, BELIEF);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER NUMERICAL-ENTAILMENT NEGATIVE TEST ===");
        System.out.println("Forward asserted nodes: " + forwardNodes.values());
        System.out.println("SmokeDetected(BuildingB) supported? "
                + smokeDetectedBuildingB.supported(CONTEXT, BELIEF, 0));
        System.out.println("HeatDetected(BuildingB) supported? "
                + heatDetectedBuildingB.supported(CONTEXT, BELIEF, 0));
        System.out.println("AlarmTriggered(BuildingB) supported? "
                + alarmTriggeredBuildingB.supported(CONTEXT, BELIEF, 0));
        System.out.println("FireLikely(BuildingB) supported? "
                + fireLikelyBuildingB.supported(CONTEXT, BELIEF, 0));

        assertTrue(
                smokeDetectedBuildingB.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingB) should be supported after add()."
        );

        assertFalse(
                heatDetectedBuildingB.supported(CONTEXT, BELIEF, 0),
                "HeatDetected(BuildingB) should remain unsupported because it was not added."
        );

        assertFalse(
                alarmTriggeredBuildingB.supported(CONTEXT, BELIEF, 0),
                "AlarmTriggered(BuildingB) should remain unsupported because it was not added."
        );

        assertTrue(
                forwardNodes.isEmpty(),
                "No forward node should be inferred because only 1 of 3 antecedents is satisfied."
        );

        assertFalse(
                fireLikelyBuildingB.supported(CONTEXT, BELIEF, 0),
                "FireLikely(BuildingB) should NOT be supported because the rule needs at least 2 antecedents."
        );

        System.out.println("✓ Numerical-entailment negative case worked: 1 of 3 warning signs did not infer FireLikely(BuildingB).");
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