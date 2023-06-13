// Final
package context;
import network.Network;
import nodes.Node;
import nodes.PropositionNode;
import set.NodeSet;
import set.PropositionNodeSet;
import java.util.*;
import paths.AndPath;
import paths.ComposePath;
import paths.FUnitPath;
import paths.KPlusPath;
import paths.Path;
import paths.PathTrace;
import relations.Relation;
import set.NodeSet;
import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import components.MyClassCreator;
import context.Context;
import nodes.ActNode;
import nodes.IndividualNode;
import nodes.MolecularType;
import nodes.Node;
import nodes.PropositionNode;
import cables.DownCable;

public class Controller{
    private static String currContext = "default";
    private static ContextSet contextSet = new ContextSet(currContext);
    //private static ArrayList<BitSet> minimalNoGoods = new ArrayList<>();
    private static String conflictingContext = null;
    private static PropositionNodeSet conflictingHyps;
	//Make it  final variable 
	private static Hashtable<Integer, String > attitudeNames = new Hashtable<Integer, String>();
	// set of  set  of ids of the attitudes and changed to final
    private static Hashtable<Integer, ArrayList<Integer>> consistencies = new Hashtable<Integer , ArrayList<Integer>>();
    private static boolean automaticBR = false;
// Some attitudes don't have to be consistent like likes etc so when adding a prop check first this. L , C
	private static Integer[] L;
	private static Integer[] C;
	//bridge rules
	private static Integer[] BR;
    
    //Consistencies list dictatation
    //Add an attitude to the consistencies list
	// Attitude names are created only once in the start and cannot be changed any time else
    public void addAttitude(Integer attitudeName) {
      // If it doesn't exist then add it
      if (!(consistencies.containsKey(attitudeName))) {
        consistencies.put(attitudeName, new ArrayList<Integer>());
    }
    }

	public void setAutomaticBR(boolean automaticBR) {
		Controller.automaticBR = automaticBR;
	}

    //Adding a consistency to an attitude
    public void addConsistency(Integer attitudeName, Integer consistency) {
        //If the atttiude doesn't exist then create a new one and add it there
        if (!(consistencies.containsKey(attitudeName))) {
            addAttitude(attitudeName);
        }
        //Get the arraylist of consistencies for the attitude
        ArrayList<Integer> attitudeConsistencies = consistencies.get(attitudeName);
        //If the attitude exists then add the consistency to the list
        if (attitudeConsistencies != null && !attitudeConsistencies.contains(consistency)) {
            attitudeConsistencies.add(consistency);
        }
    }
	
	public String getPropositionAttitudename(Integer prop,Context c){
		//get the key value from the  attitudeNames hashtable
		System.out.println(c.getPropositionAttitude(prop));
		return attitudeNames.get(c.getPropositionAttitude(prop));
	}

    //Method that returns all consistencies for a partiuclar attitude
  public ArrayList<Integer> getConsistenciesForAttitude(Integer attitudeName) {
    if (consistencies.containsKey(attitudeName)) {
        return consistencies.get(attitudeName);
    }
    return null;
}

    //Checking if a consistency exists for an attitude
    public boolean consistencyExists(Integer attitudeName, Integer consistency) {
        ArrayList<Integer> attitudeConsistencies = consistencies.get(attitudeName);
        if (attitudeConsistencies != null) {
            return attitudeConsistencies.contains(consistency);
        }
        return false;
    }

	public Context createContext(String contextName) {
        // if a context already exists with that name then throw  an error
		if (contextSet.getContext(contextName) != null){
			throw new RuntimeException("Context with name '" + contextName + "' already exists !");
                 }
		Context c = new Context(contextName);
		return contextSet.add(c);
	}

	public static Context getContext(String contextName) {
		//if no context with this name exists then throw an error
		if (contextSet.getContext(contextName) == null){
			throw new RuntimeException("Context with name '" + contextName + "' doesn't exist !");
		}
		return contextSet.getContext(contextName);
	}
	
