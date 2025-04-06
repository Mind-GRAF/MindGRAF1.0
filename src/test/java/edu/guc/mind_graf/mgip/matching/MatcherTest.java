package edu.guc.mind_graf.mgip.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;

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
        network = NetworkController.setUp(attitudeNames, consistentAttitudes, true, false, false, 1);
        ContextController.createNewContext("guc");
    }

    @Test
    void nodeBasedNone() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void nodeBasedNoneEmpty() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void wireBasedReduceType1() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void wireBasedReduceType2() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void wireBasedExpandType1() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void wireBasedExpandType2() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void uvbrCheckVarTerm() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

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

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(1, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var2, var1);

            List<Substitutions> switchSubs = new ArrayList<>();
            switchSubs.add(switch1);

            int matchType = 0;

            assertTrue(matchList.get(0).getFilterSubs().getMap().isEmpty());
            assertTrue(Substitutions.testContains(switchSubs, matchList.get(0).getSwitchSubs()));
            assertEquals(M0, matchList.get(0).getNode());
            assertEquals(matchType, matchList.get(0).getMatchType());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void uvbrCheckVarVar() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node var2 = Network.createVariableNode("var2", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(var1));
            DownCable d2 = new DownCable(prop, new NodeSet(cs));

            DownCable d3 = new DownCable(obj, new NodeSet(var2));
            DownCable d4 = new DownCable(prop, new NodeSet(var1));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d4));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void occursCheck() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node var1 = Network.createVariableNode("var1", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);
            Relation father = Network.createRelation("father", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(var1));

            DownCable d2 = new DownCable(prop, new NodeSet(var1));
            Node M0 = Network.createNode("propositionnode", new DownCableSet(d2));

            DownCable molecularCable = new DownCable(father, new NodeSet(M0));

            DownCable d3 = new DownCable(obj, new NodeSet(var1));
            DownCable d4 = new DownCable(father, new NodeSet(var1));

            Node M1 = Network.createNode("propositionnode", new DownCableSet(d1, molecularCable));
            Node M2 = Network.createNode("propositionnode", new DownCableSet(d3, d4));

            List<Match> matchList = Matcher.match(M2, ctx, 0);

            assertEquals(0, matchList.size());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void EmptyPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node var2 = Network.createVariableNode("var2", "propositionnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);

            obj.setPath(new edu.guc.mind_graf.paths.EmptyPath());

            DownCable d1 = new DownCable(obj, new NodeSet(cs, phy));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(var1, var2));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

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
    void BangPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node theory = Network.createNode("theory", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");
            int attitude = 0;
            int level = Network.currentLevel;

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation q = Network.createRelation("q", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.ComposePath(new edu.guc.mind_graf.paths.BangPath(),
                    new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            ctx.addHypothesisToContext(level, attitude, ((PropositionNode) M0));

            List<Match> matchList = Matcher.match(M1, ctx, attitude);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

            List<Substitutions> switchSubs = new ArrayList<>();
            switchSubs.add(switch1);
            switchSubs.add(switch2);

            int matchType = 0;

            for (Match m : matchList) {
                assertTrue(m.getFilterSubs().getMap().isEmpty());
                assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
                assertEquals(M0, m.getNode());
                assertEquals(matchType, m.getMatchType());
                if (m.getSwitchSubs().get(var).equals(phy)) {
                    assertTrue(((Support) m.getSupport()).getJustificationBasedSupport().get(level).get(attitude).getFirst()
                            .getFirst().get(attitude).getFirst().contains(M0));
                }
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void KStarPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.KStarPath(new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void KPlusPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.KPlusPath(new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void FUnitPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.FUnitPath(p));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void BUnitPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.BUnitPath(p));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1));
            M0.getUpCableSet().addNode(p, phy);
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void AndPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.NONE, 0);
            Relation q = Network.createRelation("q", "", Adjustability.NONE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.AndPath(new edu.guc.mind_graf.paths.BUnitPath(q),
                    new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(1, matchList.size());

            int matchType = 0;

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            List<Substitutions> switchSubs = new ArrayList<>();
            switchSubs.add(switch1);

            assertTrue(matchList.get(0).getFilterSubs().getMap().isEmpty());
            assertTrue(Substitutions.testContains(switchSubs, matchList.get(0).getSwitchSubs()));
            assertEquals(M0, matchList.get(0).getNode());
            assertEquals(matchType, matchList.get(0).getMatchType());

            M0.getUpCableSet().addNode(q, phy);

            matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

            switchSubs.add(switch2);

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
    void OrPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node theory = Network.createNode("theory", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation q = Network.createRelation("q", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.OrPath(new edu.guc.mind_graf.paths.BUnitPath(q),
                    new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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

            M0.getUpCableSet().addNode(q, theory);

            matchList = Matcher.match(M1, ctx, 0);

            assertEquals(3, matchList.size());

            Substitutions switch3 = new Substitutions();
            switch3.add(var, theory);

            switchSubs.add(switch3);

            for (Match m : matchList) {
                assertTrue(m.getFilterSubs().getMap().isEmpty());
                assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
                assertEquals(M0, m.getNode());
                if (m.getSwitchSubs().get(var).equals(cs))
                    assertEquals(0, m.getMatchType());
                else
                    assertEquals(1, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void ComposePath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.ComposePath(new edu.guc.mind_graf.paths.FUnitPath(p),
                    new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));
            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d2));

            DownCable d4 = new DownCable(p, new NodeSet(M0));

            Node M1 = Network.createNode("propositionnode", new DownCableSet(d1, d4));
            Node M2 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M2, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

            List<Substitutions> switchSubs = new ArrayList<>();
            switchSubs.add(switch1);
            switchSubs.add(switch2);

            int matchType = 0;

            for (Match m : matchList) {
                assertTrue(m.getFilterSubs().getMap().isEmpty());
                assertTrue(Substitutions.testContains(switchSubs, m.getSwitchSubs()));
                assertEquals(M1, m.getNode());
                assertEquals(matchType, m.getMatchType());
            }
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void ConversePath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.ConversePath(new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1));
            M0.getUpCableSet().addNode(p, phy);
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void IrreflexiveRestrictPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.IrreflexiveRestrictPath(new edu.guc.mind_graf.paths.FUnitPath(p)));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));

            DownCable d3 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void DomainRestrictPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node n = Network.createNode("n", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation q = Network.createRelation("q", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.DomainRestrictPath(new edu.guc.mind_graf.paths.FUnitPath(q),
                    new edu.guc.mind_graf.paths.FUnitPath(p), n));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));
            DownCable d3 = new DownCable(q, new NodeSet(n));

            DownCable d4 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2, d3));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d4));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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
    void RangeRestrictPath() throws DirectCycleException {
        try {
            Context ctx = ContextController.getContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node phy = Network.createNode("phy", "propositionnode");
            Node n = Network.createNode("n", "propositionnode");
            Node var = Network.createVariableNode("var", "propositionnode");

            Relation p = Network.createRelation("p", "", Adjustability.REDUCE, 0);
            Relation q = Network.createRelation("q", "", Adjustability.REDUCE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.REDUCE, 0);
            obj.setPath(new edu.guc.mind_graf.paths.RangeRestrictPath(new edu.guc.mind_graf.paths.BUnitPath(q),
                    new edu.guc.mind_graf.paths.FUnitPath(p), n));

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(p, new NodeSet(phy));
            phy.getUpCableSet().addNode(q, n);

            DownCable d4 = new DownCable(obj, new NodeSet(var));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d4));

            List<Match> matchList = Matcher.match(M1, ctx, 0);

            assertEquals(2, matchList.size());

            Substitutions switch1 = new Substitutions();
            switch1.add(var, cs);

            Substitutions switch2 = new Substitutions();
            switch2.add(var, phy);

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

}