//create method  that checks for conflicts  in a given context
package context;
import network.Network;
import nodes.Node;
import nodes.PropositionNode;
import set.PropositionNodeSet;
import java.io.Serializable;
import java.util.*;

public class Context implements Serializable{
    
    // Attributes
    private Hashtable< Integer, PropositionNodeSet> attitudes;
    private String name;
    private Hashtable<Integer , BitSet> AttitudesBitset;
	//add brdige rules
	
	// Constructor
	protected Context(String name){
	this.name = name;
	this.attitudes = new Hashtable<Integer, PropositionNodeSet>();
	this.AttitudesBitset = new Hashtable<Integer, BitSet>();
	}

	//Consturctor with an attitude
	//Double check the size of attiude keys is consistent with its names
	protected Context(Hashtable<Integer, PropositionNodeSet> attitudes, String name) {
	this.name = name;
	this.attitudes = attitudes;
	this.AttitudesBitset = new Hashtable<Integer, BitSet>();
	//for each String in Hashtable attitudes, create a new BitSet representing the propositions in the PropositionNodeSet
	for (Integer key : attitudes.keySet()){
    PropositionNodeSet current_props = this.attitudes.get(key);
		BitSet bitset = new BitSet();
    if(current_props != null)
    {
    //Need to set the exception handling below to shut this off
		int[] arr = PropositionNodeSet.getPropsSafely(current_props);
    //Set bitset of the current attitude to true for each proposition in the PropositionNodeSet
    for (int i = 0; i < arr.length; i++){
            bitset.set(arr[i]);
        }
    //add bitset then rest its value
    this.AttitudesBitset.put(key, bitset);
    bitset = new BitSet();

    }
    else{
        //set bitset to null if the PropositionNodeSet is null
        this.AttitudesBitset.put(key, null);
        continue;
      }

	}
	}
	//Methods
	
	//Retrieve PropositionNodeSet of an attitude
	protected PropositionNodeSet getAttitude_propositions(Integer attitude){
		return this.attitudes.get(attitude);
	}

	protected boolean hasAttitude(Integer attitude)
	{
		return this.attitudes.containsKey(attitude);
	}

	//Check if the propositionNode is already in the PropositionNodeSet
	//If so add it
	protected void checkProp_Inattitude(Integer att, PropositionNode p){
			if (!this.attitudes.get(att).contains(p)){
				this.attitudes.get(att).add(p);
				// Add to Bitset as well
				AdjustBitset(att, p);
			}
			else{
				throw new RuntimeException("PropositionNode already exists in the attitude");
			}
	}
	
	//Check if the attitude exists if so 
	//Check if it exists in the attitude's propSet.
	//if not then add the attitude to the context and then add to it the propositionNode
	protected void addProposition(Integer att, PropositionNode p){
			if(hasAttitude(att)){
				checkProp_Inattitude(att, p);
				return;
			}
			//If the attitude does not exist, create a new PropositionNodeSet and add the propositionNode to it
			// Then add with the attitude to the context
			PropositionNodeSet new_propositions = new PropositionNodeSet();
			new_propositions.add(p);

			// Add to Bitset as well
			AdjustBitset(att, p);

			this.attitudes.put(att, new_propositions);
	}

	// Adds to attitudesBitset values 
	protected void AdjustBitset(Integer att, PropositionNode p){
		//Get the current bitset of the attitude
		BitSet bitset = this.AttitudesBitset.get(att);
		if (bitset == null){
			bitset = new BitSet();
		}
		//Set the bitset of the propositionNode to true
		bitset.set(p.getId());
		//Add the bitset to the attitudesBitset
		this.AttitudesBitset.put(att, bitset);
	}

	//Create a method that returns which attitude a propositionNode belongs to // Create one that returns a set
	public Integer getPropositionAttitude(Integer prop){
		//loop through all the Integer keys of attitudesBitset
		for (Integer key : this.AttitudesBitset.keySet()){
			//If the propositionNode is in the attitude then return the name of the attitude
			if (this.AttitudesBitset.get(key).get(prop)){
				return key;
			}
		}
		throw new RuntimeException("PropositionNode is not in any attitude");
	}

	// Method that removes a proposition from a given attitude given 
	// Check if the prop is in that attitude if not return an appropriate error message
	// Uses is supported method to check if the proposition is supported in every given attitude
	protected boolean remove_Prop(Integer att, PropositionNode p){
		//Check if the attitude exists
		if(isSupported(p,att)){
		//remove it from the attitude
		this.attitudes.get(att).remove(p);
		return true;
		}
		//Not removed if the propositionNode is not in the attitude
		return false;
	}

	//return all keys of attitudes hashtable in a list
	protected ArrayList<Integer> getAttitudes(){
		ArrayList<Integer> attitudes = new ArrayList<Integer>();
		for (Integer key : this.attitudes.keySet()){
			attitudes.add(key);
		}
		return attitudes;
	}

	//retrive the name of the Context
	protected String getName(){
		return this.name;
	}

	protected Hashtable<Integer, PropositionNodeSet> getAttitudesHashtable(){
		return this.attitudes;
	}

	    /**
     * Checks if a propositions is asserted in this Attitude
     *
     * @param p the proposition to be checked for assertion.
     * @return <code>true</code> if the proposition exists, otherwise <code>false</code>
     * @throws NotAPropositionNodeException   If the node p is not a proposition.
     * @throws NodeNotFoundInNetworkException If the node p doesn't exist in the network.
     */
	
