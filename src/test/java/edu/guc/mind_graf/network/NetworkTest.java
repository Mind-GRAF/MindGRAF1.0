package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.matching.Match;
import edu.guc.mind_graf.mgip.matching.Matcher;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

class NetworkTest {
	Network n;

	@BeforeEach
	void setUp() {
		n = new Network();
		System.out.println("Testing Network");
		Set<String, Integer> attitudeNames = new Set<>();
		attitudeNames.add("beliefs", 0);
		attitudeNames.add("obligations", 1);

		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
		consistentAttitudes.add(new ArrayList<>(List.of(0)));
		consistentAttitudes.add(new ArrayList<>(List.of(1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));

		ContextController.setUp(attitudeNames, consistentAttitudes, false);
		ContextController.createNewContext("guc");
	}

	// @Test
	// void test1() {
	// try {
	// System.out.println("Testing example 1: Who is Aquatic");
	// ContextController.setCurrContext("guc");
	// Node nemo = Network.createNode("nemo", "propositionnode");
	// Node clownFish = Network.createNode("clown-fish", "propositionnode");
	// Node aquatic = Network.createNode("aquatic", "propositionnode");
	// Node x = Network.createVariableNode("X", "propositionnode");
	// Relation mem = Network.createRelation("mem", "", Adjustability.NONE, 2);
	// Relation cla = Network.createRelation("class", "", Adjustability.NONE, 2);

	// NodeSet ns1 = new NodeSet();
	// ns1.add(nemo);
	// NodeSet ns2 = new NodeSet();
	// ns2.add(clownFish);
	// NodeSet ns3 = new NodeSet();
	// ns3.add(x);

	// DownCable downCableMemM0 = new DownCable(mem, ns1);
	// DownCable downCableClassM0 = new DownCable(cla, ns2);
	// DownCable downCableMemM1 = new DownCable(mem, ns3);
	// DownCable downCableClassM1 = new DownCable(cla, ns2);

	// DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,
	// downCableClassM0);
	// DownCableSet downCableSetM1 = new DownCableSet(downCableMemM1,
	// downCableClassM1);

	// Node M0 = Network.createNode("propositionnode", downCableSetM0);
	// Node M1 = Network.createNode("propositionnode", downCableSetM1);

	// Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
	// n.printNodes();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	@Test
	void nodeBasedExact() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");
			Node var2 = Network.createVariableNode("var2", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(var1, var2));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(2, matchList.size());

			Substitutions switch1 = new Substitutions();
			switch1.add(var1, cs);
			switch1.add(var2, phy);

			Substitutions switch2 = new Substitutions();
			switch2.add(var1, phy);
			switch2.add(var2, cs);

			List<Substitutions> switchSubs = new ArrayList<>();
			switchSubs.add(switch1);
			switchSubs.add(switch2);

			int matchType = 0;

			for (Match m : matchList) {
				assertTrue(m.getFilterSubs().getMap().isEmpty());
				assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
				assertEquals(m.getNode(), M0);
				assertEquals(m.getMatchType(), matchType);
			}
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void nodeBasedExactEmpty() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node math = Network.createNode("math", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(math, var1));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(0, matchList.size());
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void nodeBasedReduceType1() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 1);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(var1));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(2, matchList.size());

			Substitutions switch1 = new Substitutions();
			switch1.add(var1, cs);

			Substitutions switch2 = new Substitutions();
			switch2.add(var1, phy);

			List<Substitutions> switchSubs = new ArrayList<>();
			switchSubs.add(switch1);
			switchSubs.add(switch2);

			int matchType = 1;

			for (Match m : matchList) {
				assertTrue(m.getFilterSubs().getMap().isEmpty());
				assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
				assertEquals(m.getNode(), M0);
				assertEquals(m.getMatchType(), matchType);
			}
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void nodeBasedReduceType2() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");
			Node var2 = Network.createVariableNode("var2", "propositionnode");
			Node var3 = Network.createVariableNode("var3", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 1);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(var1, var2, var3));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(6, matchList.size());

			Substitutions switch1 = new Substitutions();
			switch1.add(var1, cs);
			switch1.add(var2, phy);
			switch1.add(var3, null);

			Substitutions switch2 = new Substitutions();
			switch2.add(var1, phy);
			switch2.add(var2, cs);
			switch2.add(var3, null);

			Substitutions switch3 = new Substitutions();
			switch3.add(var1, cs);
			switch3.add(var2, null);
			switch3.add(var3, phy);

			Substitutions switch4 = new Substitutions();
			switch4.add(var1, phy);
			switch4.add(var2, null);
			switch4.add(var3, cs);

			Substitutions switch5 = new Substitutions();
			switch5.add(var1, null);
			switch5.add(var2, cs);
			switch5.add(var3, phy);

			Substitutions switch6 = new Substitutions();
			switch6.add(var1, null);
			switch6.add(var2, phy);
			switch6.add(var3, cs);

			List<Substitutions> switchSubs = new ArrayList<>();
			switchSubs.add(switch1);
			switchSubs.add(switch2);
			switchSubs.add(switch3);
			switchSubs.add(switch4);
			switchSubs.add(switch5);
			switchSubs.add(switch6);

			int matchType = 2;

			for (Match m : matchList) {
				assertTrue(m.getFilterSubs().getMap().isEmpty());
				assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
				assertEquals(m.getNode(), M0);
				assertEquals(m.getMatchType(), matchType);
			}
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void nodeBasedExpandType1() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");
			Node var2 = Network.createVariableNode("var2", "propositionnode");
			Node var3 = Network.createVariableNode("var3", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.EXPAND, 0);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(var1, var2, var3));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(6, matchList.size());

			Substitutions switch1 = new Substitutions();
			switch1.add(var1, cs);
			switch1.add(var2, phy);
			switch1.add(var3, null);

			Substitutions switch2 = new Substitutions();
			switch2.add(var1, phy);
			switch2.add(var2, cs);
			switch2.add(var3, null);

			Substitutions switch3 = new Substitutions();
			switch3.add(var1, cs);
			switch3.add(var2, null);
			switch3.add(var3, phy);

			Substitutions switch4 = new Substitutions();
			switch4.add(var1, phy);
			switch4.add(var2, null);
			switch4.add(var3, cs);

			Substitutions switch5 = new Substitutions();
			switch5.add(var1, null);
			switch5.add(var2, cs);
			switch5.add(var3, phy);

			Substitutions switch6 = new Substitutions();
			switch6.add(var1, null);
			switch6.add(var2, phy);
			switch6.add(var3, cs);

			List<Substitutions> switchSubs = new ArrayList<>();
			switchSubs.add(switch1);
			switchSubs.add(switch2);
			switchSubs.add(switch3);
			switchSubs.add(switch4);
			switchSubs.add(switch5);
			switchSubs.add(switch6);

			int matchType = 1;

			for (Match m : matchList) {
				assertTrue(m.getFilterSubs().getMap().isEmpty());
				assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
				assertEquals(m.getNode(), M0);
				assertEquals(m.getMatchType(), matchType);
			}
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void nodeBasedExpandType2() {
		Node cs;
		try {
			cs = Network.createNode("cs", "propositionnode");
			Node phy = Network.createNode("phy", "propositionnode");
			Node fun = Network.createNode("fun", "propositionnode");
			Node var1 = Network.createVariableNode("var1", "propositionnode");

			Relation obj = Network.createRelation("obj", "", Adjustability.EXPAND, 0);
			Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

			DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
			DownCable d2 = new DownCable(prop, new NodeSet(fun));

			DownCable d3 = new DownCable(obj, new NodeSet(var1));

			Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
			Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

			ArrayList<Match> matchList = Matcher.match(M1);

			assertEquals(2, matchList.size());

			Substitutions switch1 = new Substitutions();
			switch1.add(var1, cs);

			Substitutions switch2 = new Substitutions();
			switch2.add(var1, phy);

			List<Substitutions> switchSubs = new ArrayList<>();
			switchSubs.add(switch1);
			switchSubs.add(switch2);

			int matchType = 2;

			for (Match m : matchList) {
				assertTrue(m.getFilterSubs().getMap().isEmpty());
				assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
				assertEquals(m.getNode(), M0);
				assertEquals(m.getMatchType(), matchType);
			}
		} catch (NoSuchTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}