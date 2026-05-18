package edu.guc.mind_graf.htn;

/**
 * Task — The fundamental unit of work in the HTN planning framework.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Marwa's system, an ActNode represents an action to be performed.
 * Acts can be either primitive (directly executable) or complex (needing
 * further breakdown). However, this distinction is implicit — there is
 * no formal "task" abstraction.
 * 
 * In HTN/SHOP, a Task explicitly captures this distinction:
 *   - Primitive Task  → maps to an Operator (like a primitive ActNode)
 *   - Compound Task   → maps to a set of Methods that decompose it
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * This class formalizes the notion of "what the agent needs to accomplish"
 * as a first-class object. Unlike ActNode (which is a node in the semantic
 * network), Task is a lightweight planning-time abstraction used by the
 * SHOP algorithm to track what remains to be done.
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * - Task("travel", ["A", "B"], false)    → compound: needs decomposition
 * - Task("callTaxi", [], true)           → primitive: directly executable
 * - Task("pay", [], false)               → compound: pay-cash or pay-visa
 * - Task("walkToStop", [], true)          → primitive: directly executable
 */
public class Task {

    /** The name of this task (e.g., "travel", "callTaxi", "pay") */
    private String name;

    /** Arguments for this task (e.g., ["A", "B"] for travel(A,B)) */
    private String[] args;

    /**
     * Whether this task is primitive (directly executable as an Operator)
     * or compound (must be decomposed via a Method).
     * 
     * In Marwa's terms:
     *   - primitive = ActNode that can be executed directly
     *   - compound  = ActNode that needs to be broken down into sub-acts
     */
    private boolean isPrimitive;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Full constructor with name, arguments, and type.
     *
     * @param name        the task name (e.g., "travel")
     * @param args        the task arguments (e.g., ["A", "B"])
     * @param isPrimitive true if this is a primitive (operator-level) task
     */
    public Task(String name, String[] args, boolean isPrimitive) {
        this.name = name;
        this.args = args != null ? args : new String[0];
        this.isPrimitive = isPrimitive;
    }

    /**
     * Convenience constructor for tasks without arguments.
     *
     * @param name        the task name
     * @param isPrimitive true if primitive
     */
    public Task(String name, boolean isPrimitive) {
        this(name, new String[0], isPrimitive);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public String getName() {
        return name;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Human-readable representation for debugging and trace logging.
     * Example: "travel(A, B) [compound]" or "callTaxi [primitive]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (args.length > 0) {
            sb.append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            sb.append(")");
        }
        sb.append(isPrimitive ? " [primitive]" : " [compound]");
        return sb.toString();
    }
}
