package mgip.ruleIntroduction;

import set.RIISet;

public class MCII {
    RIISet riiSet;
    int RIIsCount;
    boolean isSufficent;

    public MCII() {

    }

    public MCII(RIISet riiSet) {
        this.riiSet = riiSet;
        RIIsCount = 0;
        this.isSufficent = false;
    }

    public void add(RIISet riiSet) {
        riiSet.add(riiSet);
        RIIsCount++;
    }
}
