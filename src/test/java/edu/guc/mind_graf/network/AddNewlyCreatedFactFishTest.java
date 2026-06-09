package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * Tests Change 10 using a newly created fact.
 *
 * Important:
 * add() does not create the proposition node from nothing.
 * Network.createNode(...) creates the node in the network.
 * add(...) should then assert/support that newly created node.
 *
 * Test idea:
 * 1. Create Fish(nemo) fresh.
 * 2. Check it is not supported yet.
 * 3. Call fishNemo.add(...).
 * 4. Check it became supported.
 */
public class AddNewlyCreatedFactFishTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(
                attitudeNames,
                consistentAttitudes,
                false,
                false,
                false,
                1
        );

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    public void add_shouldSupportNewlyCreatedFishFact()
            throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {

        System.out.println("\n====================================================");
        System.out.println("CHANGE 10 TEST: add() supports a newly created fact");
        System.out.println("The node is created first, then add() asserts it.");
        System.out.println("Expected after fix: Fish(nemo) becomes supported.");
        System.out.println("====================================================\n");

        /*
         * These nodes are newly created in this test.
         */
        Node nemo = Network.createNode("nemo", "propositionnode");
        Node fish = Network.createNode("Fish", "propositionnode");

        /*
         * Fish(nemo) is now in the network, but it is not supported yet.
         */
        PropositionNode fishNemo = memberClass(nemo, fish);

        System.out.println("After createNode only:");
        System.out.println("Fish(nemo) supported? "
                + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Fish(nemo) support object: "
                + fishNemo.getSupport());

        assertFalse(
                fishNemo.supported(CONTEXT, BELIEF, 0),
                "A newly created proposition should not be supported before add()."
        );

        /*
         * This is the actual forward assertion step.
         */
        fishNemo.add(CONTEXT, BELIEF);

        System.out.println("\nAfter add():");
        System.out.println("Fish(nemo) supported? "
                + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Fish(nemo) support object: "
                + fishNemo.getSupport());
        System.out.println("Forward asserted/inferred nodes: "
                + Scheduler.getForwardAssertedNodes().values());

        assertTrue(
                fishNemo.supported(CONTEXT, BELIEF, 0),
                "After the fix, add() should support the newly created fact."
        );
    }

    private PropositionNode memberClass(Node member, Node clazz) throws NoSuchTypeException {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("member"),
                                new NodeSet(member)
                        ),
                        new DownCable(
                                Network.getRelations().get("class"),
                                new NodeSet(clazz)
                        )
                )
        );
    }
}