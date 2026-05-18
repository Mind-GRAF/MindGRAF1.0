package edu.guc.mind_graf.htn;

import java.util.HashSet;
import java.util.Set;

/**
 * Operator — A primitive action with STRIPS-style preconditions and effects.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Marwa's system, a primitive ActNode represents an action that can be
 * executed directly. The ActNode has implicit preconditions (checked via
 * channels and inference) and implicit effects (state changes after execution).
 * 
 * In HTN/SHOP, the Operator makes these EXPLICIT using the STRIPS formalism:
 *   - Preconditions: a set of propositions that MUST hold in the current
 *     WorldState for the operator to be applicable.
 *   - Add-list: propositions that become TRUE after the operator executes.
 *   - Delete-list: propositions that become FALSE after the operator executes.
 * 
 * This is the standard representation from:
 *   "operator represents primitive task with preconditions and effects"
 *   (Report PDF, Section on SHOP operators)
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * Unlike Marwa's ActNode where effects are handled implicitly through the
 * channel/report system, the Operator provides a clean, declarative way to
 * specify how the world changes. This enables the planner to SIMULATE
 * execution without actually running anything — which is the key to planning.
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * Operator: "callTaxi"
 *   preconditions: {taxiAvailable}
 *   add-list:      {taxiCalled}
 *   delete-list:   {}
 * 
 * Operator: "payCash"
 *   preconditions: {hasCash(sufficient)}
 *   add-list:      {paid}
 *   delete-list:   {hasCash(sufficient)}
 * 
 * Operator: "walkToStop"
 *   preconditions: {}             ← always applicable
 *   add-list:      {atBusStop}
 *   delete-list:   {at(origin)}
 */
public class Operator {

    /** The name of this operator (matches the primitive task name) */
    private String name;

    /**
     * Preconditions: propositions that MUST all be present in the WorldState
     * for this operator to be applicable.
     * 
     * In Marwa's terms: these are the conditions checked by the inference
     * engine before an ActNode can fire.
     */
    private Set<String> preconditions;

    /**
     * Add-list (STRIPS): propositions that become true after execution.
     * 
     * In Marwa's terms: these are the positive effects of executing an act.
     * E.g., after "rideTaxi", the agent is now at the destination.
     */
    private Set<String> addList;

    /**
     * Delete-list (STRIPS): propositions that become false after execution.
     * 
     * In Marwa's terms: these are things that are no longer true after the
     * act. E.g., after "rideTaxi", the agent is no longer at the origin.
     */
    private Set<String> deleteList;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a new Operator with explicit preconditions and STRIPS effects.
     *
     * @param name          the operator name (must match the primitive Task name)
     * @param preconditions propositions required to be true in the current state
     * @param addList       propositions to add to the state after execution
     * @param deleteList    propositions to remove from the state after execution
     */
    public Operator(String name, Set<String> preconditions,
                    Set<String> addList, Set<String> deleteList) {
        this.name = name;
        this.preconditions = preconditions != null ? preconditions : new HashSet<>();
        this.addList = addList != null ? addList : new HashSet<>();
        this.deleteList = deleteList != null ? deleteList : new HashSet<>();
    }

    // =========================================================================
    // Core Logic
    // =========================================================================

    /**
     * Checks whether this operator is applicable in the given world state.
     * 
     * An operator is applicable if ALL its preconditions are satisfied
     * (i.e., all precondition propositions exist in the current state).
     * 
     * This is the key gate that drives backtracking in SHOP:
     * if an operator is NOT applicable, the planner returns FAIL and
     * tries the next alternative.
     * 
     * In Marwa's system, this is analogous to the precondition check
     * performed via channels before an ActNode can execute.
     *
     * @param state the current world state
     * @return true if all preconditions hold
     */
    public boolean isApplicable(WorldState state) {
        return state.holdsAll(preconditions);
    }

    /**
     * Applies this operator to the given state, producing a NEW state.
     * 
     * The state transition follows the STRIPS model:
     *   newState = (oldState - deleteList) ∪ addList
     * 
     * IMPORTANT: This creates a COPY of the state. The original state is
     * preserved for backtracking purposes. This is critical for DFS search
     * in the SHOP algorithm — when a branch fails, we need to restore
     * the state to before the failed operator was applied.
     *
     * @param state the current world state (not modified)
     * @return a new WorldState reflecting the effects of this operator
     */
    public WorldState apply(WorldState state) {
        return state.applyOperator(this);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public String getName() {
        return name;
    }

    public Set<String> getPreconditions() {
        return preconditions;
    }

    public Set<String> getAddList() {
        return addList;
    }

    public Set<String> getDeleteList() {
        return deleteList;
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Human-readable representation for plan output and debugging.
     */
    @Override
    public String toString() {
        return "Operator{" + name
                + ", pre=" + preconditions
                + ", add=" + addList
                + ", del=" + deleteList + "}";
    }
}
