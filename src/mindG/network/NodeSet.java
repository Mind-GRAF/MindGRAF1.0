package mindG.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class NodeSet implements Iterable<Node> {
    private HashMap<String, Node> nodes;

    public NodeSet() {
        nodes = new HashMap<String, Node>();
    }

    @Override
    public Iterator<Node> iterator() {
        // TODO Auto-generated method stub
        return ((Iterable<Node>) nodes).iterator();
    }

    public NodeSet union(NodeSet otherSet) {
        NodeSet result = new NodeSet();
        result.putAll(this.nodes);
        otherSet.addAllTo(result);
        return result;
    }

    public void putAll(HashMap<String, Node> Set) {
        this.nodes.putAll(Set);

    }

    public void addAllTo(NodeSet nodeSet) {
        nodeSet.putAll(this.nodes);
    }

    // public String toString() {
    // String s = "[";
    // int i = 1;
    // for (Node n : nodes.values()) {
    // s += n.getName() + (i == nodes.values().size() ? "" : ",");
    // i++;
    // }
    // s += "]";
    // return s;

    // }

    public void add(Node n) {
        nodes.put(n.getName(), n);
    }

    public int size() {
        return nodes.size();
    }

    public Node remove(Node n) {
        return nodes.remove(n.getName());
    }

    public void removeAll() {
        nodes.clear();
    }

    public Collection<Node> getValues() {
        return this.nodes.values();
    }

    public boolean isEmpty() {
        return this.nodes.size() == 0;
    }

    public Node get(String name) {
        return nodes.get(name);
    }

    public boolean contains(Object s) {
        return this.nodes.containsKey(s) || this.nodes.containsValue(s);
    }

}
