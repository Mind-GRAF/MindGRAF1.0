package nodes;

import mgip.Scheduler;
import mgip.requests.Channel;
import mgip.requests.ChannelSet;
import mgip.requests.MatchChannel;
import network.Network;
import set.NodeSet;
import relations.*;

import java.util.ArrayList;

import cables.DownCableSet;
import caseFrames.Adjustability;
import caseFrames.CaseFrame;

public class ActNode extends Node {

    protected ChannelSet outgoingChannels;
    private Agenda agenda;

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

    public void processIntends() {
    }

    public void restartAgenda() {
        agenda = Agenda.START;
    }

}
