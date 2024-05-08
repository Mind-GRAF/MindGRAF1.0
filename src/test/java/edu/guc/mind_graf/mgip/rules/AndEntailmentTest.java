package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.IntroductionChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.*;
import org.junit.jupiter.api.Test;

import static edu.guc.mind_graf.network.Network.*;
import static org.junit.jupiter.api.Assertions.*;

class AndEntailmentTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException {
        Network network = new Network();
        Node G = Network.createVariableNode("G", "propositionnode");
        Node C = Network.createVariableNode("C", "propositionnode");
        Node Co = Network.createVariableNode("Co", "propositionnode");

        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(G));
        Node government = createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        Node M0 = createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(C));
        Node civilian = createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        Node M1 = createNode("propositionnode", new DownCableSet(cMem, civ));

        DownCable coMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Co));
        Node country = createNode("country", "propositionnode");
        DownCable coun = new DownCable(Network.getRelations().get("class"), new NodeSet(country));
        Node M2 = createNode("propositionnode", new DownCableSet(coMem, coun));

        Relation rule = Network.createRelation("rule", "",
                Adjustability.EXPAND, 2);
        Relation ruled = Network.createRelation("ruled", "",
                Adjustability.EXPAND, 2);
        DownCable ruling = new DownCable(rule, new NodeSet(G));
        DownCable ruledC = new DownCable(ruled, new NodeSet(Co));
        Node M3 = createNode("propositionnode", new DownCableSet(ruling, ruledC));

        Relation living = Network.createRelation("living", "",
                Adjustability.EXPAND, 2);
        Relation in = Network.createRelation("in", "",
                Adjustability.EXPAND, 2);
        DownCable livingC = new DownCable(living, new NodeSet(C));
        DownCable inC = new DownCable(in, new NodeSet(Co));
        Node M4 = createNode("propositionnode", new DownCableSet(livingC, inC));

        Relation brainwash = Network.createRelation("brainwash", "propositionnode",
                Adjustability.EXPAND, 2);
        Relation brainwashed = Network.createRelation("brainwashed", "propositionnode",
                Adjustability.EXPAND, 2);
        DownCable brainwashG = new DownCable(brainwash, new NodeSet(G));
        DownCable brainwashedC = new DownCable(brainwashed, new NodeSet(C));
        Node M5 = createNode("propositionnode", new DownCableSet(brainwashG, brainwashedC));

        Node P0 = createNode("AndEntailment", new DownCableSet(new DownCable(Network.getRelations().get("&ant"), new NodeSet(M0, M1, M2, M3, M4)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M5))));

        FlagNode govFlag = new FlagNode(M0, true, new PropositionNodeSet());
        Substitutions govSubs = new Substitutions();
        Node henry = createNode("henry", "individualnode");
        govSubs.add(G, henry);
        RuleInfo ruleInfo0 = new RuleInfo(1, 0, govSubs, new FlagNodeSet(govFlag));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo0);

        FlagNode civFlag = new FlagNode(M1, true, new PropositionNodeSet());
        Substitutions civSubs = new Substitutions();
        Node anne = createNode("anne", "individualnode");
        civSubs.add(C, anne);
        RuleInfo ruleInfo1 = new RuleInfo(1, 0, civSubs, new FlagNodeSet(civFlag));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo1);

        FlagNode coFlag = new FlagNode(M2, true, new PropositionNodeSet());
        Substitutions coSubs = new Substitutions();
        Node england = createNode("england", "individualnode");
        coSubs.add(Co, england);
        RuleInfo ruleInfo2 = new RuleInfo(1, 0, coSubs, new FlagNodeSet(coFlag));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo2);

        FlagNode rFlag = new FlagNode(M3, true, new PropositionNodeSet());
        Substitutions rSubs = new Substitutions();
        rSubs.add(Co, england);
        rSubs.add(G, henry);
        RuleInfo ruleInfo3 = new RuleInfo(1, 0, rSubs, new FlagNodeSet(rFlag));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo3);

        FlagNode lFlag = new FlagNode(M4, true, new PropositionNodeSet());
        Substitutions lSubs = new Substitutions();
        lSubs.add(Co, england);
        lSubs.add(C, anne);
        RuleInfo ruleInfo4 = new RuleInfo(1, 0, lSubs, new FlagNodeSet(lFlag));
        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo4);
        ((RuleNode)P0).setRootRuleInfos(inserted);

        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
        assertEquals(1, inferrable[0].size());
    }

    @Test
    void processIntroReqAndEntail() throws NoSuchTypeException {
        Scheduler S = new Scheduler();
        S.initiate();
        Set<String, Integer> attNames = new Set<String, Integer>();
        attNames.add("Belif", 1);
        Context currContext = new Context("OG Context",attNames);
        ContextSet contextSet = new ContextSet();
        contextSet.add("OG Context", currContext);


        Network N = new Network();
        // Create a new RuleNode
        Node uni = createNode("Uni", "propositionnode");
        Node prof = createNode("Prof", "propositionnode");
        Node academic = createNode("Academic", "propositionnode");
        Node phd = createNode("PHD", "propositionnode");
        Node researcher = createNode("Researcher", "propositionnode");
        Node X = createVariableNode("X", "propositionnode");

        Relation member = createRelation("Member", "", Adjustability.NONE, 1);
        Relation classs = createRelation("Class", "", Adjustability.NONE, 1);
        Relation works = createRelation("Works","",Adjustability.NONE,1);
        Relation at = createRelation("At","",Adjustability.NONE,1);

        DownCable memberDC = new DownCable(member, new NodeSet(X));
        DownCable p1ClassDC = new DownCable(classs, new NodeSet(phd));
        DownCable p3ClassDC = new DownCable(classs, new NodeSet(researcher));
        DownCable p4ClassDC = new DownCable(classs, new NodeSet(prof));
        DownCable p5ClassDC = new DownCable(classs, new NodeSet(academic));
        DownCable p2WorksDC= new DownCable(works, new NodeSet(X));
        DownCable p2AtDC= new DownCable(at, new NodeSet(uni));


        Node p1 = createNode("propositionnode", new DownCableSet(p1ClassDC, memberDC));
        p1.setName("P1");
        Node p2 = createNode("propositionnode", new DownCableSet(p2WorksDC, p2AtDC));
        p2.setName("P2");
        Node p3 = createNode("propositionnode", new DownCableSet(p3ClassDC, memberDC));
        p3.setName("P3");
        Node p4 = createNode("propositionnode", new DownCableSet(p4ClassDC, memberDC));
        p4.setName("P4");
        Node p5 = createNode("propositionnode", new DownCableSet(p5ClassDC, memberDC));
        p5.setName("P5");

        Relation ants = getRelations().get("ants");
        Relation cqs = getRelations().get("cqs");
        Relation forall = getRelations().get("forall");

        DownCable m1AntsDC = new DownCable(ants,new NodeSet(p1, p2));
        DownCable m1CqsDC = new DownCable(cqs,new NodeSet(p4, p3));

        DownCable m2AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m2CqsDC = new DownCable(cqs,new NodeSet(p3));


        DownCable m3AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m3CqsDC = new DownCable(cqs,new NodeSet(p5));


        DownCable m4AntsDC = new DownCable(ants,new NodeSet(p2, p5));
        DownCable m4CqsDC = new DownCable(cqs,new NodeSet(p4));

        DownCable forAllDC = new DownCable(forall, new NodeSet(X));

        AndEntailment m1 = new AndEntailment(new DownCableSet(m1AntsDC, m1CqsDC, forAllDC));
        m1.setName("M1");
        AndEntailment m2 = new AndEntailment(new DownCableSet(m2AntsDC, m2CqsDC, forAllDC));
        m2.setName("M2");
        AndEntailment m3 = new AndEntailment(new DownCableSet(m3AntsDC, m3CqsDC, forAllDC));
        m3.setName("M3");
        AndEntailment m4 = new AndEntailment(new DownCableSet(m4AntsDC, m4CqsDC, forAllDC));
        m4.setName("M4");

        NodeSet freeVars = m1.fetchFreeVariables();
        System.out.println("M1 Free Vars are : " + freeVars);

    //Query PhD(John) & WorksAtUni(John) => Researcher(John) & Prof(John)
    // Building PhD(John) & WorksAtUni(John)
        Node john = createNode("John", "propositionnode");

        DownCable m5Member = new DownCable(member, new NodeSet(john));
        DownCable m5Calss = new DownCable(classs, new NodeSet(phd));

        DownCable m6Works = new DownCable(works, new NodeSet(john));
        DownCable m6At = new DownCable(at, new NodeSet(uni));

        Node m5 = createNode("propositionnode", new DownCableSet(m5Calss, m5Member));
        Node m6 = createNode("propositionnode", new DownCableSet(m6Works, m6At));
        Substitutions filterSubs = new Substitutions();
        Substitutions switchSubs = new Substitutions();
        filterSubs.add(X,john);

        // Building Query Node M0
        DownCable m7Member = new DownCable(member, new NodeSet(john));
        DownCable m7Calss = new DownCable(classs, new NodeSet(researcher));
        DownCable m8Member = new DownCable(member, new NodeSet(john));
        DownCable m8Calss = new DownCable(classs, new NodeSet(prof));
        Node m7 = createNode("propositionnode", new DownCableSet(m7Calss, m7Member));
        Node m8 = createNode("propositionnode", new DownCableSet(m8Calss, m8Member));
        DownCable m0AntDC = new DownCable(ants, new NodeSet(m5,m6));
        DownCable m0CqDC = new DownCable(cqs, new NodeSet(m7,m8));
        Node m0 = createNode("AndEntailment", new DownCableSet(m0AntDC, m0CqDC));
        m0.setName("M0");

        Request introReq = new Request(new IntroductionChannel(switchSubs,filterSubs, currContext.getName(), 1, m0), m1);

        assertTrue(m1.processIntroductionRequest(introReq));
    }

    @Test
    void processIntroReqAndEntailWithSubFreeVar() throws NoSuchTypeException {
        Scheduler S = new Scheduler();
        S.initiate();
        Set<String, Integer> attNames = new Set<String, Integer>();
        attNames.add("Belif", 1);
        Context currContext = new Context("OG Context",attNames);
        ContextSet contextSet = new ContextSet();
        contextSet.add("OG Context", currContext);


        Network N = new Network();
        // Create a new RuleNode
        Node uni = createNode("Uni", "propositionnode");
        Node prof = createNode("Prof", "propositionnode");
        Node academic = createNode("Academic", "propositionnode");
        Node phd = createNode("PHD", "propositionnode");
        Node researcher = createNode("Researcher", "propositionnode");
        Node X = createVariableNode("X", "propositionnode");

        Relation member = createRelation("Member", "", Adjustability.NONE, 1);
        Relation classs = createRelation("Class", "", Adjustability.NONE, 1);
        Relation works = createRelation("Works","",Adjustability.NONE,1);
        Relation at = createRelation("At","",Adjustability.NONE,1);

        DownCable memberDC = new DownCable(member, new NodeSet(X));
        DownCable p1ClassDC = new DownCable(classs, new NodeSet(phd));
        DownCable p3ClassDC = new DownCable(classs, new NodeSet(researcher));
        DownCable p4ClassDC = new DownCable(classs, new NodeSet(prof));
        DownCable p5ClassDC = new DownCable(classs, new NodeSet(academic));
        DownCable p2WorksDC= new DownCable(works, new NodeSet(X));
        DownCable p2AtDC= new DownCable(at, new NodeSet(uni));


        Node p1 = createNode("propositionnode", new DownCableSet(p1ClassDC, memberDC));
        p1.setName("P1");
        Node p2 = createNode("propositionnode", new DownCableSet(p2WorksDC, p2AtDC));
        p2.setName("P2");
        Node p3 = createNode("propositionnode", new DownCableSet(p3ClassDC, memberDC));
        p3.setName("P3");
        Node p4 = createNode("propositionnode", new DownCableSet(p4ClassDC, memberDC));
        p4.setName("P4");
        Node p5 = createNode("propositionnode", new DownCableSet(p5ClassDC, memberDC));
        p5.setName("P5");

        Relation ants = getRelations().get("ants");
        Relation cqs = getRelations().get("cqs");
        Relation forall = getRelations().get("forall");

        DownCable m1AntsDC = new DownCable(ants,new NodeSet(p1, p2));
        DownCable m1CqsDC = new DownCable(cqs,new NodeSet(p4, p3));

        DownCable m2AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m2CqsDC = new DownCable(cqs,new NodeSet(p3));


        DownCable m3AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m3CqsDC = new DownCable(cqs,new NodeSet(p5));


        DownCable m4AntsDC = new DownCable(ants,new NodeSet(p2, p5));
        DownCable m4CqsDC = new DownCable(cqs,new NodeSet(p4));


        AndEntailment m1 = new AndEntailment(new DownCableSet(m1AntsDC, m1CqsDC));
        m1.setName("M1");
        AndEntailment m2 = new AndEntailment(new DownCableSet(m2AntsDC, m2CqsDC));
        m2.setName("M2");
        AndEntailment m3 = new AndEntailment(new DownCableSet(m3AntsDC, m3CqsDC));
        m3.setName("M3");
        AndEntailment m4 = new AndEntailment(new DownCableSet(m4AntsDC, m4CqsDC));
        m4.setName("M4");

    //Query PhD(John) & WorksAtUni(John) => Researcher(John) & Prof(John)
    // Building PhD(John) & WorksAtUni(John)
        Node john = createNode("John", "propositionnode");

        DownCable m5Member = new DownCable(member, new NodeSet(john));
        DownCable m5Calss = new DownCable(classs, new NodeSet(phd));

        DownCable m6Works = new DownCable(works, new NodeSet(john));
        DownCable m6At = new DownCable(at, new NodeSet(uni));

        Node m5 = createNode("propositionnode", new DownCableSet(m5Calss, m5Member));
        Node m6 = createNode("propositionnode", new DownCableSet(m6Works, m6At));
        Substitutions filterSubs = new Substitutions();
        Substitutions switchSubs = new Substitutions();
        filterSubs.add(X,john);

        // Building Query Node M0
        DownCable m7Member = new DownCable(member, new NodeSet(john));
        DownCable m7Calss = new DownCable(classs, new NodeSet(researcher));
        DownCable m8Member = new DownCable(member, new NodeSet(john));
        DownCable m8Calss = new DownCable(classs, new NodeSet(prof));
        Node m7 = createNode("propositionnode", new DownCableSet(m7Calss, m7Member));
        Node m8 = createNode("propositionnode", new DownCableSet(m8Calss, m8Member));
        DownCable m0AntDC = new DownCable(ants, new NodeSet(m5,m6));
        DownCable m0CqDC = new DownCable(cqs, new NodeSet(m7,m8));
        Node m0 = createNode("AndEntailment", new DownCableSet(m0AntDC, m0CqDC));
        m0.setName("M0");

        Request introReq = new Request(new IntroductionChannel(switchSubs,filterSubs, currContext.getName(), 1, m0), m1);

        assertTrue(m1.processIntroductionRequest(introReq));
    }

    @Test
    void processIntroRepAndEntail() throws NoSuchTypeException {
        Scheduler S = new Scheduler();
        S.initiate();
        Set<String, Integer> attNames = new Set<String, Integer>();
        attNames.add("Belif", 1);
        Context currContext = new Context("OG Context",attNames);
        ContextSet contextSet = new ContextSet();
        contextSet.add("OG Context", currContext);


        Network N = new Network();
        // Create a new RuleNode
        Node uni = createNode("Uni", "propositionnode");
        Node prof = createNode("Prof", "propositionnode");
        Node academic = createNode("Academic", "propositionnode");
        Node phd = createNode("PHD", "propositionnode");
        Node researcher = createNode("Researcher", "propositionnode");
        Node X = createVariableNode("X", "propositionnode");

        Relation member = createRelation("Member", "", Adjustability.NONE, 1);
        Relation classs = createRelation("Class", "", Adjustability.NONE, 1);
        Relation works = createRelation("Works","",Adjustability.NONE,1);
        Relation at = createRelation("At","",Adjustability.NONE,1);

        DownCable memberDC = new DownCable(member, new NodeSet(X));
        DownCable p1ClassDC = new DownCable(classs, new NodeSet(phd));
        DownCable p3ClassDC = new DownCable(classs, new NodeSet(researcher));
        DownCable p4ClassDC = new DownCable(classs, new NodeSet(prof));
        DownCable p5ClassDC = new DownCable(classs, new NodeSet(academic));
        DownCable p2WorksDC= new DownCable(works, new NodeSet(X));
        DownCable p2AtDC= new DownCable(at, new NodeSet(uni));


        Node p1 = createNode("propositionnode", new DownCableSet(p1ClassDC, memberDC));
        p1.setName("P1");
        Node p2 = createNode("propositionnode", new DownCableSet(p2WorksDC, p2AtDC));
        p2.setName("P2");
        Node p3 = createNode("propositionnode", new DownCableSet(p3ClassDC, memberDC));
        p3.setName("P3");
        Node p4 = createNode("propositionnode", new DownCableSet(p4ClassDC, memberDC));
        p4.setName("P4");
        Node p5 = createNode("propositionnode", new DownCableSet(p5ClassDC, memberDC));
        p5.setName("P5");

        Relation ants = getRelations().get("ants");
        Relation cqs = getRelations().get("cqs");
        Relation forall = getRelations().get("forall");

        DownCable m1AntsDC = new DownCable(ants,new NodeSet(p1, p2));
        DownCable m1CqsDC = new DownCable(cqs,new NodeSet(p4, p3));

        DownCable m2AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m2CqsDC = new DownCable(cqs,new NodeSet(p3));


        DownCable m3AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m3CqsDC = new DownCable(cqs,new NodeSet(p5));


        DownCable m4AntsDC = new DownCable(ants,new NodeSet(p2, p5));
        DownCable m4CqsDC = new DownCable(cqs,new NodeSet(p4));


        AndEntailment m1 = new AndEntailment(new DownCableSet(m1AntsDC, m1CqsDC));
        m1.setName("M1");
        AndEntailment m2 = new AndEntailment(new DownCableSet(m2AntsDC, m2CqsDC));
        m2.setName("M2");
        AndEntailment m3 = new AndEntailment(new DownCableSet(m3AntsDC, m3CqsDC));
        m3.setName("M3");
        AndEntailment m4 = new AndEntailment(new DownCableSet(m4AntsDC, m4CqsDC));
        m4.setName("M4");

        System.out.println("M1 Free Vars " + m1.getFreeVariables());
        System.out.println("P1 Free Vars " + p1.getFreeVariables());

        //Query PhD(John) & WorksAtUni(John) => Researcher(John) & Prof(John)
        // Building PhD(John) & WorksAtUni(John)
        Node john = createNode("John", "propositionnode");

        DownCable m5Member = new DownCable(member, new NodeSet(john));
        DownCable m5Calss = new DownCable(classs, new NodeSet(phd));

        DownCable m6Works = new DownCable(works, new NodeSet(john));
        DownCable m6At = new DownCable(at, new NodeSet(uni));

        Node m5 = createNode("propositionnode", new DownCableSet(m5Calss, m5Member));
        Node m6 = createNode("propositionnode", new DownCableSet(m6Works, m6At));
        Substitutions filterSubs = new Substitutions();
        Substitutions switchSubs = new Substitutions();
        filterSubs.add(X,john);

        // Building Query Node M0
        DownCable m7Member = new DownCable(member, new NodeSet(john));
        DownCable m7Calss = new DownCable(classs, new NodeSet(researcher));
        DownCable m8Member = new DownCable(member, new NodeSet(john));
        DownCable m8Calss = new DownCable(classs, new NodeSet(prof));
        Node m7 = createNode("propositionnode", new DownCableSet(m7Calss, m7Member));
        Node m8 = createNode("propositionnode", new DownCableSet(m8Calss, m8Member));
        DownCable m0AntDC = new DownCable(ants, new NodeSet(m5,m6));
        DownCable m0CqDC = new DownCable(cqs, new NodeSet(m7,m8));
        Node m0 = createNode("AndEntailment", new DownCableSet(m0AntDC, m0CqDC));
        m0.setName("M0");

        Request introReq = new Request(new IntroductionChannel(switchSubs,filterSubs, currContext.getName(), 1, m0), m1);
        Scheduler.addToLowQueue(introReq);
        System.out.println(Scheduler.schedule());
    }

    @Test
    void processIntroRepAndEntailWithBoundVarNoSub() throws NoSuchTypeException {
        Scheduler S = new Scheduler();
        S.initiate();
        Set<String, Integer> attNames = new Set<String, Integer>();
        attNames.add("Belif", 1);
        Context currContext = new Context("OG Context",attNames);
        ContextSet contextSet = new ContextSet();
        contextSet.add("OG Context", currContext);


        Network N = new Network();
        // Create a new RuleNode
        Node uni = createNode("Uni", "propositionnode");
        Node prof = createNode("Prof", "propositionnode");
        Node academic = createNode("Academic", "propositionnode");
        Node phd = createNode("PHD", "propositionnode");
        Node researcher = createNode("Researcher", "propositionnode");
        Node X = createVariableNode("X", "propositionnode");

        Relation member = createRelation("Member", "", Adjustability.NONE, 1);
        Relation classs = createRelation("Class", "", Adjustability.NONE, 1);
        Relation works = createRelation("Works","",Adjustability.NONE,1);
        Relation at = createRelation("At","",Adjustability.NONE,1);

        DownCable memberDC = new DownCable(member, new NodeSet(X));
        DownCable p1ClassDC = new DownCable(classs, new NodeSet(phd));
        DownCable p3ClassDC = new DownCable(classs, new NodeSet(researcher));
        DownCable p4ClassDC = new DownCable(classs, new NodeSet(prof));
        DownCable p5ClassDC = new DownCable(classs, new NodeSet(academic));
        DownCable p2WorksDC= new DownCable(works, new NodeSet(X));
        DownCable p2AtDC= new DownCable(at, new NodeSet(uni));


        Node p1 = createNode("propositionnode", new DownCableSet(p1ClassDC, memberDC));
        p1.setName("P1");
        Node p2 = createNode("propositionnode", new DownCableSet(p2WorksDC, p2AtDC));
        p2.setName("P2");
        Node p3 = createNode("propositionnode", new DownCableSet(p3ClassDC, memberDC));
        p3.setName("P3");
        Node p4 = createNode("propositionnode", new DownCableSet(p4ClassDC, memberDC));
        p4.setName("P4");
        Node p5 = createNode("propositionnode", new DownCableSet(p5ClassDC, memberDC));
        p5.setName("P5");

        Relation ants = getRelations().get("ants");
        Relation cqs = getRelations().get("cqs");
        Relation forall = getRelations().get("forall");

        DownCable m1AntsDC = new DownCable(ants,new NodeSet(p1, p2));
        DownCable m1CqsDC = new DownCable(cqs,new NodeSet(p4, p3));

        DownCable m2AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m2CqsDC = new DownCable(cqs,new NodeSet(p3));


        DownCable m3AntsDC = new DownCable(ants,new NodeSet(p1));
        DownCable m3CqsDC = new DownCable(cqs,new NodeSet(p5));


        DownCable m4AntsDC = new DownCable(ants,new NodeSet(p2, p5));
        DownCable m4CqsDC = new DownCable(cqs,new NodeSet(p4));


        AndEntailment m1 = new AndEntailment(new DownCableSet(m1AntsDC, m1CqsDC));
        m1.setName("M1");
        AndEntailment m2 = new AndEntailment(new DownCableSet(m2AntsDC, m2CqsDC));
        m2.setName("M2");
        AndEntailment m3 = new AndEntailment(new DownCableSet(m3AntsDC, m3CqsDC));
        m3.setName("M3");
        AndEntailment m4 = new AndEntailment(new DownCableSet(m4AntsDC, m4CqsDC));
        m4.setName("M4");

        //Query PhD(John) & WorksAtUni(John) => Researcher(John) & Prof(John)
        // Building PhD(John) & WorksAtUni(John)
        Node john = createNode("John", "propositionnode");

        DownCable m5Member = new DownCable(member, new NodeSet(john));
        DownCable m5Calss = new DownCable(classs, new NodeSet(phd));

        DownCable m6Works = new DownCable(works, new NodeSet(john));
        DownCable m6At = new DownCable(at, new NodeSet(uni));

        Node m5 = createNode("propositionnode", new DownCableSet(m5Calss, m5Member));
        Node m6 = createNode("propositionnode", new DownCableSet(m6Works, m6At));
        Substitutions filterSubs = new Substitutions();
        Substitutions switchSubs = new Substitutions();
        filterSubs.add(X,john);

        // Building Query Node M0
        DownCable m7Member = new DownCable(member, new NodeSet(john));
        DownCable m7Calss = new DownCable(classs, new NodeSet(researcher));
        DownCable m8Member = new DownCable(member, new NodeSet(john));
        DownCable m8Calss = new DownCable(classs, new NodeSet(prof));
        Node m7 = createNode("propositionnode", new DownCableSet(m7Calss, m7Member));
        Node m8 = createNode("propositionnode", new DownCableSet(m8Calss, m8Member));
        DownCable m0AntDC = new DownCable(ants, new NodeSet(m5,m6));
        DownCable m0CqDC = new DownCable(cqs, new NodeSet(m7,m8));
        Node m0 = createNode("AndEntailment", new DownCableSet(m0AntDC, m0CqDC));
        m0.setName("M0");

        Request introReq = new Request(new IntroductionChannel(switchSubs,filterSubs, currContext.getName(), 1, m0), m1);
        Scheduler.addToLowQueue(introReq);
        System.out.println(Scheduler.schedule());
    }
}