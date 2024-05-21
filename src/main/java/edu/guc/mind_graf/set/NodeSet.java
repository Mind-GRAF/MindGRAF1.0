package edu.guc.mind_graf.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import edu.guc.mind_graf.nodes.Node;

public class NodeSet implements Iterable<Node> {
    private HashMap<String, Node> nodes;
    private boolean isFinal;

    public NodeSet() {
        nodes = new HashMap<String, Node>();
    }

    public NodeSet(HashMap<String, Node> nodes) {
        this.nodes = nodes;
    }

    public NodeSet(Node... nodes) {
        this.nodes = new HashMap<String, Node>();
        for (Node n : nodes)
            this.nodes.put(n.getName(), n);
    }

    public NodeSet(HashMap<String, Node> list, Node... nodes) {
        this.nodes = list;
        for (Node n : nodes)
            this.nodes.put(n.getName(), n);

    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public NodeSet union(NodeSet otherSet) {

        NodeSet result = new NodeSet();
        result.putAll(this.nodes);
        if (!isFinal) {
            otherSet.addAllTo(result);
        }
        return result;
    }

    public NodeSet intersection(NodeSet otherSet) {
        NodeSet result = new NodeSet();
        for (Node entry : this.nodes.values()) {
            if (otherSet.contains(entry)) {
                result.add(entry);
            }
        }
        if (!isFinal)
            return result;
        else
            return this;
    }

    public void putAll(HashMap<String, Node> Set) {
        if (!isFinal)
            this.nodes.putAll(Set);

    }

    public void addAllTo(NodeSet nodeSet) {
        nodeSet.putAll(this.nodes);
    }

    public String toString() {
        String s = "[";
        int i = 1;
        for (Node n : nodes.values()) {
            s += n.getName() + (i == nodes.values().size() ? "" : ",");
            i++;
        }
        s += "]";
        return s;

    }

    public void add(Node n) {
        if (!isFinal)
            nodes.put(n.getName(), n);
    }

    public Node getNode(int index) {
        int i = 0;
        for (Node node : this.nodes.values()) {
            if (i == index) {
                return node;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + i);
    }

    public int size() {
        return nodes.size();
    }

    public Node remove(Node n) {
        if (!isFinal)
            return nodes.remove(n.getName());
        else
            return null;
    }

    public void removeAll() {
        if (!isFinal)
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

    public ArrayList<String> getNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (Node node : this.nodes.values()) {
            result.add(node.getName());
        }
        return result;
    }

    public boolean equals(Object n) {
        NodeSet ns = (NodeSet) n;// changed because it makes more sense this way
        if(this.size() != ns.size())
            return false;
        for(Node node : this.nodes.values()){
            if(!ns.contains(node))
                return false;
        }
        return true;
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.values().iterator();
    }

    public NodeSet clone() {
        NodeSet clone = new NodeSet();
        for (Node n : this.nodes.values())
            clone.add(n);
        return clone;
    }

    public void clear() {
        if(!isFinal)
            nodes.clear();
    }

    public int getIntValue() {
        if(this.getNames().size() != 1)
            throw new IllegalArgumentException("NodeSet doesn't represent a number");
        String number = this.getNames().get(0);
        if(number.contains("temp")){
            number = number.substring(0, number.length() - 4);
        }
        return Integer.parseInt(number);
    }
}
