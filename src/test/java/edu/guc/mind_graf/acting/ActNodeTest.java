//package edu.guc.mind_graf.acting;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Stack;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import edu.guc.mind_graf.acting.rules.DoIfNode;
//import edu.guc.mind_graf.acting.rules.WhenDoNode;
//import edu.guc.mind_graf.cables.DownCable;
//import edu.guc.mind_graf.cables.DownCableSet;
//import edu.guc.mind_graf.caseFrames.Adjustability;
//import edu.guc.mind_graf.components.Substitutions;
//import edu.guc.mind_graf.context.ContextController;
//import edu.guc.mind_graf.exceptions.DirectCycleException;
//import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
//import edu.guc.mind_graf.exceptions.NoSuchTypeException;
//import edu.guc.mind_graf.mgip.InferenceType;
//import edu.guc.mind_graf.mgip.reports.KnownInstance;
//import edu.guc.mind_graf.mgip.reports.Report;
//import edu.guc.mind_graf.mgip.reports.Report;
//import edu.guc.mind_graf.mgip.Scheduler;
//import edu.guc.mind_graf.mgip.requests.ChannelSet;
//import edu.guc.mind_graf.mgip.requests.ChannelType;
//import edu.guc.mind_graf.network.Network;
//import edu.guc.mind_graf.nodes.ActNode;
//import edu.guc.mind_graf.nodes.IndividualNode;
//import edu.guc.mind_graf.nodes.Node;
//import edu.guc.mind_graf.nodes.PropositionNode;
//import edu.guc.mind_graf.nodes.RuleNode;
//import edu.guc.mind_graf.relations.Relation;
//import edu.guc.mind_graf.set.NodeSet;
//import edu.guc.mind_graf.set.Set;
//
//public class ActNodeTest {
//    Network n;
//    PropositionNode M0;
//    ActNode M5;
//    ActNode M3;
//    ActNode M4;
//    PropositionNode M2;
//    ActNode M6;
//    Relation objectRelation;
//    Relation properetyRelation;
//    Relation actionRelation;
//
//    @BeforeEach
//    void setUp() throws NoSuchTypeException {
//        n=new Network();
//        Set<String,Integer> attitudeNames = new Set<>();
//		attitudeNames.add( "beliefs",0);
//		attitudeNames.add("obligations",1);
//
//		ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
//		consistentAttitudes.add(new ArrayList<>(List.of(0)));
//		consistentAttitudes.add(new ArrayList<>(List.of(1)));
//		consistentAttitudes.add(new ArrayList<>(List.of(0,1)));
//
//		ContextController.setUp(attitudeNames,consistentAttitudes ,false);
//		ContextController.createNewContext("guc");
//        ContextController.setCurrContext("guc");
//
//        IndividualNode door=(IndividualNode)Network.createNode("door", "individualnode");
//        IndividualNode closed=(IndividualNode)Network.createNode("closed", "individualnode");
//        IndividualNode opened=(IndividualNode)Network.createNode("opened", "individualnode");
//        IndividualNode alarm=(IndividualNode)Network.createNode("alarm", "individualnode");
//        IndividualNode room=(IndividualNode)Network.createNode("room", "individualnode");
//        IndividualNode occupied=(IndividualNode)Network.createNode("occupied", "individualnode");
//        IndividualNode lock=(IndividualNode)Network.createNode("lock","individualnode");
//        IndividualNode deactivate=(IndividualNode)Network.createNode("deactivate", "individualnode");
//        IndividualNode light=(IndividualNode)Network.createNode("light", "individualnode");
//        IndividualNode turnon=(IndividualNode)Network.createNode("turnon", "individualnode");
//        IndividualNode leave=(IndividualNode)Network.createNode("leave", "individualnode");
//
//
//
//        objectRelation=new Relation("obj", "individualnode", Adjustability.NONE, 0);
//        properetyRelation=new Relation("properety", "individualnode", Adjustability.NONE, 0);
//        actionRelation=new Relation("action", "individual", Adjustability.NONE, 0);
//
//        DownCable objectCable1=new DownCable(objectRelation, new NodeSet(door));
//        DownCable objectCable2=new DownCable(objectRelation, new NodeSet(alarm));
//        DownCable objectCable3=new DownCable(objectRelation, new NodeSet(room));
//        DownCable objectCable4=new DownCable(objectRelation, new NodeSet(light));
//        DownCable propertyCable1=new DownCable(properetyRelation, new NodeSet(opened));
//        DownCable propertyCable2=new DownCable(properetyRelation, new NodeSet(closed));
//        DownCable propertyCable3=new DownCable(properetyRelation, new NodeSet(occupied));
//        DownCable actionCable1=new DownCable(actionRelation, new NodeSet(lock));
//        DownCable actionCable2=new DownCable(actionRelation, new NodeSet(deactivate));
//        DownCable actionCable3=new DownCable(actionRelation, new NodeSet(turnon));
//        DownCable actionCable4=new DownCable(actionRelation, new NodeSet(leave));
//
//
//        DownCableSet objProp1=new DownCableSet(objectCable1,propertyCable1);
//        DownCableSet objProp2=new DownCableSet(objectCable1,propertyCable2);
//        DownCableSet objProp3=new DownCableSet(objectCable3,propertyCable3);
//        DownCableSet actionobj1=new DownCableSet(objectCable2,actionCable2);
//        DownCableSet actionobj2=new DownCableSet(objectCable1,actionCable1);
//        DownCableSet actionobj3=new DownCableSet(objectCable4,actionCable3);
//        DownCableSet actionobj4=new DownCableSet(objectCable3,actionCable4);
//
//
//        M0=(PropositionNode)Network.createNode("propositionnode", objProp1);
//        PropositionNode M1=(PropositionNode)Network.createNode("propositionnode", objProp2);
//        M2=(PropositionNode)Network.createNode("propositionnode", objProp3);
//        M3=(ActNode)Network.createNode("actnode", actionobj1);
//        M4=(ActNode)Network.createNode("actnode", actionobj2);
//        M5=(ActNode)Network.createNode("actnode", actionobj3);
//        M6=(ActNode)Network.createNode("actnode", actionobj4);
//
//
//
//    }
//    @Test
//    void testPrimitiveActWithoutPreconditions() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
//        M5.setPrimitive(true);
//        Scheduler.initiate();
//        Scheduler.addToActQueue(M5);
//        Scheduler.schedule();
//
//
//
//    }
//
//    @Test
//    void testPrimitiveActWithAssertedPreconditions() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//
//        Relation preconditionRelation=new Relation("precondition", "propositionnode", Adjustability.NONE, 0);
//        Relation actRelation=new Relation("act", "actnode", Adjustability.NONE, 0);
//
//        DownCable preconditionCable=new DownCable(preconditionRelation, new NodeSet(M2));
//        DownCable actCable=new DownCable(actRelation, new NodeSet(M3));
//        DownCableSet preconditionActCable1=new DownCableSet(preconditionCable,actCable);
//
//        PropositionNode preconditionActNode1=(PropositionNode)Network.createNode("propositionnode", preconditionActCable1);
//
//
//
//        M3.setPrimitive(true);
//        Scheduler.initiate();
//        Scheduler.addToActQueue(M3);
//        Scheduler.schedule();
//
//    }
//
//    @Test
//    void testPrimitiveActWithNotAssertedPreconditions() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//
//        Relation preconditionRelation=new Relation("precondition", "propositionnode", Adjustability.NONE, 0);
//        Relation actRelation=new Relation("act", "actnode", Adjustability.NONE, 0);
//
//        DownCable preconditionCable=new DownCable(preconditionRelation, new NodeSet(M2));
//        DownCable actCable=new DownCable(actRelation, new NodeSet(M3));
//        DownCableSet preconditionActCable1=new DownCableSet(preconditionCable,actCable);
//
//        PropositionNode preconditionActNode1=(PropositionNode)Network.createNode("propositionnode", preconditionActCable1);
//
//        M3.setPrimitive(true);
//        Scheduler.initiate();
//        Scheduler.addToActQueue(M3);
//        Scheduler.schedule();
//
//    }
//
//    @Test
//    void testComplexActWithAssertedPreconditions() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//
//        Relation preconditionRelation=new Relation("precondition", "propositionnode", Adjustability.NONE, 0);
//        Relation actRelation=new Relation("act", "actnode", Adjustability.NONE, 0);
//        Relation planRelation=new Relation("plan", "actnode", Adjustability.NONE, 0);
//
//        DownCable preconditionCable=new DownCable(preconditionRelation, new NodeSet(M0));
//        DownCable actCable=new DownCable(actRelation, new NodeSet(M6));
//        DownCableSet preconditionActCable1=new DownCableSet(preconditionCable,actCable);
//        DownCable planCable=new DownCable(planRelation, new NodeSet(M4));
//        DownCable actCable2=new DownCable(actRelation, new NodeSet(M6));
//        DownCableSet planActCable1=new DownCableSet(planCable,actCable2);
//
//        PropositionNode preconditionActNode1=(PropositionNode)Network.createNode("propositionnode", preconditionActCable1);
//        PropositionNode planActNode1=(PropositionNode)Network.createNode("propositionnode", planActCable1);
//
//
//        Scheduler.initiate();
//        Scheduler.addToActQueue(M6);
//        Scheduler.schedule();
//
//    }
//
//    @Test
//    void testDoIf() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//        IndividualNode chair=(IndividualNode)Network.createNode("chair", "individualnode");
//        IndividualNode heavy=(IndividualNode)Network.createNode("heavy", "individualnode");
//        IndividualNode variableNode=(IndividualNode)Network.createVariableNode("x", "individualnode");
//        IndividualNode carry=(IndividualNode)Network.createNode("carry", "individualnode");
//
//
//        Relation ifRelation=new Relation("0-if", "propositionnode", Adjustability.NONE, 0);
//        Relation doRelation=new Relation("do", "actnode", Adjustability.NONE, 0);
//
//        DownCable objectCable5=new DownCable(objectRelation, new NodeSet(chair));
//        DownCable objectCable6=new DownCable(objectRelation, new NodeSet(variableNode));
//        DownCable actionCable5=new DownCable(actionRelation, new NodeSet(carry));
//
//        DownCable propertyCable4=new DownCable(properetyRelation, new NodeSet(heavy));
//        //DownCable propertyCable5=new DownCable(properetyRelation, new NodeSet(heavy));
//        DownCableSet objprop4=new DownCableSet(objectCable5,propertyCable4);
//        //DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable5);
//        DownCableSet actionobj5=new DownCableSet(objectCable5,actionCable5);
//        PropositionNode M7=(PropositionNode)Network.createNode("propositionnode", objprop4);
//        //PropositionNode M8=(PropositionNode)Network.createNode("propositionnode", objprop5);
//        ActNode M8=(ActNode)Network.createNode("actnode", actionobj5);
//        DownCable ifCable=new DownCable(ifRelation, new NodeSet(M7));
//        DownCable doCable=new DownCable(doRelation, new NodeSet(M8));
//        DownCableSet doifCableSet=new DownCableSet(ifCable,doCable);
//        DoIfNode P9=(DoIfNode)Network.createNode("doifnode", doifCableSet);
//        for(Node n:P9.getFreeVariables()){
//            System.out.println("Free variables are"+n);
//        }
//        n.printNodes();
//        M7.deduce();
//
//    }
//
//
//    @Test
//    void testWhenDoRuleReceiveForwardReportFromWhen() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//        IndividualNode chair=(IndividualNode)Network.createNode("chair", "individualnode");
//        IndividualNode heavy=(IndividualNode)Network.createNode("heavy", "individualnode");
//        IndividualNode variableNode=(IndividualNode)Network.createVariableNode("x", "individualnode");
//        IndividualNode move=(IndividualNode)Network.createNode("move", "individualnode");
//
//
//        Relation whenRelation=new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
//        Relation doRelation=new Relation("do", "actnode", Adjustability.NONE, 0);
//
//        DownCable objectCable5=new DownCable(objectRelation, new NodeSet(chair));
//        DownCable objectCable6=new DownCable(objectRelation, new NodeSet(variableNode));
//        DownCable actionCable5=new DownCable(actionRelation, new NodeSet(move));
//
//        DownCable propertyCable4=new DownCable(properetyRelation, new NodeSet(heavy));
//        DownCableSet objprop4=new DownCableSet(objectCable5,propertyCable4);
//        DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable4);
//        DownCableSet actionobj5=new DownCableSet(objectCable5,actionCable5);
//        PropositionNode M7=(PropositionNode)Network.createNode("propositionnode", objprop4);
//        PropositionNode M8=(PropositionNode)Network.createNode("propositionnode", objprop5);
//        ActNode M9=(ActNode)Network.createNode("actnode", actionobj5);
//        DownCable whenCable=new DownCable(whenRelation, new NodeSet(M7));
//        DownCable doCable=new DownCable(doRelation, new NodeSet(M9));
//        DownCableSet whendoCableSet=new DownCableSet(whenCable,doCable);
//        WhenDoNode P9=(WhenDoNode)Network.createNode("whendonode", whendoCableSet);
//        for(Node n:P9.getFreeVariables()){
//            System.out.println("Free variables are"+n);
//        }
//        n.printNodes();
//        M7.add();
//
//    }
//
//
//    @Test
//    void testWhenDoRuleReceiveForwardReportNotFromWhen() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//        IndividualNode chair=(IndividualNode)Network.createNode("chair", "individualnode");
//        IndividualNode heavy=(IndividualNode)Network.createNode("heavy", "individualnode");
//        IndividualNode variableNode=(IndividualNode)Network.createVariableNode("x", "individualnode");
//        IndividualNode move=(IndividualNode)Network.createNode("move", "individualnode");
//
//
//        Relation whenRelation=new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
//        Relation doRelation=new Relation("do", "actnode", Adjustability.NONE, 0);
//
//        DownCable objectCable5=new DownCable(objectRelation, new NodeSet(chair));
//        DownCable objectCable6=new DownCable(objectRelation, new NodeSet(variableNode));
//        DownCable actionCable5=new DownCable(actionRelation, new NodeSet(move));
//
//        DownCable propertyCable4=new DownCable(properetyRelation, new NodeSet(heavy));
//        DownCableSet objprop4=new DownCableSet(objectCable5,propertyCable4);
//        DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable4);
//        DownCableSet actionobj5=new DownCableSet(objectCable5,actionCable5);
//        PropositionNode M7=(PropositionNode)Network.createNode("propositionnode", objprop4);
//        PropositionNode M8=(PropositionNode)Network.createNode("propositionnode", objprop5);
//        ActNode M9=(ActNode)Network.createNode("actnode", actionobj5);
//        DownCable whenCable=new DownCable(whenRelation, new NodeSet(M7));
//        DownCable doCable=new DownCable(doRelation, new NodeSet(M9));
//        DownCableSet whendoCableSet=new DownCableSet(whenCable,doCable);
//        WhenDoNode P10=(WhenDoNode)Network.createNode("whendonode", whendoCableSet);
//        for(Node n:P10.getFreeVariables()){
//            System.out.println("Free variables are"+n);
//        }
//        //Report report=new Report(new Substitutions(),null, 0, true,InferenceType.FORWARD, P10);
//        Scheduler.initiate();
//        //Scheduler.addToHighQueue(report);
//        Scheduler.schedule();
//        n.printNodes();
//
//
//    }
//    @Test
//    void testWhenDoRuleWithSubstitutions() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//        IndividualNode chair=(IndividualNode)Network.createNode("chair", "individualnode");
//        IndividualNode heavy=(IndividualNode)Network.createNode("heavy", "individualnode");
//        IndividualNode variableNode=(IndividualNode)Network.createVariableNode("x", "individualnode");
//        IndividualNode move=(IndividualNode)Network.createNode("move", "individualnode");
//
//
//        Relation whenRelation=new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
//        Relation doRelation=new Relation("do", "actnode", Adjustability.NONE, 0);
//
//        DownCable objectCable5=new DownCable(objectRelation, new NodeSet(chair));
//        DownCable objectCable6=new DownCable(objectRelation, new NodeSet(variableNode));
//        DownCable actionCable5=new DownCable(actionRelation, new NodeSet(move));
//
//        DownCable propertyCable4=new DownCable(properetyRelation, new NodeSet(heavy));
//        DownCableSet objprop4=new DownCableSet(objectCable5,propertyCable4);
//        DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable4);
//        DownCableSet actionobj5=new DownCableSet(objectCable6,actionCable5);
//        PropositionNode M7=(PropositionNode)Network.createNode("propositionnode", objprop4);
//        PropositionNode M8=(PropositionNode)Network.createNode("propositionnode", objprop5);
//        ActNode M9=(ActNode)Network.createNode("actnode", actionobj5);
//        DownCable whenCable=new DownCable(whenRelation, new NodeSet(M8));
//        DownCable doCable=new DownCable(doRelation, new NodeSet(M9));
//        DownCableSet whendoCableSet=new DownCableSet(whenCable,doCable);
//        WhenDoNode P10=(WhenDoNode)Network.createNode("whendonode", whendoCableSet);
//        for(Node n:P10.getFreeVariables()){
//            System.out.println("Free variables are"+n);
//        }
//        Substitutions substitutions=new Substitutions();
//        substitutions.add(variableNode, chair);
//        //Report report=new Report(substitutions,null, 0, true,InferenceType.FORWARD, P10);
//       //report.setContextName("guc");
//        //report.setReportType(ChannelType.RuleCons);
//        //P10.getKnownInstances().addKnownInstance(report);
//        //M8.getKnownInstances().addKnownInstance(report);
//        Scheduler.initiate();
//        //Scheduler.addToHighQueue(report);
//        Scheduler.schedule();
//
//    }
//
//    @Test
//    void testWhenDoRuleReceiveBackwardReport() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
//        IndividualNode chair=(IndividualNode)Network.createNode("chair", "individualnode");
//        IndividualNode heavy=(IndividualNode)Network.createNode("heavy", "individualnode");
//        IndividualNode variableNode=(IndividualNode)Network.createVariableNode("x", "individualnode");
//        IndividualNode move=(IndividualNode)Network.createNode("move", "individualnode");
//
//
//        Relation whenRelation=new Relation("0-when", "propositionnode", Adjustability.NONE, 0);
//        Relation doRelation=new Relation("do", "actnode", Adjustability.NONE, 0);
//
//
//        DownCable objectCable5=new DownCable(objectRelation, new NodeSet(chair));
//        DownCable objectCable6=new DownCable(objectRelation, new NodeSet(variableNode));
//        DownCable actionCable5=new DownCable(actionRelation, new NodeSet(move));
//
//        DownCable propertyCable4=new DownCable(properetyRelation, new NodeSet(heavy));
//        DownCableSet objprop4=new DownCableSet(objectCable5,propertyCable4);
//        DownCableSet objprop5=new DownCableSet(objectCable6,propertyCable4);
//        DownCableSet actionobj5=new DownCableSet(objectCable5,actionCable5);
//        PropositionNode M7=(PropositionNode)Network.createNode("propositionnode", objprop4);
//        PropositionNode M8=(PropositionNode)Network.createNode("propositionnode", objprop5);
//        ActNode M9=(ActNode)Network.createNode("actnode", actionobj5);
//        DownCable whenCable=new DownCable(whenRelation, new NodeSet(M7));
//        DownCable doCable=new DownCable(doRelation, new NodeSet(M9));
//        DownCableSet whendoCableSet=new DownCableSet(whenCable,doCable);
//        WhenDoNode P10=(WhenDoNode)Network.createNode("whendonode", whendoCableSet);
//
//        Relation antecedentRelation=new Relation("antecedent", "propositionnode", Adjustability.NONE, 0);
//        Relation consequentRelation=new Relation("consequent", "propositionnode", Adjustability.NONE, 0);
//        DownCable antCable1=new DownCable(antecedentRelation, new NodeSet(M0));
//        DownCable consCable1=new DownCable(consequentRelation, new NodeSet(P10));
//        DownCableSet antConsCableSet=new DownCableSet(antCable1,consCable1);
//        RuleNode P11=(RuleNode)Network.createNode("rulenode", antConsCableSet);
//
//        //Report report=new Report(new Substitutions(),null, 0, true,InferenceType.BACKWARD, P10);
//
//        n.printNodes();
//        P10.deduce();
//
//    }
//
//
//
//
//
//
//
//
//
//}
