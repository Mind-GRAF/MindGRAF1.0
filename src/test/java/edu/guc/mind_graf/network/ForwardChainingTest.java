package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForwardChainingTest {

    private static final String CONTEXT = "test";

    @BeforeEach
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
    void automaticChain_AimpliesB_BimpliesC() throws Exception {

        // ── nodes ──────────────────────────────────────────────
        Node X    = Network.createVariableNode("X", "propositionnode");
        Node john = Network.createNode("john", "individualnode");
        Node A    = Network.createNode("A", "propositionnode");
        Node B    = Network.createNode("B", "propositionnode");
        Node C    = Network.createNode("C", "propositionnode");

        // Pattern: X is-a A
        PropositionNode AX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));

        // Pattern: X is-a B
        PropositionNode BX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(B))));

        // Pattern: X is-a C
        PropositionNode CX = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(C))));

        // Ground fact: john is-a A
        PropositionNode johnIsA = (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                new DownCable(Network.getRelations().get("class"),  new NodeSet(A))));

        // Rule 1: A(X) → B(X)
        RuleNode R1 = (RuleNode) Network.createNode("andentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"), new NodeSet(AX)),
                new DownCable(Network.getRelations().get("cq"),  new NodeSet(BX))));

        // Rule 2: B(X) → C(X)
        RuleNode R2 = (RuleNode) Network.createNode("andentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"), new NodeSet(BX)),
                new DownCable(Network.getRelations().get("cq"),  new NodeSet(CX))));

        // ── trigger ────────────────────────────────────────────
        // Assert "john is-a A" and let the scheduler do everything
        johnIsA.add(CONTEXT, 0);

        // ── verify ─────────────────────────────────────────────
        // After add() + schedule(), C(john) should be a known instance of CX
        var cKnown = CX.getKnownInstances().getPositiveCollectionbyAttribute(0);

        assertNotNull(cKnown, "CX should have known instances — chain did not reach C");
        assertFalse(cKnown.isEmpty(), "CX should have at least one known instance");

        // Check that X=john substitution made it all the way through
        boolean johnBound = cKnown.stream().anyMatch(ki -> {
            Substitutions subs = ki.getSubstitutions();
            return subs.contains(X) && subs.get(X).equals(john);
        });

        assertTrue(johnBound,
            "C(john) was never inferred — chain A(john)→B(john)→C(john) is broken");

        System.out.println("✓ Chain A(john) → B(john) → C(john) worked automatically");
    }
}