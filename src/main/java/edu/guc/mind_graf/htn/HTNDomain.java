package edu.guc.mind_graf.htn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTNDomain — The knowledge base of what the agent CAN do.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Marwa's system, the agent's capabilities are distributed across the
 * semantic network — ActNodes, RuleNodes, and their channel connections
 * collectively define what the agent can perceive and do. There is no
 * single "domain" object.
 * 
 * In HTN/SHOP, the Domain (D) is a centralized registry of:
 *   - All Operators (primitive actions the agent can perform)
 *   - All Methods  (decomposition rules for compound tasks)
 * 
 * The planner queries the domain to find:
 *   - Which operator matches a given primitive task
 *   - Which methods can decompose a given compound task
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * The domain provides a clean separation between:
 *   - WHAT the agent knows how to do (domain definition)
 *   - HOW the planner searches for a valid plan (HTNPlanner)
 *   - WHAT the current world looks like (WorldState)
 * 
 * Different scenarios (transportation, logistics, robot navigation) are
 * modeled as different HTNDomain instances.
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * Domain contains:
 *   Operators: callTaxi, rideTaxi, payCash, payVisa, walkToStop,
 *              takeBus, walkToDestination
 *   Methods:   travel-by-taxi, travel-by-bus, get-taxi-call,
 *              get-taxi-hail, pay-cash, pay-visa
 */
public class HTNDomain {

    /**
     * Registry of operators, indexed by task name.
     * 
     * A task name may map to multiple operators (e.g., different
     * instantiations), though typically there is one operator per
     * primitive task name.
     */
    private Map<String, List<Operator>> operators;

    /**
     * Registry of methods, indexed by the compound task name they decompose.
     * 
     * A compound task name maps to MULTIPLE methods — these are the
     * alternatives that the SHOP algorithm tries during DFS search.
     * The ORDER of methods determines which alternative is tried first.
     * 
     * Example: "travel" → [travel-by-taxi, travel-by-bus]
     *          "pay"    → [pay-cash, pay-visa]
     */
    private Map<String, List<Method>> methods;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates an empty domain. Operators and methods are added via
     * the addOperator() and addMethod() methods.
     */
    public HTNDomain() {
        this.operators = new HashMap<>();
        this.methods = new HashMap<>();
    }

    // =========================================================================
    // Domain Construction
    // =========================================================================

    /**
     * Registers an operator in this domain.
     * 
     * The operator is indexed by its name, which must match the
     * corresponding primitive Task name.
     *
     * @param operator the operator to register
     */
    public void addOperator(Operator operator) {
        operators.computeIfAbsent(operator.getName(), k -> new ArrayList<>())
                 .add(operator);
    }

    /**
     * Registers a method in this domain.
     * 
     * The method is indexed by its taskName (the compound task it
     * decomposes). Multiple methods for the same task are stored
     * in the order they are added — this order determines the
     * DFS exploration sequence.
     *
     * @param method the method to register
     */
    public void addMethod(Method method) {
        methods.computeIfAbsent(method.getTaskName(), k -> new ArrayList<>())
               .add(method);
    }

    // =========================================================================
    // Planner Queries
    // =========================================================================

    /**
     * Finds the first operator matching the given task name.
     * 
     * Called by the planner when processing a primitive task.
     * Returns null if no operator is registered for this task name.
     *
     * @param taskName the primitive task name to look up
     * @return the matching Operator, or null if not found
     */
    public Operator findOperator(String taskName) {
        List<Operator> ops = operators.get(taskName);
        if (ops != null && !ops.isEmpty()) {
            return ops.get(0);
        }
        return null;
    }

    /**
     * Gets all methods that can decompose the given compound task.
     * 
     * Called by the planner when processing a compound task. The
     * planner iterates through these methods in order, trying each
     * one (DFS). If a method fails, the next one is tried (backtracking).
     *
     * @param taskName the compound task name to look up
     * @return list of applicable methods (may be empty, never null)
     */
    public List<Method> getMethods(String taskName) {
        return methods.getOrDefault(taskName, new ArrayList<>());
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public Map<String, List<Operator>> getOperators() {
        return operators;
    }

    public Map<String, List<Method>> getMethods() {
        return methods;
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Summarizes the domain contents for debugging.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HTNDomain{\n");
        sb.append("  Operators:\n");
        for (Map.Entry<String, List<Operator>> entry : operators.entrySet()) {
            for (Operator op : entry.getValue()) {
                sb.append("    ").append(op).append("\n");
            }
        }
        sb.append("  Methods:\n");
        for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
            for (Method m : entry.getValue()) {
                sb.append("    ").append(m).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
