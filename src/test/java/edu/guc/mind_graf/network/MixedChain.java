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

public class MixedChain {


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
void backwardChain_MixedConnectives_AND_OR_AND_askG() throws Exception {

    // ── variables and classes ─────────────────────────
    Node X = Network.createVariableNode("X", "propositionnode");

    Node A = Network.createNode("A", "propositionnode");
    Node B = Network.createNode("B", "propositionnode");
    Node D = Network.createNode("D", "propositionnode");
    Node E = Network.createNode("E", "propositionnode");
    Node F = Network.createNode("F", "propositionnode");
    Node G = Network.createNode("G", "propositionnode");

    // ── pattern nodes ─────────────────────────────────

    PropositionNode AX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));

    PropositionNode BX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(B))));

    PropositionNode DX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(D))));

    PropositionNode EX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(E))));

    PropositionNode FX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(F))));

    PropositionNode GX = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(G))));

    // ── ground facts ───────────────────────────────────
    Node john = Network.createNode("john", "propositionnode");

    PropositionNode johnIsA = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));

    PropositionNode johnIsF = (PropositionNode) Network.createNode("propositionnode",
        new DownCableSet(
            new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
            new DownCable(Network.getRelations().get("class"),  new NodeSet(F))));

    johnIsA.setHyp(CONTEXT, 0);
    johnIsF.setHyp(CONTEXT, 0);

    // ── rules ─────────────────────────────────────────

    // R1: A(X) → B(X)
    RuleNode R1 = (RuleNode) Network.createNode("andentailment",
        new DownCableSet(
            new DownCable(Network.getRelations().get("ant"), new NodeSet(AX)),
            new DownCable(Network.getRelations().get("cq"),  new NodeSet(BX))));

    R1.setHyp(CONTEXT, 0);

    // R2: B(X) OR D(X) → E(X)
    RuleNode R2 = (RuleNode) Network.createNode("orentailment",
        new DownCableSet(
            new DownCable(Network.getRelations().get("ant"), new NodeSet(BX, DX)),
            new DownCable(Network.getRelations().get("cq"),  new NodeSet(EX))));

    R2.setHyp(CONTEXT, 0);

    // R3: E(X) AND F(X) → G(X)
    RuleNode R3 = (RuleNode) Network.createNode("andentailment",
        new DownCableSet(
            new DownCable(Network.getRelations().get("ant"), new NodeSet(EX, FX)),
            new DownCable(Network.getRelations().get("cq"),  new NodeSet(GX))));

    R3.setHyp(CONTEXT, 0);

    // ── ask the question ──────────────────────────────
    System.out.println("\n=== Asking: who satisfies G(X)? ===\n");
    GX.deduce(CONTEXT, 0);

    // ── verify ────────────────────────────────────────
    var answers = Scheduler.getBackwardAssertedReplyNodes();

    System.out.println("\n=== Answers found: " + answers.values() + " ===");

    assertFalse(answers.isEmpty(),
        "No answer found — mixed connective backward chain is broken");

    System.out.println("✓ Mixed connective backward chain worked: system inferred G(john)");
}
    
}
