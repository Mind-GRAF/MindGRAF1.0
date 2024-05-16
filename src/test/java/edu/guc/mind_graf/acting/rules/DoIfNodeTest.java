package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.List;

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
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.rules.AndEntailment;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.IndividualNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class DoIfNodeTest {
    @Test
    void testAssertedDoIf() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        Network n=new Network();
        Set<String,Integer> attitudeNames = new Set<>();
		attitudeNames.add( "beliefs",0);
		attitudeNames.add("obligations",1);

		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
		consistentAttitudes.add(new ArrayList<>(List.of(0)));
		consistentAttitudes.add(new ArrayList<>(List.of(1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,1)));

		NetworkController.setUp(attitudeNames,consistentAttitudes ,false,false,false,1);
		ContextController.createNewContext("home");
       ContextController.setCurrContext("home");
        IndividualNode chair = (IndividualNode) Network.createNode("chair", "individualnode");
        IndividualNode heavy = (IndividualNode) Network.createNode("heavy", "individualnode");
        IndividualNode variableNode = (IndividualNode) Network.createVariableNode("x", "individualnode");
        IndividualNode carry = (IndividualNode) Network.createNode("carry", "individualnode");

        Relation ifRelation = new Relation("0-if", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);
        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);

        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(chair));
        DownCable objectCable6 = new DownCable(objectRelation, new NodeSet(variableNode));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(carry));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(heavy));
        // DownCable propertyCable5=new DownCable(properetyRelation, new
        // NodeSet(heavy));
        DownCableSet objprop4 = new DownCableSet(objectCable5, propertyCable4);
        // DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable5);
        DownCableSet actionobj5 = new DownCableSet(objectCable5, actionCable5);
        PropositionNode M7 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        // PropositionNode M8=(PropositionNode)Network.createNode("propositionnode",
        // objprop5);
        ActNode M8 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable ifCable = new DownCable(ifRelation, new NodeSet(M7));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M8));
        DownCableSet doifCableSet = new DownCableSet(ifCable, doCable);
        DoIfNode P10 = (DoIfNode) Network.createNode("doifnode", doifCableSet);
       
        M8.setPrimitive(true);
        P10.setHyp("home", 0);
        M7.deduce();

    }

    @Test
    void IfGetsAsserted() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        Network n=new Network();
        Set<String,Integer> attitudeNames = new Set<>();
		attitudeNames.add( "beliefs",0);
		attitudeNames.add("obligations",1);

		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
		consistentAttitudes.add(new ArrayList<>(List.of(0)));
		consistentAttitudes.add(new ArrayList<>(List.of(1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,1)));

		NetworkController.setUp(attitudeNames,consistentAttitudes ,false,false,false,1);
		ContextController.createNewContext("home");
       ContextController.setCurrContext("home");
        IndividualNode chair = (IndividualNode) Network.createNode("chair", "individualnode");
        IndividualNode heavy = (IndividualNode) Network.createNode("heavy", "individualnode");
        IndividualNode variableNode = (IndividualNode) Network.createVariableNode("x", "individualnode");
        IndividualNode carry = (IndividualNode) Network.createNode("carry", "individualnode");
        IndividualNode big = (IndividualNode) Network.createNode("big", "individualnode");
        
        Relation ifRelation = new Relation("0-if", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);
        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);


        Relation antecedentRelation=new Relation("ant", "propositionnode", Adjustability.NONE, 0);
       Relation consequentRelation=new Relation("cq", "propositionnode", Adjustability.NONE, 0);
       
        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(chair));
        DownCable objectCable6 = new DownCable(objectRelation, new NodeSet(variableNode));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(carry));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(heavy));
        DownCable propertyCable5 = new DownCable(properetyRelation, new NodeSet(big));
        // DownCable propertyCable5=new DownCable(properetyRelation, new
        // NodeSet(heavy));
        DownCableSet objprop4 = new DownCableSet(objectCable5, propertyCable4);
        // DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable5);
        DownCableSet actionobj5 = new DownCableSet(objectCable5, actionCable5);
        DownCableSet objProp5 = new DownCableSet(objectCable5, propertyCable5);
        PropositionNode M7 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        // PropositionNode M8=(PropositionNode)Network.createNode("propositionnode",
        // objprop5);
        ActNode M8 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable ifCable = new DownCable(ifRelation, new NodeSet(M7));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M8));
        DownCableSet doifCableSet = new DownCableSet(ifCable, doCable);
        DoIfNode P10 = (DoIfNode) Network.createNode("doifnode", doifCableSet);

        PropositionNode chairBig = (PropositionNode) Network.createNode("propositionnode", objProp5);

        DownCable antCable1=new DownCable(antecedentRelation, new NodeSet(chairBig));
       DownCable consCable1=new DownCable(consequentRelation, new NodeSet(M7));
       DownCableSet antConsCableSet=new DownCableSet(antCable1,consCable1);
       AndEntailment P11=(AndEntailment)Network.createNode("andentailment", antConsCableSet);
       
        M8.setPrimitive(true);
        P10.setHyp("home", 0);
        chairBig.setHyp("home", 0);
        P11.setHyp("home", 0);
        M7.deduce();

    }
    @Test
    void testOpenDoIf() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        Network n=new Network();
        Set<String,Integer> attitudeNames = new Set<>();
		attitudeNames.add( "beliefs",0);
		attitudeNames.add("obligations",1);

		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
		consistentAttitudes.add(new ArrayList<>(List.of(0)));
		consistentAttitudes.add(new ArrayList<>(List.of(1)));
		consistentAttitudes.add(new ArrayList<>(List.of(0,1)));

		NetworkController.setUp(attitudeNames,consistentAttitudes ,false,false,false,1);
		ContextController.createNewContext("home");
       ContextController.setCurrContext("home");
        IndividualNode chair = (IndividualNode) Network.createNode("chair", "individualnode");
        IndividualNode heavy = (IndividualNode) Network.createNode("heavy", "individualnode");
        IndividualNode variableNode = (IndividualNode) Network.createVariableNode("x", "individualnode");
        IndividualNode carry = (IndividualNode) Network.createNode("carry", "individualnode");

        Relation ifRelation = new Relation("0-if", "propositionnode", Adjustability.NONE, 0);
        Relation doRelation = new Relation("do", "actnode", Adjustability.NONE, 0);
        Relation objectRelation = new Relation("obj", "individualnode", Adjustability.NONE, 0);
        Relation properetyRelation = new Relation("properety", "individualnode", Adjustability.NONE, 0);
        Relation actionRelation = new Relation("action", "individual", Adjustability.NONE, 0);

        DownCable objectCable5 = new DownCable(objectRelation, new NodeSet(chair));
        DownCable objectCable6 = new DownCable(objectRelation, new NodeSet(variableNode));
        DownCable actionCable5 = new DownCable(actionRelation, new NodeSet(carry));

        DownCable propertyCable4 = new DownCable(properetyRelation, new NodeSet(heavy));
        // DownCable propertyCable5=new DownCable(properetyRelation, new
        // NodeSet(heavy));
        DownCableSet objprop4 = new DownCableSet(objectCable6, propertyCable4);
        // DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable5);
        DownCableSet actionobj5 = new DownCableSet(objectCable6, actionCable5);
        PropositionNode M7 = (PropositionNode) Network.createNode("propositionnode", objprop4);
        // PropositionNode M8=(PropositionNode)Network.createNode("propositionnode",
        // objprop5);
        ActNode M8 = (ActNode) Network.createNode("actnode", actionobj5);
        DownCable ifCable = new DownCable(ifRelation, new NodeSet(M7));
        DownCable doCable = new DownCable(doRelation, new NodeSet(M8));
        DownCableSet doifCableSet = new DownCableSet(ifCable, doCable);
        DoIfNode P10 = (DoIfNode) Network.createNode("doifnode", doifCableSet);
        
        Substitutions substitutions=new Substitutions();
           substitutions.add(variableNode, chair);
        Report report=new Report(substitutions,null, 0, true,InferenceType.FORWARD, P10,null);
        report.setContextName("home");
       report.setReportType(ChannelType.RuleCons);
       P10.getKnownInstances().addKnownInstance(report);


        M8.setPrimitive(true);
        //P10.setHyp("home", 0);
        M7.deduce();

    }

}
