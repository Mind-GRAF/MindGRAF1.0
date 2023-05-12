package mindG.network;

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

    public void processReports() {
        // TODO Auto-generated method stub

    }

    public void processRequests() {
        // TODO Auto-generated method stub

    }

}
