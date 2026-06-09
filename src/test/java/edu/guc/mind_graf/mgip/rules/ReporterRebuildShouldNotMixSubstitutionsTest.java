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

public class ReporterRebuildShouldNotMixSubstitutionsTest {

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
    void reporterRebuildShouldNotMixDifferentSubstitutions() throws Exception {

        /*
         * Focused negative test for Change 1:
         *
         * Rule:
         * Fish(X) ∧ HasGills(X) => Aquatic(X)
         *
         * Known:
         * Fish(Nemo)
         * HasGills(Dory)
         *
         * Expected:
         * Aquatic(Nemo) should NOT be inferred.
         * Aquatic(Dory) should NOT be inferred.
         *
         * Why:
         * Fish(X) gives X = Nemo.
         * HasGills(X) gives X = Dory.
         * These substitutions are incompatible.
         */

        Node x = Network.createVariableNode("X", "propositionnode");

        Node nemo = Network.createNode("Nemo", "individualnode");
        Node dory = Network.createNode("Dory", "individualnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node hasGills = Network.createNode("HasGills", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        PropositionNode fishX = buildClassMembership(x, fish);
        PropositionNode hasGillsX = buildClassMembership(x, hasGills);
        PropositionNode aquaticX = buildClassMembership(x, aquatic);

        PropositionNode fishNemo = buildClassMembership(nemo, fish);
        PropositionNode hasGillsDory = buildClassMembership(dory, hasGills);

        PropositionNode aquaticNemo = buildClassMembership(nemo, aquatic);
        PropositionNode aquaticDory = buildClassMembership(dory, aquatic);

        RuleNode rule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX, hasGillsX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        )
                )
        );

        rule.setHyp(CONTEXT, BELIEF);

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "Aquatic(Nemo) should not be supported before the test."
        );

        assertFalse(
                aquaticDory.supported(CONTEXT, BELIEF, 0),
                "Aquatic(Dory) should not be supported before the test."
        );

        fishNemo.setHyp(CONTEXT, BELIEF);
        fishNemo.add(CONTEXT, BELIEF);
        Scheduler.schedule();

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "Aquatic(Nemo) should not be inferred after only Fish(Nemo)."
        );

        assertFalse(
                aquaticDory.supported(CONTEXT, BELIEF, 0),
                "Aquatic(Dory) should not be inferred after only Fish(Nemo)."
        );

        hasGillsDory.setHyp(CONTEXT, BELIEF);
        hasGillsDory.add(CONTEXT, BELIEF);
        Scheduler.schedule();

        System.out.println("\n=== DEBUG AFTER Fish(Nemo) + HasGills(Dory) ===");
        System.out.println("Fish(Nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("HasGills(Dory) supported? " + hasGillsDory.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(Nemo) supported? " + aquaticNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(Dory) supported? " + aquaticDory.supported(CONTEXT, BELIEF, 0));
        System.out.println("Forward asserted nodes: " + Scheduler.getForwardAssertedNodes().values());

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "FAILURE: Aquatic(Nemo) was inferred even though HasGills(Nemo) is missing."
        );

        assertFalse(
                aquaticDory.supported(CONTEXT, BELIEF, 0),
                "FAILURE: Aquatic(Dory) was inferred even though Fish(Dory) is missing."
        );

        boolean badForwardAssertion =
                Scheduler.getForwardAssertedNodes()
                        .values()
                        .stream()
                        .anyMatch(node ->
                                node.getName().equals(aquaticNemo.getName())
                             || node.getName().equals(aquaticDory.getName())
                        );

        assertFalse(
                badForwardAssertion,
                "FAILURE: An Aquatic node was forward asserted even though the substitutions do not match."
        );

        System.out.println("✓ Passed: incompatible substitutions were not mixed.");
    }

    private PropositionNode buildClassMembership(Node member, Node classNode) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("member"),
                                new NodeSet(member)
                        ),
                        new DownCable(
                                Network.getRelations().get("class"),
                                new NodeSet(classNode)
                        )
                )
        );
    }
}