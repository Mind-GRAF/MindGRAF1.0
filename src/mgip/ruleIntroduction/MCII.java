package mgip.ruleIntroduction;

import java.util.ArrayList;

public class MCII {
    ArrayList<RII> riiList; //Each RII object represents a context
    int RIICount;
    boolean isSufficent;

    public MCII() {
        riiList = new ArrayList<>();
        RIICount = 0;
        isSufficent = false;
    }

    public void addRII(RII rii) {
        riiList.add(rii);
        RIICount++;
    }

    public boolean getSufficent() {
        return isSufficent;
    }

    public void setSufficent() {
        isSufficent = true;
    }

    public ArrayList<RII> getRIIList() {
        return riiList;
    }

    public int getRIICount() {
        return RIICount;
    }

    public RII getRii(int index){
        return riiList.get(index);
    }
}