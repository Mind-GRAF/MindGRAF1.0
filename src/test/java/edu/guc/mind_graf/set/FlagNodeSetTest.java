package edu.guc.mind_graf.set;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class FlagNodeSetTest {

    FlagNodeSet fns1;
    FlagNodeSet fns2;
    FlagNode fns1Fn;
    FlagNode fns2Fn;


    @BeforeEach
    void setUp() throws NoSuchTypeException {
        new Network();
        Node testingNode = Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("arg"), new NodeSet())));
        fns1 = new FlagNodeSet();
        fns1Fn = new FlagNode(testingNode, true, new PropositionNodeSet());
        fns1.addFlagNode(fns1Fn);
        fns2 = new FlagNodeSet();
        fns2Fn = new FlagNode(testingNode, true, new PropositionNodeSet());
        fns2.addFlagNode(fns2Fn);
    }

    @Test
    void combine() {
        FlagNodeSet fns3 = fns1.combine(fns2);
        assertEquals(1, fns3.size());
    }

    @Test
    void intersection() throws NoSuchTypeException {
        assertTrue(fns1Fn.equals(fns2Fn));
        assertTrue(fns2.contains(fns1Fn));
        assertEquals(1, fns1.intersection(fns2).size());
    }
}