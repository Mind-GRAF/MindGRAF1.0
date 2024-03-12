package components;

import java.util.HashMap;
import java.util.Map;

import nodes.Node;

public class Substitutions {

    private Map<Node, Node> map;

    public Substitutions() {
        map = new HashMap<>();
    }

    public void add(Node var, Node value) {
        map.put(var, value);
    }

    public Node get(Node var) {
        return map.get(var);
    }

    public boolean contains(Node var) {
        return map.containsKey(var);
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    public Substitutions getSubs(Substitutions sub) {
        return sub;
    }

    public void addSubs(Substitutions subs) {
        for (Node var : subs.map.keySet()) {
            Node value = subs.map.get(var);
            add(var, value);
        }
    }

    public boolean isSubsetOf(Substitutions otherSubs) {
        for (Node var : map.keySet()) {
            Node value = map.get(var);
            if (!otherSubs.contains(var) || !otherSubs.get(var).equals(value)) {
                return false;
            }
        }
        return true;
    }

    public boolean compatible(Substitutions substitutions) {
        if (isSubsetOf(substitutions))
            return true;
        return false;

    }

    // method for the switch substitutions
    public Substitutions switchReport(Substitutions switchSubs) {

        Substitutions newReportSubstitutions = new Substitutions();

        for (Node var : map.keySet()) {

            Node value = map.get(var);
            for (Node var1 : switchSubs.getMap().keySet()) {
                Node value1 = switchSubs.getMap().get(var1);
                if (!var1.getName().equals(value.getName())) {
                    newReportSubstitutions.add(var, value);
                } else {
                    newReportSubstitutions.add(var, value1);
                }
            }

        }

        return newReportSubstitutions;
    }

    // method for the filter substitutions
    public boolean filtertest(Substitutions reportSubstitutions) {
        if (isSubsetOf(reportSubstitutions))
            return true;
        return false;

    }

    public boolean isFreeVariableBound(Node freeVariable) {
        for (Node var : map.keySet()) {
            if (var.getName().equals(freeVariable.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Substitutions union(Substitutions reportSubs, Substitutions reportSubs2) {
        Substitutions unionSubs = new Substitutions();
        unionSubs.map.putAll(reportSubs.map);
        unionSubs.map.putAll(reportSubs2.map);
        return unionSubs;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public static void main(String[] args) {
        Substitutions subs1 = new Substitutions();
    }

    public Map<Node, Node> getMap() {
        return map;
    }

    public void setMap(Map<Node, Node> map) {
        this.map = map;
    }
}
