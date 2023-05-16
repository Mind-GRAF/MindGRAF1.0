package mindG.mgip.matching;

import java.util.HashMap;
import java.util.Map;

import mindG.network.Node;

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

    // method for the switch substitutions
    public static Substitutions switchReport(Substitutions reportSubstitutions, Substitutions switchSubstitutions) {
        Substitutions newReportSubstitutions = new Substitutions();
        // method men ali add binding we a3mel el new substitution biha
        return newReportSubstitutions;
    }

    // method for the filter substitutions
    public boolean filtertest(Substitutions filterSubstitutions) {
        if (isSubsetOf(filterSubstitutions))
            return true;
        return false;

    }

    public boolean isFreeVariableBound(Node freeVariable) {
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
}
