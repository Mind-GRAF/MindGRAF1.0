package mindG.mgip.matching;

import mindG.network.Node;

public class Substitutions {

    public Substitutions() {

    }

    public Substitutions(Substitutions Substitutions) {
    }

    public static boolean isSubSet(Substitutions variableSubstitutions, Substitutions filterSubstitutions) {
        return false;
    }

    // method for the switch substitutions
    public static Substitutions switchReport(Substitutions reportSubstitutions, Substitutions switchSubstitutions) {
        Substitutions newReportSubstitutions = new Substitutions();
        // method men ali add binding we a3mel el new substitution biha
        return newReportSubstitutions;
    }

    // method for the filter substitutions
    public static boolean filtertest(Substitutions reportSubstitutions, Substitutions filterSubstitutions) {
        if (Substitutions.isSubSet(reportSubstitutions, filterSubstitutions))
            return true;
        return false;

    }

    public boolean isFreeVariableBound(Node freeVariable) {
        return false;
    }

    public Substitutions union(Substitutions reportSubs, Substitutions reportSubs2) {
        return null;
    }
}
