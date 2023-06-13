package context; 

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;

public class ContextSet implements Serializable {

	private Hashtable<String, Context> contexts;

	//Empty Constructor :: Works !
    public ContextSet() {
        contexts = new Hashtable<String, Context>();
    }

	//Constructor with name of an empty Context :: Works !
    public ContextSet(String name) {
        this();
        this.contexts.put(name, new Context(name));
    }

	// Constructor with a Context :: Works !
	public ContextSet(Context context) {
        this();
        this.add(context);
    }

	//Returns a Context Object given its name. :: Works
	//When using this method make sure to null check the return value
    public Context getContext(String name) {
		// check if the name doesn't exist in the ContextSet and return runtime exception
        return contexts.get(name);
    }

	// given a Context name it removes it from the ContextSet :: Works !
    public boolean remove(String name) {
		// if the name doesn't exist in the ContextSet return false
		if (!contexts.containsKey(name)) {
			return false;
		}
        return contexts.remove(name) != null;
    }

    //Add a context into the ContextSet using its name as the key. :: Works !
    public Context add(Context c) {
		// check if the name already exists in the ContextSet and return runtime exception
		if (contexts.containsKey(c.getName())) {
			throw new RuntimeException("Context " + c.getName() + " already exists");
		}
        contexts.put(c.getName(), c);
    return c;
    }

	// Works !!
	public Hashtable<String, Context> getContexts() {
    return this.contexts;
}


	public static void main(String[] args) {

	// Testing ContextSet() constructor :: Works !
    //ContextSet contextSet = new ContextSet();
    //Hashtable<String, Context> contexts = contextSet.getContexts();

//    if (contexts.isEmpty()) {
//        System.out.println("ContextSet() constructor test passed");
//    } else {
//        System.out.println("ContextSet() constructor test failed");
//    }
//
	// Testing Context(String name) :: Works !
	    // Create a new ContextSet with the name "MyContextSet"
    ContextSet contextSet = new ContextSet("MyContextSet");
    
    // Get the context with name "MyContextSet" from the contextSet
    Context context = contextSet.getContext("MyContextSet");

	//Create a context with the name "TestContext"
	Context testContext = new Context("TestContext");

	// Add the context "TestContext" to the contextSet
	contextSet.add(testContext);
	//Context retrivedContext = contextSet.getContext("TestContext");	

	//if (retrivedContext == null || !retrivedContext.equals(testContext)) {
    //    System.out.println("Error: context not added or retrieved correctly");
    //} else {
    //    System.out.println("Context added and retrieved successfully");
    //}

	boolean removed = contextSet.remove("TestContext");
    System.out.println(removed); // should print true
								 
	removed = contextSet.remove("TestContext");
    System.out.println(removed); // should print true
    
    // Print the name of the context
	// System.out.println(context.getName()); // Output: "MyContextSet"

	}


}
