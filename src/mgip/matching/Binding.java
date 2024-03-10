package mindG.mgip.matching;

import mindG.network.Node;

public class Binding {
    private Node sourceNode;
    private Node targetNode;
    private boolean nodeSide;

    public Binding(Node sourceNode, Node targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public Binding(Node sourceNode, Node targetNode, boolean nodeSide) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.nodeSide = nodeSide;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public boolean nodeSide() {
        return nodeSide;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return sourceNode + " = " + targetNode + ", " + nodeSide;
    }

}
