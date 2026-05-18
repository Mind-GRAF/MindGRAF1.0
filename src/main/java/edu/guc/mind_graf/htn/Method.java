package edu.guc.mind_graf.htn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Method — A decomposition rule for compound tasks.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Marwa's system, complex ActNodes can "trigger" or "chain" to other
 * acts, but this relationship is implicit — there is no formal decomposition
 * mechanism. The system does not distinguish between "this act breaks down
 * into sub-acts" and "this act leads to another act."
 * 
 * In HTN/SHOP, the Method is the MISSING PIECE that formalizes task
 * decomposition:
 *   "method holds compound task and decomposes it into task network"
 *   (Report PDF, Section on SHOP methods)
 * 
 * A Method specifies:
 *   1. WHICH compound task it decomposes (taskName)
 *   2. WHEN this decomposition is applicable (preconditions)
 *   3. WHAT the compound task breaks into (ordered subtask list)
 * 
 * Multiple Methods can exist for the same compound task — this is what
 * enables ALTERNATIVES and BACKTRACKING.
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * Methods are the KEY addition that transforms Marwa's acting system from
 * a reactive executor into a deliberative planner. They provide:
 *   - Hierarchical decomposition (tasks within tasks)
 *   - Alternative strategies (multiple methods per task)
 *   - Conditional decomposition (preconditions gate method selection)
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * 
 * Method: "travel-by-taxi"
 *   taskName: "travel"
 *   preconditions: {taxiAvailable}
 *   subtasks: [getTaxi, rideTaxi, pay]
 * 
 * Method: "travel-by-bus"
 *   taskName: "travel"
 *   preconditions: {busTicket}
 *   subtasks: [walkToStop, takeBus, walkToDestination]
 * 
 * Method: "pay-cash"
 *   taskName: "pay"
 *   preconditions: {hasCash(sufficient)}
 *   subtasks: [payCash]
 * 
 * Method: "pay-visa"
 *   taskName: "pay"
 *   preconditions: {hasVisa}
 *   subtasks: [payVisa]
 * 
 * When the planner encounters travel(A,B):
 *   → tries travel-by-taxi first (DFS)
 *     → if it fails (e.g., can't pay) → backtracks
 *   → tries travel-by-bus next
 *     → succeeds → returns plan
 */
public class Method {

    /**
     * The compound task that this method decomposes.
     * Must match the name of a compound Task.
     * 
     * Example: "travel", "pay", "getTaxi"
     */
    private String taskName;

    /**
     * A human-readable label for this specific decomposition strategy.
     * 
     * Example: "travel-by-taxi", "travel-by-bus", "pay-cash"
     * 
     * This is primarily for debugging and thesis presentation — the
     * planner uses taskName for method lookup.
     */
    private String methodName;

    /**
     * Preconditions: propositions that must hold in the current WorldState
     * for this decomposition to be considered.
     * 
     * If preconditions are not met, the planner skips this method and
     * tries the next one (or backtracks if no methods remain).
     * 
     * Example: For "travel-by-taxi", precondition is {taxiAvailable}
     */
    private Set<String> preconditions;

    /**
     * The ordered list of subtasks that this method decomposes into.
     * 
     * These subtasks replace the original compound task in the task
     * network during planning. They can be either primitive (resolved
     * as operators) or compound (recursively decomposed via methods).
     * 
     * The ORDER matters — this is Total-Order Forward Decomposition.
     * Subtasks are executed left-to-right.
     * 
     * Example: For "travel-by-taxi":
     *   subtasks = [getTaxi, rideTaxi, pay]
     */
    private List<Task> subtasks;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a new Method for decomposing a compound task.
     *
     * @param taskName      the compound task this method decomposes
     * @param methodName    human-readable label for this strategy
     * @param preconditions conditions that must hold for this method to apply
     * @param subtasks      ordered list of subtasks
     */
    public Method(String taskName, String methodName,
                  Set<String> preconditions, List<Task> subtasks) {
        this.taskName = taskName;
        this.methodName = methodName;
        this.preconditions = preconditions != null ? preconditions : new HashSet<>();
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
    }

    // =========================================================================
    // Core Logic
    // =========================================================================

    /**
     * Checks whether this method's preconditions are satisfied in the
     * given world state.
     * 
     * The planner calls this BEFORE attempting decomposition. If the
     * preconditions are not met, this method is skipped entirely.
     *
     * @param state the current world state
     * @return true if all preconditions hold
     */
    public boolean isApplicable(WorldState state) {
        return state.holdsAll(preconditions);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public String getTaskName() {
        return taskName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<String> getPreconditions() {
        return preconditions;
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Human-readable representation for debugging and trace logging.
     */
    @Override
    public String toString() {
        return "Method{" + methodName
                + " for " + taskName
                + ", pre=" + preconditions
                + ", subtasks=" + subtasks + "}";
    }
}
