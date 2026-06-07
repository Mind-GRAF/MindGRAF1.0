package edu.guc.mind_graf.htn;

import java.util.HashSet;
import java.util.Set;

/**
 * WorldState — The planner's mental scratchpad.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Ibrahim's system, the agent's knowledge of the world is stored as
 * "mental attitudes" — specifically beliefs represented through the semantic
 * network (PropositionNode, KnownInstanceSet). For example:
 *   BELIEVE(hasCash(5))
 *   BELIEVE(at(A))
 *   BELIEVE(distance(A,B,10))
 * 
 * In HTN/SHOP, the WorldState is a simplified, flat representation of these
 * beliefs as a set of ground propositions (strings). This is the standard
 * STRIPS representation used by classical planners.
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * The WorldState serves as a SIMULATION of the world during planning. The
 * planner never modifies the actual semantic network — it works on copies
 * of the WorldState. When an operator is "applied" during planning, the
 * WorldState is updated (add-list/delete-list) to reflect what the world
 * WOULD look like if that operator were executed.
 * 
 * This is critical for backtracking: when a plan branch fails, the planner
 * discards the modified WorldState and tries the next alternative with
 * the original state.
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * Initial state:
 *   {"at(A)", "hasCash(5)", "fare(10)", "visaBalance(0)",
 *    "busTicket", "taxiAvailable"}
 * 
 * After applying operator "callTaxi":
 *   {"at(A)", "hasCash(5)", "fare(10)", "visaBalance(0)",
 *    "busTicket", "taxiAvailable", "taxiCalled"}
 * 
 * After applying operator "walkToStop":
 *   {"hasCash(5)", "fare(10)", "visaBalance(0)",
 *    "busTicket", "taxiAvailable", "atBusStop"}
 *   (note: "at(A)" removed, "atBusStop" added)
 */
public class WorldState {

    /**
     * The set of propositions that are currently true in this world state.
     * 
     * Each proposition is a simple string (ground atom) like:
     *   "at(A)", "hasCash(5)", "taxiAvailable", "busTicket"
     * 
     * In Ibrahim's system, these would be beliefs stored in the semantic
     * network. Here, we use a flat set for efficient planner operations.
     */
    private Set<String> propositions;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Creates a new WorldState with the given set of propositions.
     *
     * @param propositions the initial set of true propositions
     */
    public WorldState(Set<String> propositions) {
        this.propositions = new HashSet<>(propositions);
    }

    /**
     * Creates an empty WorldState.
     */
    public WorldState() {
        this.propositions = new HashSet<>();
    }

    // =========================================================================
    // Core Logic
    // =========================================================================

    /**
     * Checks if a single proposition holds (is true) in this state.
     *
     * @param proposition the proposition to check
     * @return true if the proposition is in the current state
     */
    public boolean holds(String proposition) {
        return propositions.contains(proposition);
    }

    /**
     * Checks if ALL propositions in the given set hold in this state.
     * 
     * This is used by Operator.isApplicable() to verify that all
     * preconditions of an operator are satisfied before it can be applied.
     * 
     * An empty precondition set always returns true (an operator with
     * no preconditions is always applicable — like "walkToStop").
     *
     * @param conditions the set of propositions to check
     * @return true if every proposition in conditions is in this state
     */
    public boolean holdsAll(Set<String> conditions) {
        return propositions.containsAll(conditions);
    }

    /**
     * Creates an independent copy of this WorldState.
     * 
     * This is ESSENTIAL for the SHOP algorithm's backtracking:
     * before applying an operator, the planner passes a copy of the state
     * down the recursion. If the branch fails, the original state is
     * untouched and available for the next alternative.
     *
     * @return a new WorldState with the same propositions
     */
    public WorldState copy() {
        return new WorldState(new HashSet<>(this.propositions));
    }

    /**
     * Applies an operator's effects to this state, returning a NEW state.
     * 
     * Follows the STRIPS state transition:
     *   newState = (currentState - deleteList) ∪ addList
     * 
     * The current state is NOT modified — a new state is returned.
     * This preserves immutability for safe backtracking.
     *
     * @param operator the operator whose effects to apply
     * @return a new WorldState reflecting the operator's effects
     */
    public WorldState applyOperator(Operator operator) {
        // Create a copy to preserve the current state (for backtracking)
        WorldState newState = this.copy();

        // STRIPS delete: remove propositions that become false
        newState.propositions.removeAll(operator.getDeleteList());

        // STRIPS add: add propositions that become true
        newState.propositions.addAll(operator.getAddList());

        return newState;
    }

    /**
     * Adds a proposition to this state.
     * Used during state initialization.
     *
     * @param proposition the proposition to add
     */
    public void addProposition(String proposition) {
        propositions.add(proposition);
    }

    /**
     * Removes a proposition from this state.
     *
     * @param proposition the proposition to remove
     */
    public void removeProposition(String proposition) {
        propositions.remove(proposition);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public Set<String> getPropositions() {
        return propositions;
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Human-readable snapshot of the current world state.
     */
    @Override
    public String toString() {
        return "WorldState" + propositions;
    }
}
