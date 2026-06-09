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
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.mgip.matching.Matcher;

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
    void forwardInference_assertFact_Ajohn_infers_Cjohn() throws Exception {

        // ── nodes ──────────────────────────────────────────────
        Node X = Network.createVariableNode("X", "individualnode");
        Node john = Network.createNode("john", "individualnode");

        Node A = Network.createNode("A", "propositionnode");
        Node B = Network.createNode("B", "propositionnode");
        Node C = Network.createNode("C", "propositionnode");

        // Pattern: A(X)
        PropositionNode AX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(A))
                )
        );

        // Pattern: B(X)
        PropositionNode BX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(B))
                )
        );

        // Pattern: C(X)
        PropositionNode CX = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(X)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(C))
                )
        );

        // Ground fact: A(john)
        PropositionNode johnIsA = (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(john)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(A))
                )
        );

        // Rule 1: A(X) → B(X)
        RuleNode R1 = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"), new NodeSet(AX)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(BX))
                )
        );

        // Rule 2: B(X) → C(X)
        RuleNode R2 = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"), new NodeSet(BX)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(CX))
                )
        );

        // Add the rules as hypotheses in the current context and attitude
        R1.setHyp(CONTEXT, 0);
        R2.setHyp(CONTEXT, 0);

        // ── real forward inference trigger ─────────────────────
        // This is the important change:
        // We assert the closed fact A(john).
        // The system should automatically publish it, match it with A(X),
        // fire R1, infer B(john), forward B(john) to R2,
        // and finally infer C(john).

        johnIsA.setHyp(CONTEXT, 0);

        System.out.println("\n=== MATCH DIRECTION CHECK ===");
        System.out.println("johnIsA -> matches: " + Matcher.match(johnIsA, ContextController.getContext(CONTEXT), 0).size());
        System.out.println("AX -> matches: " + Matcher.match(AX, ContextController.getContext(CONTEXT), 0).size());
        johnIsA.add(CONTEXT, 0);



        //Scheduler.schedule();

        // ── verify ─────────────────────────────────────────────
        // After true forward propagation, C(X) should have known instance X=john.
        var cKnown = CX.getKnownInstances().getPositiveCollectionbyAttribute(0);

        assertNotNull(cKnown, "CX should have known instances — chain did not reach C");
        assertFalse(cKnown.isEmpty(), "CX should have at least one known instance");

        boolean johnBound = cKnown.stream().anyMatch(ki -> {
            Substitutions resultSubs = ki.getSubstitutions();
            return resultSubs.contains(X) && resultSubs.get(X).equals(john);
        });

        assertTrue(
                johnBound,
                "C(john) was never inferred — true forward inference A(john) → B(john) → C(john) is broken"
        );

        System.out.println("✓ True forward inference worked: assert A(john) → infer B(john) → infer C(john)");
    }
}