package edu.guc.mind_graf.compression;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

public class CompressionTest {
    
    private static final String reality = "Reality";
    private static final String fiction = "Fiction";
    private static final String avalancheDilemma = "Avalanche Forecast Dilemma";

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        // Initialize fresh network for each test
        Set<String, Integer> attitudes = new Set<>();
        attitudes.add("Belief", 0);
        attitudes.add("Intention", 1);
        attitudes.add("Desire", 2);
        attitudes.add("Promise", 3);
        NetworkController.setUp(attitudes, new ArrayList<>(), false, false, false, 1);
        ContextController.createNewContext(reality);
        ContextController.createNewContext(fiction);
        ContextController.createNewContext(avalancheDilemma);
    }
     @Test
    void cliffDecisionTest() throws NoSuchTypeException {
        Node x= Network.createVariableNode("X", "individualnode");
        Node take = Network.createNode("Take", "individualnode");
        Node cliffPath = Network.createNode("Cliff Path", "individualnode");
        Node forestPath = Network.createNode("Forest Path", "individualnode");
        Node occur = Network.createNode("Ocurr", "individualnode");
        Node avalanche = Network.createNode("Avalanche", "individualnode");
        Node successful = Network.createNode("Successful", "individualnode");
        Node safe = Network.createNode("Safe", "individualnode");
        Node lyra= Network.createNode("Lyra", "individualnode");
        Node zero = Network.getBaseNodes().get("0");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        Relation objectRelation = Network.createRelation("object", "individualnode",
                Adjustability.NONE, 1);
        Relation propertyRelation = Network.createRelation("property", "individualnode",
                Adjustability.EXPAND, 1);
        PropositionNode m3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(lyra)),new DownCable(Network.getRelations().get("property"), new NodeSet(safe))));
        PropositionNode m4Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(lyra)),new DownCable(Network.getRelations().get("property"), new NodeSet(successful))));
        PropositionNode p1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(x)),new DownCable(Network.getRelations().get("property"), new NodeSet(safe))));
        PropositionNode p4Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(x)),new DownCable(Network.getRelations().get("verb"), new NodeSet(take)),new DownCable(Network.getRelations().get("object"), new NodeSet(forestPath))));
        PropositionNode m5Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(occur)),new DownCable(Network.getRelations().get("agent"), new NodeSet(avalanche))));
       // bridge rule
        DownCable desire1 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable belief1 = new DownCable(Network.createRelation("0-ant", "", Adjustability.NONE, 1), new NodeSet(m5Node));
        DownCable intend1 = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p4Node));
        PropositionNode m1Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(belief1, desire1, intend1, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));
        PropositionNode p2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(x)),new DownCable(Network.getRelations().get("property"), new NodeSet(successful))));
        PropositionNode p3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(x)),new DownCable(Network.getRelations().get("verb"), new NodeSet(take)),new DownCable(Network.getRelations().get("object"), new NodeSet(cliffPath))));
        PropositionNode m6Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("arg"), new NodeSet(m5Node)),new DownCable(Network.getRelations().get("min"), new NodeSet(zero)),new DownCable(Network.getRelations().get("max"), new NodeSet(zero))));
        DownCable desire2 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p2Node));
        DownCable belief2 = new DownCable(Network.createRelation("0-ant", "", Adjustability.NONE, 1), new NodeSet(m6Node));
        DownCable intend2 = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p3Node));
        PropositionNode m2Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(belief2, desire2, intend2, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));
        PropositionNode m7Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"), new NodeSet(lyra)),new DownCable(Network.getRelations().get("verb"), new NodeSet(take)),new DownCable(Network.getRelations().get("object"), new NodeSet(cliffPath))));
        ContextController.getContext(avalancheDilemma).addHypothesisToContext(0, 2, m3Node);
        ContextController.getContext(avalancheDilemma).addHypothesisToContext(0, 2, m4Node);
        ContextController.getContext(avalancheDilemma).addHypothesisToContext(0, 0, m6Node);
        ContextController.getContext(avalancheDilemma).addHypothesisToContext(0, 1, m7Node);
         
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        supportMap.put(2, new Pair<>( new PropositionNodeSet(m4Node.getId()), new PropositionNodeSet()));
        supportMap.put(0, new Pair<>( new PropositionNodeSet(m6Node.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet(m2Node.getId())));
        m7Node.addJustificationBasedSupports(1, 0, supports);
        ContextController.compress();

        assertNotNull(Network.getNodeById(m1Node.getId()));
        assertNotNull(Network.getNodeById(m2Node.getId()));
        assertNotNull(Network.getNodeById(m3Node.getId()));
        assertNotNull(Network.getNodeById(m4Node.getId()));
        assertNotNull(Network.getNodeById(m5Node.getId()));
        assertNotNull(Network.getNodeById(m6Node.getId()));
        assertNull(Network.getNodeById(m7Node.getId()));



    }
     @Test
    void veyraAndLighthouse() throws NoSuchTypeException {
        // Veyra promised her father she would protect their village by lighting the lighthouse before storms.
        //  She believes in keeping her promises.If it is believed that typhoon approaches, she intends takes
        //   a dangerous cliff path to save time. Upon reaching the top, she lights the lighthouse, guiding
        //    ships to safety. In the end, she fulfills her promise.
         Node x= Network.createVariableNode("X", "individualnode");
        Node protect = Network.createNode("Protect", "individualnode");
        Node village = Network.createNode("Village", "individualnode");
        Node typhoon = Network.createNode("Typhoon", "individualnode");
        Node near = Network.createNode("Near", "individualnode");
        Node lightHouse = Network.createNode("LightHouse", "individualnode");
        Node lighten= Network.createNode("Lighten", "individualnode");
        Node take = Network.createNode("Take", "individualnode");
        Node cliffPath= Network.createNode("Cliff Path", "individualnode");
        Node veyra= Network.createNode("Veyra", "individualnode");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        Relation objectRelation = Network.createRelation("object", "individualnode",
                Adjustability.NONE, 1);
        PropositionNode p1Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(protect)),new DownCable(Network.getRelations().get("object"),new NodeSet(village))));
        PropositionNode p2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(lighten)),new DownCable(Network.getRelations().get("object"),new NodeSet(lightHouse))));
        PropositionNode p3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(take)),new DownCable(Network.getRelations().get("object"),new NodeSet(cliffPath))));

        DownCable promise = new DownCable(Network.createRelation("3-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable belief = new DownCable(Network.createRelation("0-cq", "", Adjustability.NONE, 1), new NodeSet(p2Node));
        PropositionNode m1Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(belief, promise, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));
        
        PropositionNode m3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(typhoon)), new DownCable(Network.getRelations().get("verb"),new NodeSet(near))));
        DownCable belief1 = new DownCable(Network.createRelation("0-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable belief2 = new DownCable(Network.createRelation("0-ant", "", Adjustability.NONE, 1), new NodeSet(m3Node));
        DownCable desire2 = new DownCable(Network.createRelation("2-cq", "", Adjustability.NONE, 1), new NodeSet(p2Node));
        PropositionNode m2Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(belief2, desire2, belief1, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));


        DownCable desire3 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p2Node));
        DownCable intend = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p3Node));
        PropositionNode m4Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(intend, desire3, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));


        PropositionNode m5Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(veyra)), new DownCable(Network.getRelations().get("verb"),new NodeSet(protect)),new DownCable(Network.getRelations().get("object"),new NodeSet(village))));
        PropositionNode m6Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(veyra)), new DownCable(Network.getRelations().get("verb"),new NodeSet(lighten)),new DownCable(Network.getRelations().get("object"),new NodeSet(lightHouse))));
        PropositionNode m7Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(veyra)), new DownCable(Network.getRelations().get("verb"),new NodeSet(take)),new DownCable(Network.getRelations().get("object"),new NodeSet(cliffPath))));
        
        ContextController.getContext(fiction).addHypothesisToContext(0, 0, m3Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 0, m5Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 3, m5Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 2, m6Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 1, m7Node);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        supportMap.put(3, new Pair<>( new PropositionNodeSet(m5Node.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet(m1Node.getId())));
        m5Node.addJustificationBasedSupports(0, 0, supports);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap2 = new HashMap<>();
        supportMap2.put(0, new Pair<>( new PropositionNodeSet(m3Node.getId(),m5Node.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(supportMap2, new PropositionNodeSet(m2Node.getId())));
        m6Node.addJustificationBasedSupports(2, 0, supports2);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports3 = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap3 = new HashMap<>();
        supportMap3.put(2, new Pair<>( new PropositionNodeSet(m6Node.getId()), new PropositionNodeSet()));
        supports3.add(new Pair<>(supportMap3, new PropositionNodeSet(m4Node.getId())));
        m7Node.addJustificationBasedSupports(1, 0, supports3);

         ContextController.compress();

        assertNotNull(Network.getNodeById(m1Node.getId()));
        assertNotNull(Network.getNodeById(m2Node.getId()));
        assertNotNull(Network.getNodeById(m3Node.getId()));
        assertNotNull(Network.getNodeById(m4Node.getId()));
        assertNotNull(Network.getNodeById(m5Node.getId()));
        assertNull(Network.getNodeById(m6Node.getId()));
        assertNull(Network.getNodeById(m7Node.getId()));
}

 @Test
    void saveTheUniverse() throws NoSuchTypeException{
        Node x= Network.createVariableNode("X", "individualnode");
        Node save = Network.createNode("Save", "individualnode");
        Node universe = Network.createNode("Universe", "individualnode");
        Node fight = Network.createNode("Fight", "individualnode");
        Node plant = Network.createNode("Plant", "individualnode");
        Node tree = Network.createNode("Tree", "individualnode");
        Node avengers = Network.createNode("Avengers", "individualnode");
        Node capAmerica= Network.createNode("Captain America", "individualnode");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        Relation objectRelation = Network.createRelation("object", "individualnode",
                Adjustability.NONE, 1);
        PropositionNode p1Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(save)),new DownCable(Network.getRelations().get("object"),new NodeSet(universe))));
        PropositionNode p2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(fight)),new DownCable(Network.getRelations().get("object"),new NodeSet(avengers))));
        PropositionNode p3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("agent"),new NodeSet(x)), new DownCable(Network.getRelations().get("verb"),new NodeSet(plant)),new DownCable(Network.getRelations().get("object"),new NodeSet(tree))));
        
        DownCable desire1 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable intend1 = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p2Node));
        PropositionNode m1Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(intend1, desire1, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));
        
        DownCable desire4 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable intend4 = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p3Node));
        PropositionNode m4Node =  (PropositionNode)Network.createNode("bridgerule", new DownCableSet(intend4, desire4, new DownCable(Network.getRelations().get("forall"), new NodeSet(x))));
   
        PropositionNode m2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(save)),new DownCable(Network.getRelations().get("object"), new NodeSet(universe)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
        PropositionNode m3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(fight)),new DownCable(Network.getRelations().get("object"), new NodeSet(avengers)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
        PropositionNode m5Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(plant)),new DownCable(Network.getRelations().get("object"), new NodeSet(tree)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
        ContextController.getContext(fiction).addHypothesisToContext(0, 2, m2Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 1, m3Node);
        ContextController.getContext(reality).addHypothesisToContext(0, 2, m2Node);


        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap2 = new HashMap<>();
        supportMap2.put(2, new Pair<>( new PropositionNodeSet(m2Node.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(supportMap2, new PropositionNodeSet(m4Node.getId())));
        m5Node.addJustificationBasedSupports(1, 0, supports2);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports3 = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap3 = new HashMap<>();
        supportMap3.put(2, new Pair<>( new PropositionNodeSet(m2Node.getId()), new PropositionNodeSet()));
        supports3.add(new Pair<>(supportMap3, new PropositionNodeSet(m1Node.getId())));
        m3Node.addJustificationBasedSupports(1, 0, supports3);

         ContextController.compress();

        assertNotNull(Network.getNodeById(m1Node.getId()));
        assertNotNull(Network.getNodeById(m2Node.getId()));
        assertNull(Network.getNodeById(m3Node.getId()));
        assertNotNull(Network.getNodeById(m4Node.getId()));
        assertNull(Network.getNodeById(m5Node.getId()));
        

     
    }

 }
