package edu.guc.mind_graf.set;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import edu.guc.mind_graf.nodes.Node;

public class FreeVariableSet implements Iterable<Node>, Serializable {
    private HashMap<String, Node> freeVariables;

    public FreeVariableSet() {
        freeVariables = new HashMap<String, Node>();
    }

    public void add(Node n) {
        if (n.isVariable())
            freeVariables.put(n.getName(), n);
    }

    public void remove(Node n) {
        freeVariables.remove(n.getName());
    }

    public Node get(String name) {
        return freeVariables.get(name);
    }

    public void setFreeVariables(HashMap<String, Node> freeVariables) {
        this.freeVariables = freeVariables;
    }

    @Override
    public Iterator<Node> iterator() {
        return freeVariables.values().iterator();
    }

}
