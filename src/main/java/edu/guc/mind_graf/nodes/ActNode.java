package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelSet;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.rules.AndOr;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.cables.UpCable;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;

public class ActNode extends Node {
    protected ChannelSet outgoingChannels;
    private ActAgenda agenda;
    private ArrayList<Report> reports = new ArrayList<>();
    private Node variable;
    private NodeSet preconditions;
    // private NodeSet effects;
    private boolean isPrimitive;
    private static int variableCount = 0;
    private ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>> supports;

    public void setPrimitive(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    public ChannelSet getOutgoingChannels() {
        return outgoingChannels;
    }

    public void setOutgoingChannels(ChannelSet outgoingChannels) {
        this.outgoingChannels = outgoingChannels;
    }

    public ActNode(String name, Boolean isVariable) {
        super(name, isVariable);
        outgoingChannels = new ChannelSet();
        agenda = ActAgenda.START;
        supports = new ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>>();

    }

    public ActNode(DownCableSet downCableSet) {
        super(downCableSet);
        outgoingChannels = new ChannelSet();
        agenda = ActAgenda.START;
        supports = new ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>>();

    }

    public void runActuator() throws NoSuchTypeException {
        System.out.println("running " + this.getName() + " act's actuator");

    }

    public void addToOutgoingChannels(Channel channel) {
        outgoingChannels.addChannel(channel);
    }

    public void addReport(Report report) {
        this.reports.add(report);
    }

    public void addJustificationBasedSupport(HashMap<Integer, PropositionNodeSet> newSupport, int attitude) {

    }

    public void addToSupports(
            ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>> support) {
        this.supports.addAll(support);
    }

    public ArrayList<Report> getReports(){
        return this.reports;
    }

    protected void restartAgenda() {
        agenda = ActAgenda.START;
    }

    protected NodeSet processReportsInAct() {
        System.out.println(this.getName() + " Processing Reports as act node ");
        if (reports.isEmpty()) {
            return null;
        }
        System.out.println(variable);

        System.out.println(reports.size());
        NodeSet nodes = new NodeSet();
        if (this.agenda == ActAgenda.FIND_PLANS) {
            System.out.println("hey");
        }
        for (Report report : reports) {
            if (report.isSign()) {
                System.out.println(report.stringifyReport());
                System.out.println(report.getSubstitutions());

                // Node resultNode = report.getSubstitutions().get(variable);
                nodes.add(report.getSubstitutions().get(variable));
            }

        }

        reports.clear();
        if (nodes.isEmpty()) {
            return null;
        }
        return nodes;

    }

    private boolean isPreconditionsAsserted() {
        int count = 0;
        for (Report report : reports) {
            if (report.isSign()) {
                count++;
            }
        }
        return preconditions.size() == count;
    }

    private void sendPreconditionsToActQueue() throws NoSuchTypeException {
        NodeSet achieveSet = new NodeSet();
        for (Node precondition : preconditions) {
            Relation achieveAction = Network.createRelation("action", "individualNode", Adjustability.NONE, 0);
            Relation achieveObj = Network.createRelation("obj", "propositionNode", Adjustability.NONE, 0);
            Node achieveBaseNode = Network.createNode("Achieve" + AchieveNode.achieveCount++, "individualnode");
            DownCable actionCable = new DownCable(achieveAction, new NodeSet(achieveBaseNode));
            DownCable objCable = new DownCable(achieveObj, new NodeSet(precondition));
            DownCableSet achieveDownCableSet = new DownCableSet(actionCable, objCable);
            AchieveNode achieveMolecularNode = (AchieveNode) Network.createNode("achievenode", achieveDownCableSet);
            achieveSet.add(achieveMolecularNode);
        }
        Relation doAllAction = Network.createRelation("action", "individualNode", Adjustability.NONE, 0);
        Relation doAllobj = Network.createRelation("obj", "actNode", Adjustability.NONE, 0);
        Node doAllBaseNode = Network.createNode("DoAll" + DoAllNode.doAllCount++, "individualnode");
        DownCable actionCable2 = new DownCable(doAllAction, new NodeSet(doAllBaseNode));
        DownCable objCable2 = new DownCable(doAllobj, achieveSet);
        DownCableSet doAllDownCableSet = new DownCableSet(actionCable2, objCable2);
        DoAllNode doAllMolecularNode = (DoAllNode) Network.createNode("doallnode", doAllDownCableSet);
        Scheduler.addToActQueue(doAllMolecularNode);
        System.out.println(doAllMolecularNode);
        System.out.println("A DoALL control act of achieving preconditions of " + this.getName()
                + " act is scheduled successfully on the act stack.");

    }


    protected void sendDoOneToActQueue(NodeSet nodes) throws NoSuchTypeException {
        Relation doOneAction = Network.createRelation("action", "individualNode", Adjustability.NONE, 0);
        Relation doOneobj = Network.createRelation("obj", "actNode", Adjustability.NONE, 0);
        Node doOneBaseNode = Network.createNode("DoOne" + DoOneNode.doOneCount++, "individualnode");
        DownCable actionCable = new DownCable(doOneAction, new NodeSet(doOneBaseNode));
        DownCable objCable = new DownCable(doOneobj, nodes);
        DownCableSet doOneDownCableSet = new DownCableSet(actionCable, objCable);
        DoOneNode doOneMolecularNode = (DoOneNode) Network.createNode("doonenode", doOneDownCableSet);
        Scheduler.addToActQueue(doOneMolecularNode);
        System.out.println("A DoOne control act of doing the plans of " + this.getName()
                + " act is scheduled successfully on the act stack.");

    }

    protected void searchForPlansInAchieve(PropositionNode goal) throws NoSuchTypeException {
        Relation planRelation = Network.createRelation("plan", "actNode", Adjustability.NONE, 0);
        Relation goalRelation = Network.createRelation("goal", "propositionNode", Adjustability.NONE, 0);
        variable = Network.createVariableNode("A" + variableCount, "actnode");
        DownCable goalCable = new DownCable(goalRelation, new NodeSet(goal));
        DownCable planCable = new DownCable(planRelation, new NodeSet(variable));
        DownCableSet molecularDownCableSet = new DownCableSet(goalCable, planCable);
        PropositionNode molecularNode = (PropositionNode) Network.createNode("propositionnode", molecularDownCableSet);
        sendRequestsToNodeSet(new NodeSet(molecularNode), null, null, ContextController.getCurrContextName(), 0,
                ChannelType.Act, this);
        variableCount++;

    }

    private void sendRequest(boolean isPrecondition) throws NoSuchTypeException {
        if (isPrecondition) {
            System.out.println(this.getName() + " act is sending a request checking if it has any preconditions");
            variable = Network.createVariableNode("A" + variableCount, "propositionnode");
        } else {
            System.out.println(this.getName() + " act is sending a request checking if it has any plans");
            variable = Network.createVariableNode("A" + variableCount, "actnode");
        }
        Relation relation1 = Network.createRelation("act", "actNode", Adjustability.NONE, 1);
        Relation relation2;
        if (isPrecondition) {
            relation2 = Network.createRelation("precondition", "propositionNode", Adjustability.NONE, 0);
        } else {
            relation2 = Network.createRelation("plan", "actNode", Adjustability.NONE, 0);
        } // else {
          // relation2 = Network.createRelation("effect", "propositionNode",
          // Adjustability.NONE, 0);
          // }
        DownCable d1 = new DownCable(relation1, new NodeSet(this));
        DownCable d2 = new DownCable(relation2, new NodeSet(variable));
        DownCableSet downCableSet = new DownCableSet(d1, d2);
        PropositionNode molecularNode = (PropositionNode) Network.createNode("propositionnode", downCableSet);
        sendRequestsToNodeSet(new NodeSet(molecularNode), null, null, ContextController.getCurrContextName(), 0,
                ChannelType.Act, this);
        variableCount++;
    }

    public void perform() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException{
        /* BEGIN - Helpful Prints */
        System.out.println("perform() method initated.\n");
        System.out.println("-------------------------");
        /* END - Helpful Prints */
        Scheduler.initiate();
        String currentContextName = ContextController.getCurrContextName();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your desired attitude: ");
        String att = scanner.nextLine();
        scanner.close();
        int currentAttitudeID = 0;
        System.out.println("Performing an act initiated in Context: " + currentContextName + " & Attitude: "
                + currentAttitudeID);
        Scheduler.addToActQueue(this);
        System.out.println(Scheduler.schedule());
    }

    public void processIntends(boolean isHighStack) throws NoSuchTypeException, NoPlansExistForTheActException {
        String currentContextName=ContextController.getCurrContextName();
        boolean doAct=true;
        for (Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>> support : supports) {
            doAct=true;
            for(Integer attitude:support.getFirst().keySet()){
                for(PropositionNode node:support.getFirst().get(attitude)){
                    doAct&=node.supported(currentContextName, attitude,0);
                }
            }
            if(!doAct){
                continue;
            }
            for(Integer attitude:support.getSecond().keySet()){
                for(PropositionNode node:support.getFirst().get(attitude)){
                    doAct&=!node.supported(currentContextName, attitude,0);
                }
            }
            if(doAct){
                break;
            }
            
        }
        if(!doAct){
            System.out.println("The act is no longer needed to be done");
            return;
        }

        switch (agenda) {
            case START:
                System.out.println("In Start case");

                this.agenda = ActAgenda.FIND_PRECONDITIONS;
                if (!isHighStack) {
                    Scheduler.addToActQueue(this);
                } else {
                    Scheduler.addToHighActQueue(this);
                }
                sendRequest(true);
                break;

            case FIND_PRECONDITIONS:
                System.out.println("In find_preconditions case");
                preconditions = processReportsInAct();
                if (preconditions == null) {
                    System.out.println(this.getName() + " act doesn't have preconditions");
                    this.agenda = ActAgenda.EXECUTE;
                    // sendRequest(2, ContextController.getCurrContextName());
                } else {
                    this.agenda = ActAgenda.TEST;
                    System.out.println(this.getName() + " act have preconditions need to be tested");
                    System.out.println(this.getName()
                            + " act is sending a request checking if its preconditions are asserted or not");
                    for (Node precondition : preconditions) {
                        sendRequestsToNodeSet(new NodeSet(precondition), null, null,
                                ContextController.getCurrContextName(), 0,
                                ChannelType.Act,
                                this);
                    }

                }
                if (!isHighStack) {
                    Scheduler.addToActQueue(this);
                } else {
                    Scheduler.addToHighActQueue(this);
                }
                break;

            case TEST:
                System.out.println("In test case");

                if (isPreconditionsAsserted()) {
                    System.out.println("Preconditions of " + this.getName() + " act are asserted");
                    reports.clear();
                    this.agenda = ActAgenda.EXECUTE;
                    if (!isHighStack) {
                        Scheduler.addToActQueue(this);
                    } else {
                        Scheduler.addToHighActQueue(this);
                    }
                } else {
                    System.out.println("Preconditions of " + this.getName() + " act need to be asserted");
                    reports.clear();
                    this.agenda = ActAgenda.START;
                    if (!isHighStack) {
                        Scheduler.addToActQueue(this);
                    } else {
                        Scheduler.addToHighActQueue(this);
                    }
                    sendPreconditionsToActQueue();
                }

                break;

            // case FIND_EFFECTS:
            // System.out.println("In find effects case");

            // this.agenda = ActAgenda.EXECUTE;
            // Scheduler.addToActQueue(this);
            // NodeSet effectsAnded = processReportsInAct();
            // if (effectsAnded != null) {
            // for (Node effect : effectsAnded) {
            // if (effect instanceof AndOr) {
            // effects = ((AndOr) effect).getDownAntArgNodeSet();

            // } else {
            // if (effect instanceof PropositionNode) {
            // effects = new NodeSet(effect);

            // } else {
            // System.out
            // .println("There is a problem with the structure of effects of this act");
            // }
            // }
            // }
            // }
            // break;

            case EXECUTE:
                System.out.println("In execute case");
                System.out.println("reports size is" + reports.size());
                if (!isPrimitive) {
                    this.agenda = ActAgenda.FIND_PLANS;
                    Scheduler.addToActQueue(this);
                    sendRequest(false);
                } else {
                    this.agenda = ActAgenda.DONE;
                    this.runActuator();
                }

                break;

            case FIND_PLANS:
                System.out.println("In find_plans case");
                System.out.println("reports size is" + reports.size());

                NodeSet plans = processReportsInAct();
                if (plans == null) {
                    String action = this.getDownCableSet().get("action").getNodeSet().getNames().getFirst();
                    NodeSet objects = this.getDownCableSet().get("obj").getNodeSet();
                    throw new NoPlansExistForTheActException("Failed to execute the act of doing " + action
                            + " action on" + objects.toString() + " objects because no plans are found for it");
                } else {
                    sendDoOneToActQueue(plans);
                }
                this.agenda = ActAgenda.DONE;

                break;
            case DONE:
                System.out.println("In done case");
                break;

        }

    }

}
