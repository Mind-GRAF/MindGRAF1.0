package set;

import java.util.HashSet;
import java.util.Set;
import mgip.ruleIntroduction.MCII;

public class MCIISet {
    private Set<MCII> mciiSet;

    public MCIISet() {
        mciiSet = new HashSet<>();
    }

    public void addMCII(MCII mcii) {
        mciiSet.add(mcii);
    }

    public void removeMCII(MCII mcii) {
        mciiSet.remove(mcii);
    }

    public Set<MCII> getMCIIs() {
        return mciiSet;
    }
}

