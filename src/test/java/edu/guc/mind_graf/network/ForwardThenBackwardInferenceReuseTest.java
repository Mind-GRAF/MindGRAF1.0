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
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForwardThenBackwardInferenceReuseTest {

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
    void forwardInferredKnownInstance_isReusedByBackwardQuery() throws Exception {
        Node nemo = Network.createNode("nemo", "individualnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");
        Node hasGills = Network.createNode("HasGills", "propositionnode");
        Node waterAnimal = Network.createNode("WaterAnimal", "propositionnode");

        Node x = Network.createVariableNode("X", "individualnode");

        PropositionNode fishX = memberClass(x, fish);
        PropositionNode aquaticX = memberClass(x, aquatic);
        PropositionNode hasGillsX = memberClass(x, hasGills);
        PropositionNode waterAnimalX = memberClass(x, waterAnimal);

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode hasGillsNemo = memberClass(nemo, hasGills);
        PropositionNode waterAnimalNemo = memberClass(nemo, waterAnimal);

        RuleNode fishImpliesAquatic = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"), new NodeSet(fishX)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(aquaticX))
                )
        );

        RuleNode aquaticAndGillsImpliesWaterAnimal = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"), new NodeSet(aquaticX, hasGillsX)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(waterAnimalX))
                )
        );

        fishImpliesAquatic.setHyp(CONTEXT, BELIEF);
        aquaticAndGillsImpliesWaterAnimal.setHyp(CONTEXT, BELIEF);

        assertFalse(
                waterAnimalNemo.supported(CONTEXT, BELIEF, 0),
                "WaterAnimal(nemo) should not be supported before any inference runs."
        );

        fishNemo.add(CONTEXT, BELIEF);

        List<Node> forwardNodesAfterFish =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        assertTrue(
                containsNode(forwardNodesAfterFish, "Aquatic", "nemo"),
                "Forward inference should infer Aquatic(nemo) after Fish(nemo) is added."
        );

        hasGillsNemo.setHyp(CONTEXT, BELIEF);

        assertFalse(
                waterAnimalNemo.supported(CONTEXT, BELIEF, 0),
                "WaterAnimal(nemo) should still be unsupported before the backward query."
        );

        waterAnimalX.deduce(CONTEXT, BELIEF);

        Collection<PropositionNode> backwardAnswers =
                Scheduler.getBackwardAssertedReplyNodes().values();

        System.out.println("\n=== DEBUG FORWARD-THEN-BACKWARD REUSE TEST ===");
        System.out.println("Forward nodes after add(Fish(nemo)): " + forwardNodesAfterFish);
        System.out.println("Backward answers after deduce(WaterAnimal(X)): " + backwardAnswers);
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("HasGills(nemo) supported? " + hasGillsNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("WaterAnimal(nemo) supported? " + waterAnimalNemo.supported(CONTEXT, BELIEF, 0));

        assertFalse(
                backwardAnswers.isEmpty(),
                "The backward query should produce an answer using the forward-inferred Aquatic(nemo)."
        );

        assertTrue(
                containsNode(backwardAnswers, "WaterAnimal", "nemo"),
                "Backward inference should answer WaterAnimal(nemo)."
        );

        assertFalse(
                waterAnimalNemo.supported(CONTEXT, BELIEF, 0),
                "The open query returns a grounded answer node; it should not require the pre-created closed query node to become supported."
        );
    }

    private PropositionNode memberClass(Node member, Node clazz) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(member)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(clazz))
                )
        );
    }

    private boolean containsNode(Collection<? extends Node> nodes, String... requiredText) {
        return nodes.stream().anyMatch(node -> {
            String text = node.toString();
            for (String required : requiredText) {
                if (!text.contains(required)) {
                    return false;
                }
            }
            return true;
        });
    }
}
