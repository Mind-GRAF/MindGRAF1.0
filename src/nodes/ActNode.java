package nodes;

import mgip.requests.Channel;
import mgip.requests.ChannelSet;
import cables.DownCableSet;

public class ActNode extends Node {
    protected ChannelSet outgoingChannels;

    public ChannelSet getOutgoingChannels() {
        return outgoingChannels;
    }

    public void setOutgoingChannels(ChannelSet outgoingChannels) {
        this.outgoingChannels = outgoingChannels;
    }

    public ActNode(String name, Boolean isVariable) {
        super(name, isVariable);
        outgoingChannels = new ChannelSet();

    }

    public ActNode(DownCableSet downCableSet) {
        super(downCableSet);
        outgoingChannels = new ChannelSet();

    }

    public void addToOutgoingChannels(Channel channel) {
        outgoingChannels.addChannel(channel);
    }

    public void processIntends() {
    }

}
