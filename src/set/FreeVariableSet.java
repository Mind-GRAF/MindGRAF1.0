package set;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import nodes.Node;

public class FreeVariableSet implements Iterable<Node>, Serializable {
    private HashMap<String, Node> freeVariables;

    public FreeVariableSet() {
        freeVariables = new HashMap<String, Node>();
        // TODO Auto-generated constructor stub
    }

    public void add(Node n) {
        if (n.isVariable())
            freeVariables.put(n.getName(), n);
    }

    public void remove(Node n) {
        freeVariables.remove(n.getName());
    }

    public void get(String name) {
        freeVariables.get(name);
    }

    public void setFreeVariables(HashMap<String, Node> freeVariables) {
        this.freeVariables = freeVariables;
    }

    @Override
    public Iterator<Node> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

}
