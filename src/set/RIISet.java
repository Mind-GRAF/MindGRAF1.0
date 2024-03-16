package set;

import java.util.HashSet;
import java.util.Set;
import mgip.ruleIntroduction.RII;

public class RIISet {
    private Set<RII> riiSet;

    public RIISet() {
        riiSet = new HashSet<>();
    }

    public void addRII(RII rii) {
        riiSet.add(rii);
    }

    public void removeRII(RII rii) {
        riiSet.remove(rii);
    }

    public Set<RII> getRIIs() {
        return riiSet;
    }

}
