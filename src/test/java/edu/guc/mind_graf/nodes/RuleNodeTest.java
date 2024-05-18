package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleNodeTest {

    Network network;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));

        network = NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext("Dystopia");
        ContextController.createNewContext("Mythology");
        ContextController.createNewContext("PetStore");
        ContextController.setCurrContext("Dystopia");

    }

    @Test
    void applyRuleHandler_AndEntail() throws NoSuchTypeException {
        Node G = Network.createVariableNode("X", "propositionnode");
        Node C = Network.createVariableNode("Y", "propositionnode");
        Node Co = Network.createVariableNode("Z", "propositionnode");

        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(G));
        Node government = Network.createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(C));
        Node civilian = Network.createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMem, civ));

        DownCable coMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Co));
        Node country = Network.createNode("country", "propositionnode");
        DownCable coun = new DownCable(Network.getRelations().get("class"), new NodeSet(country));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(coMem, coun));

        Relation rule = Network.createRelation("rule", "",
                Adjustability.EXPAND, 2);
        Relation ruled = Network.createRelation("ruled", "",
                Adjustability.EXPAND, 2);
        DownCable ruling = new DownCable(rule, new NodeSet(G));
        DownCable ruledC = new DownCable(ruled, new NodeSet(Co));
        Node M3 = Network.createNode("propositionnode", new DownCableSet(ruling, ruledC));

        Relation living = Network.createRelation("living", "",
                Adjustability.EXPAND, 2);
        Relation in = Network.createRelation("in", "",
                Adjustability.EXPAND, 2);
        DownCable livingC = new DownCable(living, new NodeSet(C));
        DownCable inC = new DownCable(in, new NodeSet(Co));
        Node M4 = Network.createNode("propositionnode", new DownCableSet(livingC, inC));

        Relation brainwash = Network.createRelation("brainwash", "propositionnode",
                Adjustability.EXPAND, 2);
        Relation brainwashed = Network.createRelation("brainwashed", "propositionnode",
                Adjustability.EXPAND, 2);
        DownCable brainwashG = new DownCable(brainwash, new NodeSet(G));
        DownCable brainwashedC = new DownCable(brainwashed, new NodeSet(C));
        Node M5 = Network.createNode("propositionnode", new DownCableSet(brainwashG, brainwashedC));

        Node P0 = Network.createNode("andentailment", new DownCableSet(new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1, M2, M3, M4)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M5))));

        Substitutions govVSubs = new Substitutions();
        Node voldemort = Network.createNode("voldemort", "individualnode");
        govVSubs.add(G, voldemort);
        Report report01 = new Report(govVSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report01.setReportType(ChannelType.AntRule);
        report01.setContextName("Dystopia");

        Substitutions govDSubs = new Substitutions();
        Node daenerys = Network.createNode("daenerys", "individualnode");
        govDSubs.add(G, daenerys);
        Report report00 = new Report(govDSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report00.setReportType(ChannelType.AntRule);
        report00.setContextName("Dystopia");

        Substitutions govSubs = new Substitutions();
        Node henry = Network.createNode("henry", "individualnode");
        govSubs.add(G, henry);
        Report report0 = new Report(govSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report0.setReportType(ChannelType.AntRule);
        report0.setContextName("Dystopia");

        Substitutions civPSubs = new Substitutions();
        Node pam = Network.createNode("pam", "individualnode");
        civPSubs.add(C, pam);
        Report report10 = new Report(civPSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report10.setReportType(ChannelType.AntRule);
        report10.setContextName("Dystopia");

        Substitutions civSubs = new Substitutions();
        Node anne = Network.createNode("anne", "individualnode");
        civSubs.add(C, anne);
        Report report1 = new Report(civSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report1.setReportType(ChannelType.AntRule);
        report1.setContextName("Dystopia");

        Substitutions coSubs = new Substitutions();
        Node england = Network.createNode("england", "individualnode");
        coSubs.add(Co, england);
        Report report2 = new Report(coSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M2);
        report2.setReportType(ChannelType.AntRule);
        report2.setContextName("Dystopia");

        Substitutions coNSubs = new Substitutions();
        Node neverland = Network.createNode("neverland", "individualnode");
        coNSubs.add(Co, neverland);
        Report report20 = new Report(coNSubs, new Support(-1), 0, false, InferenceType.BACKWARD, P0, M2);
        report20.setReportType(ChannelType.AntRule);
        report20.setContextName("Dystopia");

        Substitutions rSubs = new Substitutions();
        rSubs.add(Co, england);
        rSubs.add(G, henry);
        Report report3 = new Report(rSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M3);
        report3.setReportType(ChannelType.AntRule);
        report3.setContextName("Dystopia");


        Substitutions lSubs = new Substitutions();
        lSubs.add(Co, england);
        lSubs.add(C, anne);
        Report report4 = new Report(lSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M4);
        report4.setReportType(ChannelType.AntRule);
        report4.setContextName("Dystopia");


        network.printNodes();
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report01);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report00);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report0);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report10);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report1);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report2);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report20);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report3);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report4);
        assertEquals(1, Scheduler.getHighQueue().size());

