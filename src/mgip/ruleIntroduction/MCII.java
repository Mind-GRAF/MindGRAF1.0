package mgip.ruleIntroduction;

public class MCII {
    private RII rii;
    private int RIIsSetCount;
    private boolean isSufficent;

    public MCII() {
        rii = null;
        RIIsSetCount = 0;
        isSufficent = false;
    }

    public void add(RII rii) {
        RIIsSetCount +=1;
    }

    public void setSufficent() {
        this.isSufficent = true;
    }

    public boolean isSufficent() {
        return this.isSufficent;
    }

    public int getRIIsSetCount() {
        return this.RIIsSetCount;
    }

    public RII getRII() {
        return this.rii;
    }

}
