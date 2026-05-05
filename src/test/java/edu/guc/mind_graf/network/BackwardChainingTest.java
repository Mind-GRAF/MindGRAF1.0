package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
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

class BackwardChainingTest {

    private static final String CONTEXT = "test";

    @BeforeEach

    // reset the network and context before each test
    void setUp() {
        Scheduler.initiate();
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void backwardChain_AimpliesB_BimpliesC_askC() throws Exception {

        // ── variables and classes ─────────────────────────
        Node X = Network.createVariableNode("X", "propositionnode");
        Node A = Network.createNode("A", "propositionnode");
        Node B = Network.createNode("B", "propositionnode");
        Node C = Network.createNode("C", "propositionnode");

        // ── pattern nodes ─────────────────────────────────

        // X is-a A
        PropositionNode AX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));

        // X is-a B
        PropositionNode BX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(B))));

        // X is-a C
        PropositionNode CX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(C))));

        // ── ground fact ───────────────────────────────────
        // "john is-a A" — asserted as hypothesis, no mention in the question
        Node john = Network.createNode("john", "propositionnode");
        PropositionNode johnIsA = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));
        johnIsA.setHyp(CONTEXT, 0);

        // ── rules ─────────────────────────────────────────

        // Rule 1: A(X) → B(X)
        RuleNode R1 = (RuleNode) Network.createNode("andentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"), new NodeSet(AX)),
                new DownCable(Network.getRelations().get("cq"),  new NodeSet(BX))));

        R1.setHyp(CONTEXT, 0);   // support/assert the rule itself


        // Rule 2: B(X) → C(X)
        RuleNode R2 = (RuleNode) Network.createNode("andentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"), new NodeSet(BX)),
                new DownCable(Network.getRelations().get("cq"),  new NodeSet(CX))));

        R2.setHyp(CONTEXT, 0);   // support/assert the rule itself

        // ── ask the question ──────────────────────────────
        // "who is C(X)?"
        // system walks backwards:
        //   CX → R2 needs BX
        //   BX → R1 needs AX
        //   AX matches johnIsA with {X=john}
        //   johnIsA is hypothesis → proven
        //   chain fires: john is-a B, then john is-a C
        System.out.println("\n=== Asking: who satisfies C(X)? ===\n");
        CX.deduce(CONTEXT, 0);

        // ── verify ────────────────────────────────────────
        var answers = Scheduler.getBackwardAssertedReplyNodes();
        System.out.println("\n=== Answers found: " + answers.values() + " ===");

        assertFalse(answers.isEmpty(),
            "No answer found — backward chain A(X)→B(X)→C(X) is broken");

        System.out.println("✓ Backward chain worked: system found who satisfies C(X)");
    }
}
