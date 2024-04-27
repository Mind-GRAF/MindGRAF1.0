package edu.guc.mind_graf.context;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;


public class Context {

    private Hashtable<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> attitudes;
    private String name;
    private Set<Integer, BitSet> AttitudesBitset;

    
    public Context(String name, Set<String,Integer> attitudeNames){
        this.name=name;
        //TODO Wael: set the attitude names
        for(Map.Entry<String,Integer> entry: attitudeNames.getSet().entrySet()){
            PropositionNodeSet[] attitudeNodeSet = new PropositionNodeSet[2];
            attitudeNodeSet[0] = new PropositionNodeSet();
            attitudeNodeSet[1] = new PropositionNodeSet();

            attitudes.put(entry.getValue(),  attitudeNodeSet);
        }
    }
    public Integer getPropositionAttitude(Integer prop) {

        // loop through all the Integer keys of attitudesBitset
        for (Integer key : this.AttitudesBitset.getSet().keySet()) {
            // If the propositionNode is in the attitude then return the name of the
            // attitude
            if (this.AttitudesBitset.get(key).get(prop)) {
                return key;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");
    }
    
    public String getName() {
        return name;
    }
    
//    public PropositionNodeSet getAllPropositionsInAnAttitude(int attitude) {
//        return this.attitudes.get(attitude);
//    }

    public static void addHypothesisToContext(Context c, int attitudeNumber, PropositionNode node) {
        c.attitudes.get(attitudeNumber)[0].add(node);
    }

    public static void removeHypothesisFromContext(Context c, int attitudeNumber, PropositionNode node) {
        c.attitudes.get(attitudeNumber)[0]  .remove(node);
    }

    public boolean isHypothesis(int attitudeNumber, PropositionNode node){
        return this.attitudes.get(attitudeNumber).getFirst().contains(node) || this.attitudes.get(attitudeNumber).getSecond().contains(node);
    }


}