	// Case where something implies another , has to be also checked 
	// Takes a prop and an attitude and then it checks if the props is in this attitude or not. 
    public boolean isSupported(PropositionNode node, Integer att) {
		//Check if the node exists in the attitude or not
		if (this.attitudes.get(att).contains(node)){
			return true;
		}
		//Else doesn't exist
        return false;
    }

	/*
	* Returns a list of all proposition nodes that are asserted in this context given attitude
	* @param att the attitude to be checked for assertion
	* @return a list of all proposition nodes that are asserted in this context given attitude
	// Support Needed
	public PropositionNodeSet allAsserted(Integer att){
        Collection<PropositionNode> allPropositionNodes = Network.getPropositionNodes().values();
        PropositionNodeSet asserted = new PropositionNodeSet();
		PropositionNodeSet props = this.attitudes.get(att);
        int[] hyps;
        hyps = PropositionNodeSet.getPropsSafely(props);
        for (PropositionNode node : allPropositionNodes) {
            if (Arrays.binarySearch(hyps, node.getId()) > 0) {
                asserted.add(node.getId());
            } else if (isSupported(node,att)) {
                asserted.add(node.getId());
            }
        }

        return asserted;
    }
	*/

	public Hashtable<Integer , BitSet> getAttitudesBitset(){
		return this.AttitudesBitset;
	}


  // Create the main class to test some stuff 

  public static void main(String[] args) {

	// Initialize Network :: Important for all tests
	Network network = new Network();
	// Testing Context(name) constructor :: Works
	/*
	Context context = new Context("Test");
    System.out.println("Context name: " + context.getName());
    System.out.println("Attitudes: " + context.getAttitudes());
    System.out.println("Attitudes Bitset: " + context.getAttitudesBitset());
	
	*/

	// Testing Context(attitudes, name) constructor :: Works
	
        // create some PropositionNodeSets
        //PropositionNodeSet pns1 = new PropositionNodeSet();
        //Node base1 = Network.createNode("base1", "propositionnode");
        //Node base2 = Network.createNode("base2", "propositionnode");
        //pns1.add((PropositionNode)base1);
        //pns1.add((PropositionNode)base2);
        //PropositionNodeSet pns2 = new PropositionNodeSet();
        //Node base3 = Network.createNode("base3", "propositionnode");
        //Node base4 = Network.createNode("base4", "propositionnode");
        //pns2.add((PropositionNode)base3);
        //pns2.add((PropositionNode)base4);
        //
        //// create Hashtable with the PropositionNodeSets
        //Hashtable<Integer, PropositionNodeSet> attitudes = new Hashtable<Integer, PropositionNodeSet>();
        //attitudes.put(1, pns1);
        //attitudes.put(2, pns2);
        
        // create a new Context object
        //Context context = new Context(attitudes, "myContext");

		//Testing getAttitude_propositions(attitude) :: Works !
        //PropositionNodeSet attitude1Props = context.getAttitude_propositions(2);
        //System.out.println("Propositions in Attitude 1: " + attitude1Props);

		//Testing getAttitudes() :: Works !
		//ArrayList<Integer> attitudesList = context.getAttitudes();
		//System.out.println("Attitudes: " + attitudesList);
		
		//Testing getName() :: Works !
		
		//Testing getAttitudesBitset :: Works !
		//Hashtable<Integer, BitSet> attitudesBitset = context.getAttitudesBitset();
		//System.out.println(context.getAttitudesBitset().get(2));
        
	/*
        // print out the result
        System.out.println(context.name);
        for (Integer key : context.attitudes.keySet()) {
            System.out.println("Attitude " + key + ": " + context.attitudes.get(key));
            System.out.println("Bitset for Attitude " + key + ": " + context.AttitudesBitset.get(key));
        }
	*/

    PropositionNodeSet pns1 = new PropositionNodeSet();
    Node base1 = Network.createNode("base1", "propositionnode");
    Node base2 = Network.createNode("base2", "propositionnode");
    pns1.add((PropositionNode)base1);
    //pns1.add((PropositionNode)base2);
    PropositionNodeSet pns2 = new PropositionNodeSet();
    Node base3 = Network.createNode("base3", "propositionnode");
    Node base4 = Network.createNode("base4", "propositionnode");
    pns2.add((PropositionNode)base3);
    pns2.add((PropositionNode)base4);
	Hashtable<Integer, PropositionNodeSet> attitudes = new Hashtable<Integer, PropositionNodeSet>();
    attitudes.put(1, pns1);
    attitudes.put(2, pns2);
    Context context = new Context(attitudes, "myContext");
//	// ============= Testing adding props to attitudes ============= \\
//	Integer test_attitude = 3;
//	context.addProposition(test_attitude, (PropositionNode) base2);
//
//
//    // Test the getAttitudeName method
//    try {
//        Integer attitudeName = context.getPropositionAttitude(base2.getId());
//        System.out.println("Attitude Id: " + attitudeName);
//    } catch (RuntimeException e) {
//        System.out.println(e.getMessage());
//    }

	// ============= Testing removing props from attitudes ============= \\
//	boolean removed = context.remove_Prop(2, (PropositionNode) base3);
//
//	if(removed)
//		System.out.println("Proposition removed successfully");
//	else
//		System.out.println("Proposition not removed successfully");

    } 
}
