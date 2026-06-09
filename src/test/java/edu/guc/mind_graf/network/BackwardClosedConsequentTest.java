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

public class BackwardClosedConsequentTest {

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
    void backwardInference_closedAndEntailment_Ajohn_Bjohn_infers_Cjohn() throws Exception {

        // ── individual and classes ─────────────────────────────
        Node john = Network.createNode("john", "individualnode");

        Node A = Network.createNode("A", "propositionnode");
        Node B = Network.createNode("B", "propositionnode");
        Node C = Network.createNode("C", "propositionnode");

        // ── closed antecedent: A(john) ─────────────────────────
        PropositionNode johnIsA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(A))
                )
        );

        // ── closed antecedent: B(john) ─────────────────────────
        PropositionNode johnIsB = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(B))
                )
        );

        // ── closed consequent/query: C(john) ───────────────────
        PropositionNode johnIsC = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(C))
                )
        );

        // A(john) and B(john) are supported facts.
        johnIsA.setHyp(CONTEXT, BELIEF);
        johnIsB.setHyp(CONTEXT, BELIEF);

        // ── closed AND rule: A(john) ∧ B(john) → C(john) ───────
        RuleNode R1 = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"), new NodeSet(johnIsA, johnIsB)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(johnIsC))
                )
        );

        // Rule is supported.
        R1.setHyp(CONTEXT, BELIEF);

        // Before querying, C(john) should not be directly supported.
        assertFalse(
                johnIsC.supported(CONTEXT, BELIEF, 0),
                "C(john) should not be directly supported before backward inference"
        );

        // ── backward inference trigger ─────────────────────────
        System.out.println("\n=== Asking: is C(john) true? ===\n");

        johnIsC.deduce(CONTEXT, BELIEF);

        // ── debug output ───────────────────────────────────────
        var answers = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG AFTER deduce(C(john)) ===");
        System.out.println("Backward answers: " + answers.values());
        System.out.println("johnIsA supported? " + johnIsA.supported(CONTEXT, BELIEF, 0));
        System.out.println("johnIsB supported? " + johnIsB.supported(CONTEXT, BELIEF, 0));
        System.out.println("johnIsC supported? " + johnIsC.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────
        assertFalse(
                answers.isEmpty(),
                "No backward answer was produced — C(john) was not proven by the AND rule"
        );

        boolean foundCJohn = answers.values().stream()
                .anyMatch(node -> node.getName().equals(johnIsC.getName()));

        assertTrue(
                foundCJohn,
                "Expected the backward answer to be the original C(john) node"
        );

        System.out.println("✓ Backward closed AND entailment worked: A(john) ∧ B(john) → C(john)");
    }
}