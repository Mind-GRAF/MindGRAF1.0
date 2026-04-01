package edu.guc.mind_graf.set;

import java.util.*;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;

public class PropositionNodeSet implements Iterable<PropositionNode>,Cloneable {

    private HashSet<Integer> nodes;
    private boolean isFinal;

    // Empty constructor
    public PropositionNodeSet() {
        nodes = new HashSet<>();
    }

    // Constructor using HashSet of Ids
    public PropositionNodeSet(HashSet<Integer> nodes) {
        this.nodes = nodes;
    }

    // Constructor with arity parameter ... you can add variable number of IDs (
    // including Zero )
    public PropositionNodeSet(Integer... nodeIDs) {
        this.nodes = new HashSet<Integer>();
        Collections.addAll(this.nodes, nodeIDs);
    }

    // Constructor with array of nodes
    public PropositionNodeSet(int[] nodeIDs) {
        this.nodes = new HashSet<Integer>();
        for (int id : nodeIDs) {
            this.nodes.add(id);
        }
    }

    // Constructor using both hashSet and araity parameter
    public PropositionNodeSet(HashSet<Integer> list, Integer... nodeIDs) {
        this.nodes = list;
        Collections.addAll(this.nodes, nodeIDs);

    }

    // constructor taking the node itself and storing its id ( Ariaty Parameter
    // with variable number of nodes)
    public PropositionNodeSet(Node... nodes) {
        this.nodes = new HashSet<Integer>();
        for (Node n : nodes)
            this.nodes.add(n.getId());
    }

    // constructor taking a nodeset itself and storing its id
    public PropositionNodeSet(NodeSet nodeSet) {
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
    public static int[] getPropsSafely(PropositionNodeSet set) {
        return new PropositionNodeSet(set.getProps()).getProps();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public PropositionNodeSet union(PropositionNodeSet otherSet) {

        PropositionNodeSet result = new PropositionNodeSet();
        result.putAll(this.nodes);
        if (!isFinal) {
            otherSet.addAllTo(result);
        }
        return result;
    }

    public PropositionNodeSet intersection(NodeSet otherSet) {
        PropositionNodeSet result = new PropositionNodeSet();
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

    public PropositionNodeSet difference(PropositionNodeSet otherSet) {
        PropositionNodeSet result = new PropositionNodeSet();
        for (Integer entry : this.nodes) {
            if (!otherSet.contains(entry)) {
                result.add(entry);
            }
        }
        return result;
    }

    public boolean isSubset(PropositionNodeSet otherSet) {
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

    public void addAllTo(PropositionNodeSet nodeSet) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PropositionNodeSet other = (PropositionNodeSet) obj;
        return this.getValues().equals(other.getValues());
    }

    @Override
    public Iterator<PropositionNode> iterator() {
        return new Iterator<>() {
            private Iterator<Integer> iterator = nodes.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PropositionNode next() {
                return (PropositionNode) Network.getNodeById(iterator.next());
            }
        };
    }

    public NodeSet getCommonVariables(){
        NodeSet commonVariables = new NodeSet();
        if(this.isEmpty()){
            return commonVariables;
        }
        boolean first = true;
        for (Node n : this.getNodes()){
            if(first){
                commonVariables = n.getFreeVariables();
                first = false;
            }
            else
                commonVariables = commonVariables.intersection(n.getFreeVariables());
        }
        return commonVariables;
    }
    
    @Override
    public PropositionNodeSet clone() {
        PropositionNodeSet clone = new PropositionNodeSet((HashSet<Integer>) this.getValues().clone());
        clone.setIsFinal(this.isFinal);
        return clone;
    }

}
