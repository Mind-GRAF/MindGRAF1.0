package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstanceSet;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumEntailmentTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException {
        Network network = new Network();
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

        Node P0 = Network.createNode("NumEntailment", new DownCableSet(new DownCable(Network.getRelations().get("i"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("&ant"), new NodeSet(M0, M1)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M2))));

        FlagNodeSet flags = new FlagNodeSet(new FlagNode(M0, true, new PropositionNodeSet()));
        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(new RuleInfo(1, 0, new Substitutions(), flags));
        ((RuleNode)P0).setRootRuleInfos(inserted);

        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
        assertEquals(1, inferrable[0].size());
    }

    @Test
    void processIntroductionRequestShouldTrue() throws NoSuchTypeException {
        //Testing Begin Algo.
        Scheduler S = new Scheduler();
        S.initiate();
        System.out.println("Scheduler low Q: "+S.getLowQueue());

        Network N = new Network();

        // Create a new RuleNode
        Node A = new PropositionNode("A", false);
        Node B = new PropositionNode("B", false);
        Node antecedent1 = new PropositionNode("Antecedent 1", false);
        Node antecedent2 = new PropositionNode("Antecedent 2", false);
        Node antecedent3 = new PropositionNode("Antecedent 3", false);
        Node varant = new PropositionNode("VarAnt", true);
        Node X = new PropositionNode("X", true);
        Node consequent = new PropositionNode("Consequent", false);
        Node two = new PropositionNode("2", false);

        // Add antecedents and consequent to the RuleNode
        Relation ant = new Relation("ants", "", Adjustability.NONE, 4);
        Relation consequentRelation = new Relation("cqs", "", Adjustability.EXPAND, 1);
        Relation i = new Relation("i", "", Adjustability.NONE,1);
        Relation quantifier = Network.createRelation("forall", "propositionnode",
                Adjustability.EXPAND, 2);

        NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant);
        // NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant, X);//With Free Varible no Sub "X"

        DownCable d1 = new DownCable(ant, ants);
        DownCable d2 = new DownCable(consequentRelation, new NodeSet(consequent));
        DownCable d3 = new DownCable(i, new NodeSet(two));
        DownCable d4 = new DownCable(quantifier, ants);


        NumEntailment ruleNode = new NumEntailment(new DownCableSet(d1, d2, d3, d4));

        Set<String,Integer> attitudes = new Set<String,Integer>();
        attitudes.add("Belief",1);
        attitudes.add("Fear",2);
        attitudes.add("Desire",3);

        Context currContext = new Context("Original Context", attitudes);

        //Makes node an open Formula
        NodeSet fetchedFreeVar = ruleNode.fetchFreeVariables();
        fetchedFreeVar.add(varant);//Have Sub in Filter;

        Substitutions sub = new Substitutions();
        sub.add(varant, A);
        Request IntroRequest = new Request(new Channel(new Substitutions(), sub, currContext.getName(), 1, ruleNode), ruleNode);


        assertTrue(ruleNode.processIntroductionRequest(IntroRequest));
//            ruleNode.processReports();
            // System.out.println("Processing Report in "+ rep.getName());
            System.out.println("\n Scheduler after reqs: " + S.getLowQueue());
            System.out.println("Scheduler report Q: " + S.getHighQueue());

    }

    @Test
    void processIntroductionRequestShouldFalse() throws NoSuchTypeException {
        //Testing Begin Algo.
        Scheduler S = new Scheduler();
        S.initiate();
        System.out.println("Scheduler low Q: "+S.getLowQueue());

        Network N = new Network();

        // Create a new RuleNode
        Node A = new PropositionNode("A", false);
        Node B = new PropositionNode("B", false);
        Node antecedent1 = new PropositionNode("Antecedent 1", false);
        Node antecedent2 = new PropositionNode("Antecedent 2", false);
        Node antecedent3 = new PropositionNode("Antecedent 3", false);
        Node varant = new PropositionNode("VarAnt", true);
        Node X = new PropositionNode("X", true);
        Node consequent = new PropositionNode("Consequent", false);
        Node two = new PropositionNode("2", false);

        // Add antecedents and consequent to the RuleNode
        Relation ant = new Relation("ants", "", Adjustability.NONE, 4);
        Relation consequentRelation = new Relation("cqs", "", Adjustability.EXPAND, 1);
        Relation i = new Relation("i", "", Adjustability.NONE,1);
        Relation quantifier = Network.createRelation("forall", "propositionnode",
                Adjustability.EXPAND, 2);

//        NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant);
        NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant, X);//With Free Varible no Sub "X"

        DownCable d1 = new DownCable(ant, ants);
        DownCable d2 = new DownCable(consequentRelation, new NodeSet(consequent));
        DownCable d3 = new DownCable(i, new NodeSet(two));
        DownCable d4 = new DownCable(quantifier, ants);


        NumEntailment ruleNode = new NumEntailment(new DownCableSet(d1, d2, d3, d4));

        Context currContext = new Context("Original Context", 1, new NodeSet(ruleNode));

        //Makes node an open Formula
        NodeSet fetchedFreeVar = ruleNode.fetchFreeVariables();
        fetchedFreeVar.add(varant);//Have Sub in Filter;
        fetchedFreeVar.add(X);

        Substitutions sub = new Substitutions();
        sub.add(varant, A);
        Request currentRequest = new Request(new Channel(new Substitutions(), sub, currContext.getName(), 1, ruleNode), ruleNode);


        assertFalse(ruleNode.processIntroductionRequest(currentRequest));
//            ruleNode.processReports();
            // System.out.println("Processing Report in "+ rep.getName());
            System.out.println("\n Scheduler after reqs: " + S.getLowQueue());
            System.out.println("Scheduler report Q: " + S.getHighQueue());

    }
}