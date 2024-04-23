package nodes;

import mgip.requests.Channel;
import mgip.requests.ChannelSet;

import cables.DownCableSet;

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