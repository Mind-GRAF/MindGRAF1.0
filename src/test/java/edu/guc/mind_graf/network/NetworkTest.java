package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class NetworkTest {

	@BeforeEach
	void setUp() {
		System.out.println("Testing Network");
		Set<String,Integer> attitudeNames = new Set<>();
		attitudeNames.add( "beliefs",0);
		attitudeNames.add("obligations",1);
		attitudeNames.add("fears",2);
		attitudeNames.add("hate",3);
		
		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
		consistentAttitudes.add(new ArrayList<>(List.of(0)));
		consistentAttitudes.add(new ArrayList<>(List.of(1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,2)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,2,3)));
		
		ContextController.setUp(attitudeNames,consistentAttitudes ,false);
		ContextController.createNewContext("guc");
	}
	
	@Test
	void test1(){
		try {
			System.out.println("Testing example 1: Who is Aquatic");
			ContextController.setCurrContext("guc");
			Node nemo = Network.createNode("nemo", "propositionnode");
			Node clownFish = Network.createNode("clown-fish", "propositionnode");
			Node aquatic = Network.createNode("aquatic", "propositionnode");
			Node x = Network.createVariableNode("X", "propositionnode");
			Relation mem = Network.createRelation("mem", "", Adjustability.NONE,2);
			Relation cla = Network.createRelation("class", "", Adjustability.NONE,2);
			
			NodeSet ns1 = new NodeSet();
			ns1.add(nemo);
			NodeSet ns2 = new NodeSet();
			ns2.add(clownFish);
			NodeSet ns3 = new NodeSet();
			ns3.add(x);
			
			DownCable downCableMemM0 = new DownCable(mem, ns1);
			DownCable downCableClassM0 = new DownCable(cla, ns2);
			DownCable downCableMemM1 = new DownCable(mem, ns3);
			DownCable downCableClassM1 = new DownCable(cla, ns2);
			
			DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,downCableClassM0);
			DownCableSet downCableSetM1 = new DownCableSet(downCableMemM1,downCableClassM1);
			
			Node M0 = Network.createNode("propositionnode", downCableSetM0);
			Node M1 = Network.createNode("propositionnode", downCableSetM1);
			
			
//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}