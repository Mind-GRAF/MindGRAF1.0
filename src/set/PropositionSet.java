package set;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import nodes.Node;

public class PropositionSet implements Iterable<PropositionNode> {

    private HashSet<Integer> nodes;
    private boolean isFinal;

    // Empty constructor
    public PropositionSet() {
        nodes = new HashSet<Integer>();
    }

    // Constructor using HashSet of Ids
    public PropositionSet(HashSet<Integer> nodes) {
        this.nodes = nodes;
    }

    // Constructor with arity parameter ... you can add variable number of IDs (
    // including Zero )
    public PropositionSet(Integer... nodeIDs) {
        this.nodes = new HashSet<Integer>();
        for (Integer id : nodeIDs)
            this.nodes.add(id);
    }

    // Constructor with array of nodes
    public PropositionSet(int[] nodeIDs) {
        this.nodes = new HashSet<Integer>();
        for (int id : nodeIDs) {
            this.nodes.add(id);
        }
    }

    // Constructor using both hashSet and araity parameter
    public PropositionSet(HashSet<Integer> list, Integer... nodeIDs) {
        this.nodes = list;
        for (Integer id : nodeIDs)
            this.nodes.add(id);

    }

    // constructor taking the node itself and storing it's id ( Ariaty Parameter
    // with variable number of nodes)
    public PropositionSet(Node... nodes) {
        this.nodes = new HashSet<Integer>();
        for (Node n : nodes)
            this.nodes.add(n.getId());
    }

    // constructor taking a nodeset itself and storing it's id
    public PropositionSet(NodeSet nodeSet) {
        this.nodes = new HashSet<Integer>();
        for (Node n : nodeSet.getValues())
            this.nodes.add(n.getId());
    }

    // Return the Ids of the propositions in the PropositionSet node hashset
    public int[] getProps() {
        int[] props = new int[this.nodes.size()];
        int i = 0;
        for (Integer id : this.nodes) {
            props[i] = id;
            i++;
        }
        return props;
    }

    // Returns an array of the props in a given PropositionSet but insures
    // immutability through deep cloning of the props done by the PropositionSet
    // constructor.
    public static int[] getPropsSafely(PropositionSet set) {
        return new PropositionSet(set.getProps()).getProps();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public PropositionSet union(PropositionSet otherSet) {

        PropositionSet result = new PropositionSet();
        result.putAll(this.nodes);
        if (!isFinal) {
            otherSet.addAllTo(result);
        }
        return result;
    }

    public PropositionSet intersection(NodeSet otherSet) {
        PropositionSet result = new PropositionSet();
        for (Integer entry : this.nodes) {
            if (otherSet.contains(entry)) {
                result.add(entry);
            }
        }
        if (!isFinal)
            return result;
        else
            return this;
    }

    public PropositionSet difference(PropositionSet otherSet) {
        PropositionSet result = new PropositionSet();
        for (Integer entry : this.nodes) {
            if (!otherSet.contains(entry)) {
                result.add(entry);
            }
        }
        return result;
    }

    public boolean isSubset(PropositionSet otherSet) {
        return otherSet.nodes.containsAll(this.nodes);
    }

    public void putAll(HashSet<Integer> Set) {
        if (!isFinal)
            this.nodes.addAll(Set);

    }

    public void putAll(HashMap<Integer, Node> Set) {

        if (!isFinal) {
            HashSet<Integer> s = new HashSet<Integer>();
            for (Node n : Set.values()) {
                s.add(n.getId());
            }
            this.nodes.addAll(s);

        }

    }

    public void addAllTo(PropositionSet nodeSet) {
        nodeSet.putAll(this.nodes);
    }

    public String toString() {
        String s = "[";
        int i = 1;
        for (int n : nodes) {
            s += n + (i == nodes.size() ? "" : ",");
            i++;
        }
        s += "]";
        return s;

    }

    public void add(int id) {
        if (!isFinal)
            nodes.add(id);
    }

    public void add(Node node) {
        if (!isFinal)
            nodes.add(node.getId());
    }

    public int size() {
        return nodes.size();
    }

    public boolean remove(int i) {
        if (!isFinal)
            return nodes.remove(i);
        else
            return false;
    }

    public boolean remove(Node n) {
        if (!isFinal)
            return nodes.remove(n.getId());
        else
            return false;

    }

    public void removeAll() {
        if (!isFinal)
            nodes.clear();
    }

    public HashSet<Integer> getValues() {
        return this.nodes;
    }

    public Collection<Node> getNodes() {
        HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
        for (Integer integer : this.nodes) {
            nodes.put(integer, Network.getNodeById(integer));
        }
        return nodes.values();
    }

    public boolean isEmpty() {
        return this.nodes.size() == 0;
    }

    public Node get(int id) {
        return Network.getNodeById(id);
    }

    public boolean contains(Integer s) {
        return this.nodes.contains(s);
    }

    public boolean contains(Node n) {
        return this.nodes.contains(n.getId());
    }

    public boolean equals(PropositionSet n) {
        return this.getValues().equals(n.getValues());
    }

    @Override
    public Iterator<PropositionNode> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

}
