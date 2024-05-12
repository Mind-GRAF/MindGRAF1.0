package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleNodeTest {

    @BeforeEach
    void setUp() {
        Scheduler.initiate();
        Network network = new Network();
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
        report01.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report01);

        Substitutions govDSubs = new Substitutions();
        Node daenerys = Network.createNode("daenerys", "individualnode");
        govDSubs.add(G, daenerys);
        Report report00 = new Report(govDSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report00.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report00);

        Substitutions govSubs = new Substitutions();
        Node henry = Network.createNode("henry", "individualnode");
        govSubs.add(G, henry);
        Report report0 = new Report(govSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M0);
        report0.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report0);

        Substitutions civPSubs = new Substitutions();
        Node pam = Network.createNode("pam", "individualnode");
        civPSubs.add(C, pam);
        Report report10 = new Report(civPSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report10.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report10);

        Substitutions civSubs = new Substitutions();
        Node anne = Network.createNode("anne", "individualnode");
        civSubs.add(C, anne);
        Report report1 = new Report(civSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report1.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report1);

        Substitutions coSubs = new Substitutions();
        Node england = Network.createNode("england", "individualnode");
        coSubs.add(Co, england);
        Report report2 = new Report(coSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M2);
        report2.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report2);

        Substitutions coNSubs = new Substitutions();
        Node neverland = Network.createNode("neverland", "individualnode");
        coNSubs.add(Co, neverland);
        Report report20 = new Report(coNSubs, new Support(-1), 0, false, InferenceType.BACKWARD, P0, M2);
        report20.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report20);

        Substitutions rSubs = new Substitutions();
        rSubs.add(Co, england);
        rSubs.add(G, henry);
        Report report3 = new Report(rSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M3);
        report3.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report3);

        Substitutions lSubs = new Substitutions();
        lSubs.add(Co, england);
        lSubs.add(C, anne);
        Report report4 = new Report(lSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M4);
        report4.setContextName("Dystopia");
        ((RuleNode)P0).applyRuleHandler(report4);

        assertEquals(1, Scheduler.getHighQueue().size());

    }

    @Test
    void applyRuleHandler_OrEntail() throws NoSuchTypeException {
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
        report0.setContextName("Mythology");
        ((RuleNode)P0).applyRuleHandler(report0);
        assertEquals(1, Scheduler.getHighQueue().size());

        Report report1 = new Report(new Substitutions(), new Support(-1), 0, false, InferenceType.BACKWARD, P0, M1);
        report1.setContextName("Mythology");
        ((RuleNode)P0).applyRuleHandler(report1);

    }

    @Test
    void applyRuleHandler_NumEntail() throws NoSuchTypeException {
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
        testReport.setContextName("");
        ((RuleNode)P0).applyRuleHandler(testReport);
        assertEquals(1, Scheduler.getHighQueue().size());
    }

    @Test
    void applyRuleHandler_NumEntailConVar() throws NoSuchTypeException {
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
        report0.setContextName("Mythology");
        ((RuleNode)P0).applyRuleHandler(report0);

        Substitutions mSubs = new Substitutions();
        mSubs.add(X, Merlin);
        Report report1 = new Report(mSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M1);
        report1.setContextName("Mythology");
        ((RuleNode)P0).applyRuleHandler(report1);

        Substitutions hSubs = new Substitutions();
        hSubs.add(X, Merlin);
        Report report2 = new Report(hSubs, new Support(-1), 0, true, InferenceType.BACKWARD, P0, M2);
        report2.setContextName("Mythology");
        ((RuleNode)P0).applyRuleHandler(report2);

        assertEquals(1, Scheduler.getHighQueue().size());

    }

    @Test
    void applyRuleHandler_Andor() throws NoSuchTypeException {
        Node A = Network.createVariableNode("A", "propositionnode");
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
        testReport.setContextName("");
        ((RuleNode)P0).applyRuleHandler(testReport);
        assertEquals(2, Scheduler.getHighQueue().size());
    }

    @Test
    void applyRuleHandler_Thresh() throws NoSuchTypeException {
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
        testReport.setContextName("");
        ((RuleNode)P0).applyRuleHandler(testReport);
        assertEquals(2, Scheduler.getHighQueue().size());
    }
}