	public void setCurrentContext(String contextName)
	{
		Context context = contextSet.getContext(contextName);
		if(context == null)
			throw new RuntimeException("Context with name '" + contextName + "' doesn't exist !");
		currContext = contextName; 
	}

	public Context getCurrentContext()
	{
		//Check if  current Context  is not default
		if (currContext == "default"){
			throw new RuntimeException("No current Context is set");
		}
		return contextSet.getContext(currContext);
	}

	public Context createContext(Hashtable<Integer, PropositionNodeSet> attitudes, String name){
		if (contextSet.getContext(name) != null) {
			throw new RuntimeException("Context with name '" + name + "' already exists !");
		}

		Context newContext = new Context(attitudes,name);
		return contextSet.add(newContext);
	}

	public void printContexts() {
    System.out.println("Contexts:");
    for (Context context : contextSet.getContexts().values()) {
        System.out.println(context.getName());
    }
	}

	public static void clearRevision() {
    contextSet = new ContextSet();
    //minimalNoGoods = new Hashtable<>();
    currContext = "default";
}

	// Checks if a contradiction exists if we introduce a proposition in a context's attitude
	public boolean ContradictionExists(Integer att, Context c, PropositionNode p){
	PropositionNodeSet propositionNodeSet = c.getAttitude_propositions(att);
	ArrayList<Integer> consistencies = getConsistenciesForAttitude(att);
	//System.out.println(consistencies == null);
	if (consistencies != null) {
		propositionNodeSet = consistencies_Props(consistencies, c);
	}
	//If cons is true then add the propositions of the cons to a unified props set
	// Go through all the propositionNodeSet and check if getNegation(p) returns true. if so then return true
	for (int prop : PropositionNodeSet.getPropsSafely(propositionNodeSet)) {
		try{
		if (Network.getNodeById(prop).getNegation().equals(p)) {
			return true;
		}
		}
		catch(Exception e){
			continue;
		}
	}
	return false;
	}

	//Helper method to loop through all consistencies and add them to a unified propositionsNodeSet
	public PropositionNodeSet consistencies_Props(ArrayList<Integer> consistencies, Context c)
	{
		PropositionNodeSet propSet = new PropositionNodeSet();
		for (Integer cons : consistencies) {
			//Get the propositionNodeSet for the attitude
			PropositionNodeSet propositionNodeSet = c.getAttitude_propositions(cons);
			//Add the propositionNodeSet to the unified propSet
			addAll(propositionNodeSet,propSet);
		}
		return propSet;
	}

	// Method to add a proposition to a context in a particular attitude 
	// Checks if a contradiction exists if we introduce a proposition in a context's attitude
	// if it exists then set the variables associated with that then call resolve conflicts method to resolve the conflict'
	public void addPropositionToContext(Integer att, Context c, PropositionNode p){
		//Check if the proposition already exists in the context
		if (c.getAttitude_propositions(att).contains(p.getId())) 
			throw new RuntimeException("Proposition already exists in the context");
		//Call the method that changes the required variables once a conflict exist and adds the proposition otherwise
		change_Variables(att,c,p);
	}

	//Check if a contradiction exists if we introduce a proposition in a context's attitude
	//Not finished because of this error
	/*
	 * Exception in thread "main" java.lang.NullPointerException: Cannot invoke "cables.UpCable.getNodeSet()" because the return value of "nodes.Node.getUpCable(String)" is null
        at nodes.Node.getParents(Node.java:71)
        at nodes.Node.getNegation(Node.java:156)
        at context.Controller.change_Variables(Controller.java:212)
        at context.Controller.addPropositionToContext(Controller.java:197)
        at context.Controller.main(Controller.java:513)
		Reason may be because of how propositionNodeSet is created ,  Learn from the previous design
	*/
	public void change_Variables(Integer att, Context c, PropositionNode p){
		//Check if there exists a contradiction
		if(ContradictionExists(att,c,p))
		{
			Integer id = p.getId();
			PropositionNode node = (PropositionNode) Network.getNodeById(id);
		}
	}

