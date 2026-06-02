package edu.guc.mind_graf.Context;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    Network n;
    PropositionNode p;
    PropositionNode notP;
    PropositionNode pImpliesQ;
    PropositionNode q;
    PropositionNode parent;
    Context guc;

    @BeforeEach
    void setup() throws NoSuchTypeException {
        System.out.println("Testing Context class");
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);
        attitudeNames.add("fears", 2);
        attitudeNames.add("hate", 3);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 2)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 2, 3)));

        n = NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext("guc");
        ContextController.setCurrContext("guc");
        guc = ContextController.getContext(ContextController.getCurrContextName());
        p = (PropositionNode) Network.createNode("p", "propositionnode");
        pImpliesQ = (PropositionNode) Network.createNode("pImpliesQ", "propositionnode");
        q = (PropositionNode) Network.createNode("q", "propositionnode");

        HashMap<String, Relation> relations = Network.getRelations();

        NodeSet ns1 = new NodeSet();
        ns1.add(p);
        NodeSet ns2 = new NodeSet();
        ns2.add(Network.getBaseNodes().get("0"));

        DownCable downCable1 = new DownCable(relations.get("arg"), ns1);
        DownCable downCable2 = new DownCable(relations.get("min"), ns2);
        DownCable downCable3 = new DownCable(relations.get("max"), ns2);

        DownCableSet downCableSet1 = new DownCableSet(downCable1, downCable2, downCable3);

        notP = (PropositionNode) Network.createNode("propositionnode", downCableSet1);

        HashMap<String, Node> baseNodes = Network.getBaseNodes();

        NodeSet ns3 = new NodeSet();
        ns2.add(p);
        NodeSet ns4 = new NodeSet();
        ns4.add(baseNodes.get("2"));

        DownCable downCable4 = new DownCable(relations.get("prop"), ns3);
        DownCable downCable5 = new DownCable(relations.get("grade"), ns4);

        DownCableSet downCableSet2 = new DownCableSet(downCable4, downCable5);

        parent = (PropositionNode) Network.createNode("propositionnode", downCableSet2);
    }

    @Test
    void addHypothesisToContext() {
        ContextController.addHypothesisToContext(0,0,p.getId());
        boolean insertedCorrectly = guc.getHypotheses().get(0)[0].getFirst().contains(p);
        System.out.println("The output of hypotheses.contains(p) is: "+ insertedCorrectly);
        Assertions.assertTrue(insertedCorrectly);
    }

    @Test
    void removeHypothesisFromContext() {
        ContextController.addHypothesisToContext(0,0,p.getId());
        boolean insertedCorrectly = guc.getHypotheses().get(0)[0].getFirst().contains(p);
        System.out.println("The output of hypotheses.contains(p) is: "+ insertedCorrectly);
        Assertions.assertTrue(insertedCorrectly);

        ContextController.removeHypothesisFromContext(0,0,p.getId());
        boolean removedCorrectly = guc.getHypotheses().get(0)[0].getFirst().contains(p);
        System.out.println("The output of hypotheses.contains(p) is: "+ removedCorrectly);
        Assertions.assertFalse(removedCorrectly);
    }

    @Test
    void isHypothesis1() {
        ContextController.addHypothesisToContext(0,0,p.getId());
        boolean  insertedCorrectly = guc.isHypothesis(0,0,p);
        System.out.println("The output of isHypothesis is: "+ insertedCorrectly);
        Assertions.assertTrue(insertedCorrectly);
    }

    @Test
    void isHypothesis2() {
        boolean  notInserted = guc.isHypothesis(0,0,p);
        System.out.println("The output of isHypothesis is: "+ notInserted);
        Assertions.assertFalse(notInserted);
    }

    @Test
    void isOriginHypothesis() {
        ContextController.addHypothesisToContext(0,0,p.getId());
        boolean  insertedCorrectly = guc.isOriginHypothesis(0,0,p);
        System.out.println("The output of isOriginHypothesis is: "+ insertedCorrectly);
        Assertions.assertTrue(insertedCorrectly);
    }

    @Disabled
    @Test
    void manuallyRemoveInferredNodeFromContext() {
        ContextController.addHypothesisToContext(0,0,p.getId());
        ContextController.addHypothesisToContext(0,0,pImpliesQ.getId());
        boolean isQHypothesis = guc.getHypotheses().get(0)[0].getFirst().contains(q);
        System.out.println("The output of hypotheses.contains(q) is: "+ isQHypothesis);
        Assertions.assertFalse(isQHypothesis);

        guc.manuallyRemoveInferredNodeFromContext(0,0,q);
    }

    @Test
    void completelyRemoveNodeFromContext() {
    }

    @Test
    void automaticallyRemoveInferredNodeFromContext() {
    }


    public Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> getSupport(){
        HashMap<Integer,Pair<PropositionNodeSet,PropositionNodeSet>> hm = new HashMap<>();
        Pair<PropositionNodeSet,PropositionNodeSet> pair = new Pair<>();
        pair.setFirst(new PropositionNodeSet(p,pImpliesQ));
        hm.put(0,pair);

        Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> output = new Pair<>(hm,null);
        return output;
    }
}