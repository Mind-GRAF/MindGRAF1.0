package edu.guc.mind_graf.set;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import edu.guc.mind_graf.nodes.Node;

public class FreeVariableSet implements Iterable<Node>, Serializable {
    private HashMap<String, Node> freeVariables;

    public Collection<Node> getFreeVariables() {
        return freeVariables.values();
    }

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

    private String getFreeVariablesString() {
        StringBuilder sb = new StringBuilder();
        for (Node n : freeVariables.values()) {
            sb.append(n.getName()).append(", ");
        }
        if(sb.length() > 1){
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "FreeVariableSet{" + getFreeVariablesString() +
                '}';
    }
}