	//This method resolves conflict by prompting the user the propositions that caused the conflict 
	//The user is then asked to remove one of them from the context
	//Then the conflicting context is set to default and the conflictingHyps is set to null
	//resolveConflicts();
	public void resolveConflicts(Integer att){
		//Print the conflicting context and the conflictingHyps
		System.out.println("Conflict exists between context " + conflictingContext + " and the following propositions:");
		for (int prop : PropositionNodeSet.getPropsSafely(conflictingHyps)) {
			System.out.println(Network.getNodeById(prop).getName());
		}
		//Prompt the user to remove one of the propositions from the context
		System.out.println("Please remove one of the propositions from the context");
		//Scanner to get the user input
		Scanner sc = new Scanner(System.in);
		//Get the user input
		String input = sc.nextLine();
		//Remove the proposition from the context
		contextSet.getContext(conflictingContext).remove_Prop(att, (PropositionNode) Network.getNodeById(Integer.parseInt(input)));
		//Close scanner
		sc.close();
		//Set the conflicting context to default and the conflictingHyps to null
//		conflictingContext = "default";
//		conflictingHyps = null;
	}

	//A helper method for consistencies_Props to add all the propositions of a propositionNodeSet to a unified propSet
	public PropositionNodeSet addAll(PropositionNodeSet propositionNodeSet, PropositionNodeSet propSet)
	{
		for (int prop : PropositionNodeSet.getPropsSafely(propositionNodeSet)) {
			propSet.add(prop);
		}
		return propSet;
	}

	// This method returns a propositionNodeset of all the propositions that are consistent within this context into one big propSet
	// Current Props => Method => Checks Consistencies => Returns a unified propSet
	public PropositionNodeSet getCons_Props(PropositionNodeSet current_Props, ArrayList<Integer> consistencies, Context c)
	{
		//if consistencies is not null then add the propositions of the cons to a unified props set
		if(consistencies != null)
		{
			//Helper method to loop through all consistencies and add them to a unified propositionsNodeSet
			PropositionNodeSet propSet = consistencies_Props(consistencies, c);
			return propSet;
		}
		return  current_Props;
	}

//	public static void addPropositionNode(Integer att, PropositionNode p, Context c){
//		// Get the propositionNodeSet for the attitude if it exists if not create it 
//		
//		// Add the proposition to the propositionNodeSet
//		propositionNodeSet.add(p);
//	}

	public static boolean isAutomaticBR() {
		return automaticBR;
	}

	public static Context getContextByName(String contextName) {
		return contextSet.getContext(contextName);
	}

	public boolean removeContext(String contextName) {
		return contextSet.remove(contextName);
	}

