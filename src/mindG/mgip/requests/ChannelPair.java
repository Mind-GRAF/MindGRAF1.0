package mindG.mgip.requests;

import java.util.Collection;

import javafx.util.Pair;
import mindG.network.*;

public class ChannelPair {
    private Pair<Channel, NodeSet> pair;
    private NodeSet nodeSet;

    public ChannelPair(Channel channel) {
        pair = new Pair<>(channel, new NodeSet());
        this.nodeSet = pair.getValue();

    }

    public NodeSet getNodeSet() {
        return nodeSet;
    }

    public void addNode(Node node) {
        nodeSet.add(node);
    }

    public Pair<Channel, NodeSet> getPair() {
        return pair;
    }

    public void setPair(Pair<Channel, NodeSet> pair) {
        this.pair = pair;
    }

    public void setNodeSet(NodeSet nodeSet) {
        this.nodeSet = nodeSet;
    }

}
