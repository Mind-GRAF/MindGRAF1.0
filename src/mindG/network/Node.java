package mindG.network;

import mindG.mgip.requests.Channel;

public class Node {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVariableNode(PropositionNode propositionNode) {
        return false;
    }

    public VariableSet getFreeVariables(PropositionNode propositionNode) {
        return null;
    }

    public boolean nodeType(Node node) {
        return true;
    };

    public String getName() {
        return name;
    }

}
