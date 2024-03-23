package mgip.ruleHandlers;

import components.Substitutions;

public class ruleInfo {

    private int pcount;
    private int ncount;
    private Substitutions subs;

    // ArrayList<Report> reports;
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

    public boolean isCompatible(ruleInfo r) {
        
        return true;
    }

    public ruleInfo combine(ruleInfo r) {
        //needs editing
        this.pcount += r.getPcount();
        this.ncount += r.getNcount();
        return this;
    }

}
