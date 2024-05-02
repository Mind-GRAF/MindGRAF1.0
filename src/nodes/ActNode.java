package nodes;

import mgip.Report;
import mgip.Scheduler;
import mgip.requests.Channel;
import mgip.requests.ChannelSet;
import mgip.requests.ChannelType;
import mgip.requests.MatchChannel;
import network.Controller;
import network.Network;
import relations.Relation;
import set.NodeSet;

import java.util.*;

import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import caseFrames.CaseFrame;
import exceptions.NoSuchTypeException;

public abstract class ActNode extends Node {

    protected ChannelSet outgoingChannels;
    private Agenda agenda;
	private int count;
	private Node variable;
	private ArrayList<Report> reports;

	public ChannelSet getOutgoingChannels() {
		return outgoingChannels;
	}

	public void setOutgoingChannels(ChannelSet outgoingChannels) {
		this.outgoingChannels = outgoingChannels;
	}

    public ActNode(String name, Boolean isVariable) {
        super(name, isVariable);
        outgoingChannels = new ChannelSet();
        agenda = Agenda.DONE;

    }

    public ActNode(DownCableSet downCableSet) {
        super(downCableSet);
        outgoingChannels = new ChannelSet();
        agenda = Agenda.DONE;
    }

    public void addToOutgoingChannels(Channel channel) {
        outgoingChannels.addChannel(channel);
    }

	public void restartAgenda(){
		agenda = Agenda.START;
	}

	public abstract void runActuator(ActNode node) throws NoSuchTypeException;

	public void searchForPlansInAchieve(PropositionNode goal) throws NoSuchTypeException{
        Relation planRelation = Network.createRelation("plan", "actNode", Adjustability.NONE, 0);
        Relation goalRelation = Network.createRelation("goal", "propositionNode", Adjustability.NONE, 0);
        variable = Network.createVariableNode("A" + count, "actnode"); //TODO: Fix it with marwa
        DownCable goalCable = new DownCable(goalRelation, new NodeSet(goal));
        DownCable planCable = new DownCable(planRelation, new NodeSet(variable)); //TODO: Fix it with marwa
        DownCableSet molecularDownCableSet = new DownCableSet(goalCable, planCable);
        PropositionNode molecularNode = (PropositionNode) Network.createNode("propositionnode", molecularDownCableSet);
        //sendRequestsToNodeSet(new NodeSet(molecularNode), null, null, Controller.getCurrContext(), 0, ChannelType.Act, this);
        count++;
    }

	public NodeSet processReportsInAct() {
		NodeSet nodes = new NodeSet();
		for (Report report : reports) {
			if (report.isSign()) {
				Node resultNode = report.getSubstitutions().get(variable); //TODO: Fix it with marwa
				nodes.add(resultNode);
			}
		}
		reports.clear();
		return nodes;
	}

	public void sendDoOneToActQueue(NodeSet nodes) throws NoSuchTypeException {
        Relation doOneAction = Network.createRelation("action", "individualNode", Adjustability.NONE, 0);
        Relation doOneobj = Network.createRelation("obj", "actNode", Adjustability.NONE, 0);
        Node doOneBaseNode = Network.createNode("DoOne" + achieveCount++, "individualnode");
        DownCable actionCable = new DownCable(doOneAction, new NodeSet(doOneBaseNode));
        DownCable objCable = new DownCable(doOneobj, nodes);
        DownCableSet doOneDownCableSet = new DownCableSet(actionCable, objCable);
        DoOneNode doOneMolecularNode = (DoOneNode) Network.createNode("actnode", doOneDownCableSet);
        doOneMolecularNode.restartAgenda();
        Scheduler.addToActQueue(doOneMolecularNode);
    }
}