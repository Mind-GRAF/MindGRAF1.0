package edu.guc.mind_graf.mgip.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class MatcherTest {
    Network network;

    @BeforeEach
    void setUp() {
        network = new Network();
        System.out.println("Testing Matcher...");
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

    @Test
    void nodeBasedNone() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

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
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void nodeBasedNoneEmpty() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void wireBasedReduceType1() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

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
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void wireBasedReduceType2() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

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
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void wireBasedExpandType1() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

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
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void wireBasedExpandType2() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
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

            List<Match> matchList = Matcher.match(M1);

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
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void uvbrCheckVarTerm() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node var2 = Network.createVariableNode("var2", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(cs, var1));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(cs, var2));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

            List<Match> matchList = Matcher.match(M1);

            assertEquals(1, matchList.size());

            Substitutions filter = new Substitutions();
            filter.add(var1, var2);

            List<Substitutions> filterSubs = new ArrayList<>();
            filterSubs.add(filter);

            int matchType = 0;

            assertTrue(Substitutions.testContains(filterSubs, matchList.get(0).getFilterSubs()));
            assertTrue(matchList.get(0).getSwitchSubs().getMap().isEmpty());
            assertEquals(M0, matchList.get(0).getNode());
            assertEquals(matchType, matchList.get(0).getMatchType());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void uvbrCheckVarVar() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node var2 = Network.createVariableNode("var2", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(cs, cs));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(var1, var2));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

            List<Match> matchList = Matcher.match(M1);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void occursCheck() {
        try {
            Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(cs, var1));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(var1, var1));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

            List<Match> matchList = Matcher.match(M1);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}