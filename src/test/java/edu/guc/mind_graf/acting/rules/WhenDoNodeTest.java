package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.rules.AndEntailment;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.IndividualNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class WhenDoNodeTest {
    Network n = new Network();

    @BeforeEach
    void setUp() {

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext("home");
        ContextController.setCurrContext("home");
    }

    @Test
    void testWhenDoRuleReceiveForwardReportFromWhen()
            throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {

        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);

        IndividualNode door = (IndividualNode) Network.createNode("door", "individualnode");
        IndividualNode opened = (IndividualNode) Network.createNode("opened", "individualnode");
        IndividualNode close = (IndividualNode) Network.createNode("close", "individualnode");

        Relation whenRelation = new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);

        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(door));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(close));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(opened));
        DownCableSet objprop4 = new DownCableSet(objectCable5, propertyCable4);
        DownCableSet actionobj5 = new DownCableSet(objectCable5, actionCable5);
        PropositionNode M0 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        ActNode M1 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable whenCable = new DownCable(whenRelation, new NodeSet(M0));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M1));
        DownCableSet whendoCableSet = new DownCableSet(whenCable, doCable);
        WhenDoNode P2 = (WhenDoNode) Network.createNode("whendonode", whendoCableSet);

        n.printNodes();
        M0.setHyp("home", 0);
        P2.setHyp("home", 0);
        M1.setPrimitive(true);
        M0.add();
    }

    @Test
    void testWhenDoRuleReceiveForwardReportNotFromWhen()
            throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {

        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);

        IndividualNode door = (IndividualNode) Network.createNode("door", "individualnode");
        IndividualNode opened = (IndividualNode) Network.createNode("opened", "individualnode");
        IndividualNode close = (IndividualNode) Network.createNode("close", "individualnode");

        Relation whenRelation = new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);

        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(door));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(close));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(opened));
        DownCableSet objprop4 = new DownCableSet(objectCable5, propertyCable4);
        DownCableSet actionobj5 = new DownCableSet(objectCable5, actionCable5);
        PropositionNode M0 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        ActNode M1 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable whenCable = new DownCable(whenRelation, new NodeSet(M0));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M1));
        DownCableSet whendoCableSet = new DownCableSet(whenCable, doCable);
        WhenDoNode P2 = (WhenDoNode) Network.createNode("whendonode", whendoCableSet);

        Report report=new Report(new Substitutions(),null, 0,
        true,InferenceType.FORWARD, P2,null);
        report.setReportType(ChannelType.RuleCons);
        report.setContextName("home");
        n.printNodes();
        M1.setPrimitive(true);
        M0.setHyp("home", 0);
        P2.setHyp("home", 0);
        Scheduler.initiate();
        Scheduler.addToHighQueue(report);
        Scheduler.schedule();
        
    }

    @Test
    void testNotAssertedWhenDoRuleReceiveForwardReportFromWhen()
            throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {

        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);
        Relation antecedentRelation=new Relation("ant", "propositionnode", Adjustability.NONE, 0);
        Relation consequentRelation=new Relation("cq", "propositionnode", Adjustability.NONE, 0);

        IndividualNode door = (IndividualNode) Network.createNode("door", "individualnode");
        IndividualNode opened = (IndividualNode) Network.createNode("opened", "individualnode");
        IndividualNode close = (IndividualNode) Network.createNode("close", "individualnode");
        IndividualNode room = (IndividualNode) Network.createNode("room", "individualnode");
        IndividualNode ocuupied = (IndividualNode) Network.createNode("ocuupied", "individualnode");

        Relation whenRelation = new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);

        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(door));
        DownCable objectCable6 = new DownCable(objectRelation, new NodeSet(room));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(close));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(opened));
        DownCable propertyCable6 = new DownCable(properetyRelation, new NodeSet(ocuupied));
        

        DownCableSet objprop4 = new DownCableSet(objectCable5, propertyCable4);
        DownCableSet actionobj5 = new DownCableSet(objectCable5, actionCable5);
        DownCableSet objprop6 = new DownCableSet(objectCable6, propertyCable6);
        PropositionNode M0 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        ActNode M1 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable whenCable = new DownCable(whenRelation, new NodeSet(M0));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M1));
        DownCableSet whendoCableSet = new DownCableSet(whenCable, doCable);
        WhenDoNode P2 = (WhenDoNode) Network.createNode("whendonode", whendoCableSet);
        PropositionNode M3 = (PropositionNode) Network.createNode("propositionnode", objprop6);

        DownCable antCable1=new DownCable(antecedentRelation, new NodeSet(M3));
       DownCable consCable1=new DownCable(consequentRelation, new NodeSet(P2));
       DownCableSet antConsCableSet=new DownCableSet(antCable1,consCable1);
       AndEntailment P4=(AndEntailment)Network.createNode("andentailment", antConsCableSet);

        n.printNodes();
        M0.setHyp("home", 0);
        P4.setHyp("home", 0);
        M1.setPrimitive(true);
        M0.add();
    }
}
