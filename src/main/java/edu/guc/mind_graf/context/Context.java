package edu.guc.mind_graf.context;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;

public class Context implements Cloneable{

    private Hashtable<Integer, PropositionNodeSet[]> attitudes;
    private String name;
    private Set<Integer, BitSet> AttitudesBitset;

    
    public Context(String name, Set<String,Integer> attitudeNames){
        this.name=name;
        for(Map.Entry<String,Integer> entry: attitudeNames.getSet().entrySet()){
            PropositionNodeSet[] attitudeNodeSet = new PropositionNodeSet[2];
            attitudeNodeSet[0] = new PropositionNodeSet();
            attitudeNodeSet[1] = new PropositionNodeSet();
            
            attitudes.put(entry.getValue(),  attitudeNodeSet);
        }
    }
    
//    public Context copyContext(Context c, int attitudeId, PropositionNodeSet NodeSet) {
//        Context newContext = c.clone();
//        newContext.attitudes.get(attitudeId).union(NodeSet);
//        //TODO check consistency
//        return newContext;
//    }
    
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
    
    public static void addHypothesisToContext(Context c, int attitudeNumber, PropositionNode node) {
        c.attitudes.get(attitudeNumber)[0].add(node);
    }
    
    public static void removeHypothesisFromContext(Context c, int attitudeNumber, PropositionNode node) {
        c.attitudes.get(attitudeNumber)[0]  .remove(node);
    }
    
//    @Override
//    public Context clone() {
//        Context clone = new Context(this.name+" copy",ContextController.getAttitudes() );
//        for( Map.Entry<Integer,PropositionNodeSet> entry: this.attitudes.entrySet()){
//                clone.attitudes.put(entry.getKey(), entry.getValue().clone());
//        }
//        //TODO wael: clone bitsets
//        return clone;
//    }
    
}
