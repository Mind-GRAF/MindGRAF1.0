package mindG.mgip.requests;

import mindG.network.Node;

public class Request {

    Channel channel;
    Node reporterNode;

    public Request(Channel channel, Node reporterNode) {
        this.channel = channel;
        this.reporterNode = reporterNode;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Node getReporterNode() {
        return reporterNode;
    }

    public void setReporterNode(Node reporterNode) {
        this.reporterNode = reporterNode;
    }

    public String stringifyRequest() {
        Node reporterNode = this.getReporterNode();
        String channelId = this.channel.stringifyChannelID() +
                " reportedBy " + reporterNode.getName();
        return channelId;
    }

}
