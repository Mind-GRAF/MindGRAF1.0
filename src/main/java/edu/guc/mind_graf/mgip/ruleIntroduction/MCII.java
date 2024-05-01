package edu.guc.mind_graf.mgip.ruleIntroduction;

import java.util.ArrayList;

public class MCII {
    ArrayList<RII> riiList; //Each RII object represents a context
    int posRII;
    int negRII;
    int RIICount;
    boolean isSufficient;

    public MCII() {
        riiList = new ArrayList<>();
        RIICount = 0;
        posRII = 0;
        negRII = 0;
        isSufficient = false;
    }

    public void addRII(RII rii) {
        riiList.add(rii);
        RIICount++;
    }

    public boolean isSufficient() {
        return isSufficient;
    }

    public void setSufficent() {
        isSufficient = true;
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

    public void addPosRII() {
        posRII++;
    }

    public void addNegRII() {
        negRII++;
    }

    public int getPosRII() {
        return posRII;
    }

    public int getNegRII() {
        return negRII;
    }

    public int getExpectedReportsCount(){
        return RIICount*this.getRii(0).getConqArgNodes().size();
    }


    public String toString() {
        String str = "[";
        for (RII rii : riiList) {
            str += rii.getContext().getContextName()+", ";
        }
        str+="]";
        return str;
    }
}
