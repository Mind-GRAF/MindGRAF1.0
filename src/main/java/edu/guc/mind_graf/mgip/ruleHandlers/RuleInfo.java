package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.Map;
import java.util.Objects;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;

public class RuleInfo {

    private int pcount;
    private int ncount;
    private Substitutions subs;
    private FlagNodeSet fns;
    // dont have inference type since it should always be backward, will revise this
    // if needed

    public RuleInfo() {
        pcount = 0;
        ncount = 0;
        subs = new Substitutions();
        fns = new FlagNodeSet();
    }

    public RuleInfo(int pcount, int ncount, Substitutions subs, FlagNodeSet fns) {
        this.pcount = pcount;
        this.ncount = ncount;
        this.subs = subs;
        this.fns = fns;
    }

    public FlagNodeSet getFns() {
        return fns;
    }

    public void setFns(FlagNodeSet fns) {
        this.fns = fns;
    }

    public int getPcount() {
        return pcount;
    }

    public void setPcount(int pcount) {
        this.pcount = pcount;
    }

    public void incrementPcount() {
        this.pcount++;
    }

    public void incrementNcount() {
        this.ncount++;
    }

    public int getNcount() {
        return ncount;
    }

    public void setNcount(int ncount) {
        this.ncount = ncount;
    }

    public Substitutions getSubs() {
        return subs;
    }

    public void setSubs(Substitutions subs) {
        this.subs = subs;
    }

    public boolean isCompatible(RuleInfo r) {
        for (Map.Entry<Node, Node> entry : this.subs.getMap().entrySet()) {
            Node var = entry.getKey();
            Node value = entry.getValue();
            if (r.getSubs().contains(var) && !r.getSubs().get(var).equals(value)) {
                return false;
            }
        }
        return true;
    }

    // not handling the case of different signs (if i got reports of the same
    // substitution I'm assuming it's the same sign; otherwise, BR would've handled
    // it)
    public RuleInfo combine(RuleInfo r) {
        if (!isCompatible(r))
            return null;
        int resPcount = this.pcount + r.getPcount();
        int resNcount = this.ncount + r.getNcount();
        // if disjoint loop wouldn't start so checking if disjoint's useless
        FlagNodeSet intersection = this.fns.intersection(r.getFns());
        // if a node exists in both then it was counted twice, we want to count it once
        for (FlagNode fn : intersection.getFlagNodes()) {
            if (fn.isFlag())
                resPcount--;
            else
                resNcount--;
        }

        Substitutions resSubs = new Substitutions();
        resSubs.addSubs(this.subs);
        resSubs.addSubs(r.getSubs()); // counting on that if the subs are not compatible, the method will not be
                                      // called and that adding overwrites repeated nodes ==> a variable wouldn't
                                      // exist twice in two different nodes
        FlagNodeSet resFns = this.fns.combine(r.getFns());
        RuleInfo result = new RuleInfo(resPcount, resNcount, resSubs, resFns);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RuleInfo ri = (RuleInfo) obj;
        return this.pcount == ri.getPcount() &&
           this.ncount == ri.getNcount() &&
           this.subs.equals(ri.getSubs())
           && this.fns.equals(ri.getFns());
    }

}
