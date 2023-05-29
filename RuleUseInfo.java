package inferenceRules;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

//import java.util.Set;
//
import javax.naming.Binding;

import components.Support;
import matching.Substitutions;
import set.PropositionSet;
//
public class RuleUseInfo {
    private PropositionSet support;
   private int pos;
   protected Substitutions sub;
    private int neg;
   private FlagNodeSet fns;
    private InferenceTypes type;    
    public RuleUseInfo(PropositionSet Support, int pos, int neg,FlagNodeSet fns,Substitutions sub) {
        this.pos = pos;
        this.neg = neg;
        this.fns = fns;
        this.type = type;
        this.sub=sub;
    }
 
    public RuleUseInfo(Substitutions substitutions, int i, int j, FlagNodeSet fns2) {
    	  this.pos = pos;
          this.neg = neg;
          this.fns = fns;
          this.type = type;	}

	public int getPositiveCount() {
        return pos;
    }
    
    public int getNegativeCount() {
        return neg;
   }
    
  public FlagNodeSet getFlagNodeSet() {
       return fns;
   }
   
    public InferenceTypes getInferenceType() {
        return type;
    }

	public RuleUseInfo combine(RuleUseInfo tRui) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropositionSet getsupport() {
		return support;
	}


	public Substitutions getSubstitutions() {
		return sub;
	}

	public ArrayList<Integer> getBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSub(Substitutions sub) {
		this.sub = sub;
	}

	
    

}