//        System.out.println(G);
//        System.out.println(C);
//        System.out.println(Co);
//        System.out.println(voldemort);
//        System.out.println(daenerys);
//        System.out.println(henry);
//        System.out.println(pam);
//        System.out.println(anne);
//        System.out.println(england);
//        System.out.println(neverland);
//        System.out.println(government);
//        System.out.println(civilian);
//        System.out.println(country);
//        System.out.println(M0);
//        System.out.println(M1);
//        System.out.println(M2);
//        System.out.println(M3);
//        System.out.println(M4);
//        System.out.println(M5);
//        System.out.println(P0);


    }

    @Test
    void applyRuleHandler_OrEntail() throws NoSuchTypeException {
        ContextController.setCurrContext("Mythology");
        Node Patroclus = Network.createNode("Patroclus", "propositionnode");
        Node Achilles = Network.createNode("Achilles", "propositionnode");
        Node Hector = Network.createNode("Hector", "propositionnode");
        Node Alive = Network.createNode("Alive", "propositionnode");

        DownCable pMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Patroclus));
        DownCable pClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(pMember, pClass));

        DownCable hMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Hector));
        DownCable hClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(hMember, hClass));

        DownCable aMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Achilles));
        DownCable aClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(aMember, aClass));

        Node P0 = Network.createNode("orentailment", new DownCableSet(new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1)), new DownCable(Network.getRelations().get("cq"), new NodeSet(M2))));
        Report report0 = new Report(new Substitutions(), new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report0.setReportType(ChannelType.AntRule);
        report0.setContextName("Mythology");

        Report report1 = new Report(new Substitutions(), new Support(-1), 0, false, InferenceType.BACKWARD, P0, M1);
        report1.setReportType(ChannelType.AntRule);
        report1.setContextName("Mythology");

        network.printNodes();
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report0);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report1);
        assertEquals(1, Scheduler.getHighQueue().size());