	public String contextToString(String contextName) {
    Context context = contextSet.getContext(contextName);
    if (context == null) {
        return "Context not found: " + contextName;
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("Context: ").append(context.getName()).append("\n");
    
    // Iterate over the attitudes in the context
    for (Integer attitude : context.getAttitudes()) {
        sb.append("Attitude ").append(attitude).append(": ");
        
        // Retrieve the PropositionNodeSet for the attitude
        PropositionNodeSet propositionNodeSet = context.getAttitude_propositions(attitude);
        
        // Convert the PropositionNodeSet to a string representation
        String propositions = propositionNodeSet != null ? propositionNodeSet.toString() : "Empty";
        
        sb.append(propositions).append("\n");
    }
    
    return sb.toString();
}

//
//	/**
//	 * Checks if some node's addition to a context c introduces a contradiction.
//	 *
//	 * @param node
//	 * @param c
//	 * @param skipCache This is a boolean flag to allow skipping the cache checking
//	 *                  stage. It is useful for testing purposes only, for now.
//	 * @return
//	 * @throws NodeNotFoundInNetworkException
//	 * @throws DuplicatePropositionException
//	 * @throws NotAPropositionNodeException
//	 */
//	//Not finished.
//	public static ArrayList<NodeSet> checkForContradiction(PropositionNode node, Context c, boolean skipCache)
//			throws NodeNotFoundInNetworkException, DuplicatePropositionException, NotAPropositionNodeException {
//
//		if (c.getNames().contains(conflictingContext)) {
//			checkForContradictionCore(node, c, true);
//			return checkForContradictionCore(node, c, false);
//		}
//
//		return checkForContradictionCore(node, c, skipCache);
//	}
    //Create a main method for testing purposes
    public static void main(String[] args) {
    // add some consistencies
    //controller.addConsistency("attitude1", "consistency1");
    //controller.addConsistency("attitude1", "consistency2");
    //controller.addConsistency("attitude2", "consistency3");

    //// check if the consistencies were added correctly
    //ArrayList<String> attitude1Consistencies = controller.getConsistenciesForAttitude("attitude1");
    //ArrayList<String> attitude2Consistencies = controller.getConsistenciesForAttitude("attitude2");

    //System.out.println("Attitude 1 consistencies: " + attitude1Consistencies);
    //System.out.println("Attitude 2 consistencies: " + attitude2Consistencies);
    //// try to add a consistency that already exists
    //controller.addConsistency("attitude1", "consistency1");

    //// check if the consistency was added despite already existing
    //System.out.println("Attitude 1 consistencies: " + attitude1Consistencies);
	//
	
    // create a new Controller instance
    Controller controller = new Controller();
	Network network = new Network();
//	// create a new context
    PropositionNodeSet pns1 = new PropositionNodeSet();
//    Node base1 = Network.createNode("p", "propositionnode");
//    Node base2 = Network.createNode("q", "propositionnode");
//    pns1.add((PropositionNode)base1);
//    pns1.add((PropositionNode)base2);
//    PropositionNodeSet pns2 = new PropositionNodeSet();
//    Node base3 = Network.createNode("w", "propositionnode");
//    Node base4 = Network.createNode("z", "propositionnode");
//    pns2.add((PropositionNode)base3);
//    pns2.add((PropositionNode)base4);
//	Hashtable<Integer, PropositionNodeSet> attitudes = new Hashtable<Integer, PropositionNodeSet>();
//    attitudes.put(1, pns1);
//    attitudes.put(2, pns2);
//	Context context = controller.createContext(attitudes,"testContext");
//	attitudeNames.put(1, "belief");
//	attitudeNames.put(2, "desires");
//	//works !
//	System.out.println(attitudeNames.get(1));
//
//	// Print the initial list of contexts
//	System.out.println("Initial list of contexts:");
//	controller.printContexts();
//
//	// Remove the context with the specified name
//	//fails as its wrong and doesn't exist
//	//boolean removed = controller.removeContext("myContext");
//	//boolean removed = controller.removeContext("testContext");
//
//	//if (removed) {
//    //System.out.println("Context 'myContext' removed successfully.");
//	//} else {
//    //System.out.println("Failed to remove context 'myContext'.");
//	//}
//	//System.out.println("Updated list of contexts:");
//	//controller.printContexts();
//
//	//Testing Context to String method
	
//	System.out.println("=======================");
//	System.out.println("=======================");
//
//	String contextString = controller.contextToString("testContext");
//	System.out.println(contextString);

	//Creating negation prop
    Node zero = Network.createNode("0", "propositionnode");
	// Want to set the arg relation with a node in the down cable set
	Node negationTest = Network.createNode("negationTest", "propositionnode");
	//Create a node set to add negationTest node to it
	NodeSet pns = new NodeSet();
	NodeSet Zero = new NodeSet();
	Zero.add(zero);
	pns.add((PropositionNode)negationTest);
	pns1.add((PropositionNode)negationTest);
	//Creating the relations
	Relation arg = new Relation ("arg", "", Adjustability.NONE, 1);
	Relation min = new Relation ("min", "", Adjustability.NONE, 1);
	Relation max = new Relation ("max", "", Adjustability.NONE, 1);
	//Creating the down cables
	DownCable dc = new DownCable(arg, pns);
	DownCable Min = new DownCable(min, Zero);
	DownCable Max = new DownCable(max, Zero);
	// Create node M1 like example with arg cable to m2. m2 <= negationTest
	// M1 negates negationTest node
	Node M1 = Network.createNode("propositionnode", new DownCableSet(dc, Min, Max));
	Integer M1ID = negationTest.getId();
	System.out.println("M1 Id: " + M1ID);
//	Integer M1Id = M1.getId();
//	System.out.println("M1Id: " + M1Id);
	// Above code generates the negation as in the figure 2.5
	// Below is testing detecting the contradiction.

    // Test the getAttitudeName method
//    try {
//        Integer attitudeName = context.getPropositionAttitude(base2.getId());
//        System.out.println("Attitude Id: " + attitudeName);
//    } catch (RuntimeException e) {
//        System.out.println(e.getMessage());
//    }
//
//	String attitudename = controller.getPropositionAttitudename(base2.getId(), context);
//	// Check if the output matches the expected value
//	if (attitudename.equals("belief")) {
//	    System.out.println("Output is correct: " + attitudename);
//	} else {
//	    System.out.println("Output is incorrect. Expected: belief, Actual: " + attitudename);
//	}

	// Testing SetAutomatic BR method :: Works !
//	System.out.println("Initial value of automaticBR: " + Controller.isAutomaticBR());
//	// Call the setAutomaticBR method to change the value to true
//	controller.setAutomaticBR(true);
//	// Print the updated value of automaticBR
//	System.out.println("Updated value of automaticBR: " + Controller.isAutomaticBR());


	// ======================== TESTING CONTRADICTIONS ======================== \\
	// In this example we add a negated node in the attitudes 2 , and we create a consistency between attitude 1 and 2
	// We then add a node in attitude 1 that contradicts the negated node in attitude 2
	// We then check if the contradiction is detected and it is hence the test is passed !
	// ======================== CONSISTENCIES INCLUDED ======================== \\
    // Create a new context
    //Context context = new Context("ContextName");
    Node base1 = Network.createNode("base1", "propositionnode");
    Node base2 = Network.createNode("base2", "propositionnode");
//    pns1.add((PropositionNode)base1);
//    pns1.add((PropositionNode)base2);
    PropositionNodeSet pns2 = new PropositionNodeSet();
    Node base3 = Network.createNode("base3", "propositionnode");
    Node base4 = Network.createNode("base4", "propositionnode");
    pns2.add((PropositionNode)base3);
	//May cause an issue that you are type casting.
	pns2.add((PropositionNode) negationTest);
	//Print negation testing id
	Integer negationTestId = negationTest.getId();
	System.out.println("NegationTestId: " + negationTestId);
    pns2.add((PropositionNode)base4);
	Hashtable<Integer, PropositionNodeSet> attitudes = new Hashtable<Integer, PropositionNodeSet>();
    attitudes.put(1, pns1);
    attitudes.put(2, pns2);
	Context context = controller.createContext(attitudes,"testContext");
	//add consistency between attitude 1 and 2
	controller.addConsistency(1, 2);

	//Node test = negationTest.getNegation(); if(test == null) {System.out.println("null");} else{
	//if(test.equals(M1)) {System.out.println("true");} else {System.out.println("false");}}
	//Possible issue is the type casting of the variables into prop nodes , Figure out a fix.
//	boolean contradictionExists = controller.ContradictionExists(1, context, (PropositionNode) M1);
//    // Check the result
//    if (contradictionExists) {
//        System.out.println("Contradiction exists!");
//    } else {
//        System.out.println("No contradiction found.");
//    }

	// ======================== TESTING ADDING PROPS ======================== \\
//	controller.addPropositionToContext(2 , context, (PropositionNode) base2);
//
//    // Test the getAttitudeName method
//    try {
//        Integer attitudeName = context.getPropositionAttitude(base2.getId());
//        System.out.println("Attitude Id: " + attitudeName);
//    } catch (RuntimeException e) {
//        System.out.println(e.getMessage());
//    }
	// ======================== TESTING ADDING PROPS W/ CONTRADICTIONS ======================== \\
	controller.addPropositionToContext(2 , context, (PropositionNode) M1);

    // Test the getAttitudeName method
//    try {
//		String conflict = controller.conflictingContext;
//        System.out.println(conflict);
//    } catch (RuntimeException e) {
//        System.out.println(e.getMessage());
//    }

    }
}
