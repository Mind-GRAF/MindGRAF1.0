package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.Map;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.FreeVariableSet;

public class RuleInfo {

    private String context;
    private int attitude;
    private int pcount;
    private int ncount;
    private Substitutions subs;
    private FlagNodeSet fns;
    // dont have inference type since it should always be backward, will revise this
    // if needed

    public RuleInfo(String context, int attitude) {
        this.context = context;
        this.attitude = attitude;
        pcount = 0;
        ncount = 0;
        subs = new Substitutions();
        fns = new FlagNodeSet();
    }

    public RuleInfo(String context, int attitude, int pcount, int ncount, Substitutions subs, FlagNodeSet fns) {
        this.context = context;
        this.attitude = attitude;
        this.pcount = pcount;
        this.ncount = ncount;
        this.subs = subs;
        this.fns = fns;
    }

    public static RuleInfo createRuleInfo(Report report){
        int pcount = 0;
        int ncount = 0;
        if(report.isSign())
            pcount++;
        else
            ncount++;
        FlagNode reporter = new FlagNode(report.getReporterNode(), report.isSign(), report.getSupport());
        return new RuleInfo(report.getContextName(), report.getAttitude(), pcount, ncount, report.getSubstitutions(), new FlagNodeSet(reporter));
    }

    public boolean isCompatible(RuleInfo r) {
        if(!this.context.equals(r.context) || this.attitude != r.attitude)
            return false;
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
        RuleInfo res = new RuleInfo(r.getContext(), r.getAttitude());
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
        res.pcount = resPcount;
        res.ncount = resNcount;
        res.subs = resSubs;
        res.fns = resFns;
        return res;
    }

    public RuleInfo addNullSubs(FreeVariableSet ns){
        RuleInfo ruleInfoWithNulls = clone();
        for(Node n : ns.getFreeVariables()){
            if(!ruleInfoWithNulls.getSubs().contains(n)){
                ruleInfoWithNulls.getSubs().add(n, null);
            }
        }
        return ruleInfoWithNulls;

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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RuleInfo ri = (RuleInfo) obj;
        return this.context.equals(ri.context) && this.attitude == ri.attitude && this.pcount == ri.pcount &&
           this.ncount == ri.ncount &&
           this.subs.equals(ri.subs)
           && this.fns.equals(ri.fns);
    }

//    public RuleInfo combineAdd(RuleInfo ri) {
//        RuleInfo res = new RuleInfo();
//        res = res.combine(this);
//        res = res.combine(ri);
//        return res;
//    } //combines in new RuleInfo

    @Override
    public String toString() {
        return "RuleInfo{" +
                "context=" + context +
                ", attitude=" + attitude +
                ", pcount=" + pcount +
                ", ncount=" + ncount +
                ", subs=" + subs +
                ", fns=" + fns +
                '}';
    }

    public RuleInfo clone(){
        RuleInfo ri = new RuleInfo(this.context, this.attitude);
        ri.setPcount(this.pcount);
        ri.setNcount(this.ncount);
        ri.setSubs(this.subs.clone());
        ri.setFns(this.fns.clone());
        return ri;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getAttitude() {
        return attitude;
    }

    public void setAttitude(int attitude) {
        this.attitude = attitude;
    }
}