//        System.out.println(Patroclus);
//        System.out.println(Hector);
//        System.out.println(Achilles);
//        System.out.println(Alive);
//        System.out.println(M0);
//        System.out.println(M1);
//        System.out.println(M2);
//        System.out.println(P0);



    }

    @Test
    void applyRuleHandler_NumEntail() throws NoSuchTypeException {
        ContextController.setCurrContext("Mythology");
        Node one = Network.createNode("1", "propositionnode");

        Node Patroclus = Network.createNode("Patroclus", "propositionnode");
        Node Achilles = Network.createNode("Achilles", "propositionnode");
        Node Hector = Network.createNode("Hector", "propositionnode");
        Node Alive = Network.createNode("Alive", "propositionnode");

        DownCable pMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Patroclus));
        DownCable pClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(pMember, pClass));

        DownCable hMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Hector));
        DownCable hClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(hMember, hClass));

        DownCable aMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Achilles));
        DownCable aClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(aMember, aClass));

        Node P0 = Network.createNode("numentailment", new DownCableSet(new DownCable(Network.getRelations().get("i"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M2))));
        Report testReport = new Report(new Substitutions(), new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        testReport.setContextName("Mythology");
        ((RuleNode) P0).applyRuleHandler(testReport);
        assertEquals(1, Scheduler.getHighQueue().size());
    }

    @Test
    void applyRuleHandler_NumEntailConVar() throws NoSuchTypeException {
        ContextController.setCurrContext("Mythology");
        Node three = Network.createNode("3", "propositionnode");
        Node X = Network.createVariableNode("X", "propositionnode");
        Node Merlin = Network.createNode("Merlin", "individualnode");

        Node Magic = Network.createNode("Magic", "individualnode");
        Node Around = Network.createNode("Around", "individualnode");
        DownCable mMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Magic));
        DownCable mClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Around));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(mMember, mClass));

        Node Flying = Network.createNode("Flying", "individualnode");
        DownCable fMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable fClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Flying));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(fMember, fClass));

        Node spellcaster = Network.createNode("spellcaster", "individualnode");
        DownCable hMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable hClass = new DownCable(Network.getRelations().get("class"), new NodeSet(spellcaster));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(hMember, hClass));

        Node fortuneTeller = Network.createNode("fortuneTeller", "individualnode");
        DownCable gMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable gClass = new DownCable(Network.getRelations().get("class"), new NodeSet(fortuneTeller));
        Node M3 = Network.createNode("propositionnode", new DownCableSet(gMember, gClass));

        Node magician = Network.createNode("magician", "individualnode");
        DownCable magMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable magClass = new DownCable(Network.getRelations().get("class"), new NodeSet(magician));
        Node M4 = Network.createNode("propositionnode", new DownCableSet(magMember, magClass));

        Node P0 = Network.createNode("numentailment", new DownCableSet(new DownCable(Network.getRelations().get("i"), new NodeSet(three)),
                new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1, M2, M3)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M4))));

        Report report0 = new Report(new Substitutions(), new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report0.setReportType(ChannelType.AntRule);
        report0.setContextName("Mythology");

        Substitutions mSubs = new Substitutions();
        mSubs.add(X, Merlin);
        Report report1 = new Report(mSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report1.setReportType(ChannelType.AntRule);
        report1.setContextName("Mythology");

        Substitutions hSubs = new Substitutions();
        hSubs.add(X, Merlin);
        Report report2 = new Report(hSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M2);
        report2.setReportType(ChannelType.AntRule);
        report2.setContextName("Mythology");

        System.out.println("----------------------------------------------------------");
        network.printNodes();
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report0);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report1);
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(report2);
        assertEquals(1, Scheduler.getHighQueue().size());

        /*System.out.println(X);
        System.out.println(three);
        System.out.println(Merlin);
        System.out.println(Magic);
        System.out.println(Around);
        System.out.println(Flying);
        System.out.println(spellcaster);
        System.out.println(fortuneTeller);
        System.out.println(M0);
        System.out.println(M1);
        System.out.println(M2);
        System.out.println(M3);
        System.out.println(M4);
        System.out.println(P0);*/

    }

    @Test
    void applyRuleHandler_Andor() throws NoSuchTypeException {
        ContextController.setCurrContext("PetStore");

        Node A = Network.createVariableNode("X", "propositionnode");
        Node one = Network.createNode("1", "propositionnode");
        Node Fish = Network.createNode("Fish", "propositionnode");
        Node Cat = Network.createNode("Cat", "propositionnode");
        Node Dog = Network.createNode("Dog", "propositionnode");
        DownCable fMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable cMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable dMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable fClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Fish));
        DownCable cClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Cat));
        DownCable dClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Dog));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(fMember, fClass));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMember, cClass));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(dMember, dClass));
        Node P0 = Network.createNode("AndOr", new DownCableSet(new DownCable(Network.getRelations().get("min"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("max"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("arg"), new NodeSet(M0, M1, M2))));

        Substitutions subs = new Substitutions();
        subs.add(A, Network.createNode("Nemo", "propositionnode"));
        Report testReport = new Report(subs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        testReport.setReportType(ChannelType.AntRule);
        testReport.setContextName("PetStore");

        System.out.println("----------------------------------------------------------");
        network.printNodes();
        System.out.println("----------------------------------------------------------");
        ((RuleNode)P0).applyRuleHandler(testReport);
        assertEquals(2, Scheduler.getHighQueue().size());

        /*System.out.println(A);
        System.out.println(one);
        System.out.println(Fish);
        System.out.println(Cat);
        System.out.println(Dog);
        System.out.println(M0);
        System.out.println(M1);
        System.out.println(M2);
        System.out.println(P0);*/
    }

    @Test
    void applyRuleHandler_Thresh() throws NoSuchTypeException {
        ContextController.setCurrContext("Mythology");
        Node X = Network.createVariableNode("X", "propositionnode");
        Node one = Network.createNode("1", "individualnode");
        Node two = Network.createNode("2", "individualnode");
        Node idealistic = Network.createNode("Idealistic", "individualnode");
        Node moral = Network.createNode("Moral", "individualnode");
        Node brave = Network.createNode("Brave", "individualnode");

        DownCable iMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable mMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable bMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable iClass = new DownCable(Network.getRelations().get("class"), new NodeSet(idealistic));
        DownCable mClass = new DownCable(Network.getRelations().get("class"), new NodeSet(moral));
        DownCable bClass = new DownCable(Network.getRelations().get("class"), new NodeSet(brave));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(iMember, iClass));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(mMember, mClass));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(bMember, bClass));

        Node P0 = Network.createNode("thresh", new DownCableSet(new DownCable(Network.getRelations().get("thresh"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("threshmax"), new NodeSet(two)),
                new DownCable(Network.getRelations().get("arg"), new NodeSet(M0, M1, M2))));

        Substitutions subs = new Substitutions();
        subs.add(X, Network.createNode("Patroclus", "individualnode"));
        Report testReport = new Report(subs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        testReport.setReportType(ChannelType.AntRule);
        testReport.setContextName("Mythology");

        System.out.println("----------------------------------------------------------");
        network.printNodes();
        System.out.println("----------------------------------------------------------");
        ((RuleNode) P0).applyRuleHandler(testReport);
        assertEquals(2, Scheduler.getHighQueue().size());

        /*System.out.println(X);
        System.out.println(one);
        System.out.println(two);
        System.out.println(idealistic);
        System.out.println(moral);
        System.out.println(brave);
        System.out.println(M0);
        System.out.println(M1);
        System.out.println(M2);
        System.out.println(P0);*/
    }

    @Test
    void createSupport() throws NoSuchTypeException, DirectCycleException {
        Node X = Network.createVariableNode("X", "propositionnode");
        Node one = Network.createNode("1", "individualnode");
        Node two = Network.createNode("2", "individualnode");
        Node idealistic = Network.createNode("Idealistic", "individualnode");
        Node moral = Network.createNode("Moral", "individualnode");
        Node brave = Network.createNode("Brave", "individualnode");

        DownCable iMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable mMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable bMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        DownCable iClass = new DownCable(Network.getRelations().get("class"), new NodeSet(idealistic));
        DownCable mClass = new DownCable(Network.getRelations().get("class"), new NodeSet(moral));
        DownCable bClass = new DownCable(Network.getRelations().get("class"), new NodeSet(brave));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(iMember, iClass));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(mMember, mClass));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(bMember, bClass));

        Node P0 = Network.createNode("thresh", new DownCableSet(new DownCable(Network.getRelations().get("thresh"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("threshmax"), new NodeSet(two)),
                new DownCable(Network.getRelations().get("arg"), new NodeSet(M0, M1, M2))));
        Substitutions rSubs = new Substitutions();
        rSubs.add(X, Network.createNode("Patroclus", "individualnode"));
        FlagNodeSet fns = new FlagNodeSet();
        fns.addFlagNode(new FlagNode(M0, true, new Support(M0.getId())));
        RuleInfo ri = new RuleInfo("Mythology", 0, 1, 0, rSubs, fns, new Support(-1));
        Support sup = ((RuleNode)P0).createSupport(ri);
        System.out.println(sup);
        assertNotNull(sup);
    }
}