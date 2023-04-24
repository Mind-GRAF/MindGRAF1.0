package mindG.network;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

public class VariableSet implements Iterable<Node>, Serializable {
    protected Vector<Node> variables;

    public VariableSet() {
        variables = new Vector<Node>();
    }

    @Override
    public Iterator<Node> iterator() {
        return variables.iterator();
    }
}
