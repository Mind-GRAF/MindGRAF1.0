package edu.guc.mind_graf.htn;

import java.util.ArrayList;
import java.util.List;

/**
 * HTNPlanner — The SHOP-style HTN Planning Engine.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * Ibrahim's system operates reactively:
 *   1. Inference engine identifies a goal (ActNode)
 *   2. The ActNode checks its conditions
 *   3. If conditions hold → execute immediately
 *   4. If conditions fail → system is STUCK (no alternatives)
 * 
 * The HTNPlanner replaces step 2-4 with a DELIBERATIVE planning process:
 *   1. Inference engine identifies a goal (same as before)
 *   2. Goal is converted to an HTN Task
 *   3. Planner performs DFS over task decompositions
 *   4. If a valid plan is found → return it for execution
 *   5. If no plan exists → report failure (gracefully)
 * 
 * ==========================================================================
 * ALGORITHM: SHOP (Simple Hierarchical Ordered Planner)
 * ==========================================================================
 * The planner implements Total-Order Forward Decomposition (TFD):
 * 
 *   seekPlan(State S, TaskList T, Domain D, Plan p):
 *     1. If T is empty → return p (success: all tasks resolved)
 *     2. Take first task t from T, let R = remaining tasks
 *     3. If t is PRIMITIVE:
 *        a. Find operator matching t
 *        b. If operator applicable in S:
 *           - Apply operator → new state S'
 *           - Add operator to plan p
 *           - Recurse: seekPlan(S', R, D, p)
 *        c. Else → return FAIL (triggers backtracking)
 *     4. If t is COMPOUND:
 *        a. For each method m that decomposes t:
 *           - If m's preconditions hold in S:
 *             - Replace t with m's subtasks
 *             - Recurse: seekPlan(S, subtasks + R, D, p)
 *             - If result ≠ FAIL → return result
 *        b. All methods failed → return FAIL (backtrack)
 * 
 * This is a DEPTH-FIRST SEARCH with BACKTRACKING:
 *   - "Depth-first" because it fully explores one decomposition path
 *     before trying alternatives
 *   - "Backtracking" because when a path fails, it returns to the
 *     last decision point and tries the next alternative
 * 
 * ==========================================================================
 * INTEGRATION WITH MINDGRAF:
 * ==========================================================================
 * 
 * BEFORE (Ibrahim only):
 *   execute(actNode);
 * 
 * AFTER (with HTN planner):
 *   PlanResult result = planner.plan(goalTask, initialState, domain);
 *   if (result.isSuccess()) {
 *       scheduler.executeAll(result.getPlan());
 *   }
 * 
 * The planner sits BETWEEN the inference engine and the scheduler.
 * It does NOT modify any existing MindGRAF components.
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN TRACE:
 * ==========================================================================
 * See HTNPlannerTest.java for a full execution trace with the extended
 * transportation domain (taxi/bus with nested pay decomposition).
 */
public class HTNPlanner {

    /** Indentation depth for trace logging (tracks recursion level) */
    private int traceDepth = 0;

    /** Whether to print detailed trace logs during planning */
    private boolean verbose = true;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a new HTNPlanner instance.
     *
     * @param verbose if true, prints detailed trace logs during planning
     */
    public HTNPlanner(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Creates a new HTNPlanner with verbose logging enabled.
     */
    public HTNPlanner() {
        this(true);
    }

    // =========================================================================
    // Public Entry Point
    // =========================================================================

    /**
     * Plans a sequence of operators to achieve the given goal task.
     * 
     * This is the main entry point called from the acting system.
     * It wraps the goal in a task list and delegates to seekPlan().
     *
     * @param goal   the high-level goal task (typically compound)
     * @param state  the initial world state (agent's beliefs)
     * @param domain the HTN domain (operators + methods)
     * @return PlanResult with the plan or failure
     */
    public PlanResult plan(Task goal, WorldState state, HTNDomain domain) {
        log("============================================================");
        log("HTN PLANNER: Starting planning for goal: " + goal);
        log("Initial state: " + state);
        log("============================================================\n");

        // Wrap the single goal task in a task list (SHOP format)
        List<Task> taskList = new ArrayList<>();
        taskList.add(goal);

        // Call the core SHOP algorithm with an empty plan
        traceDepth = 0;
        PlanResult result = seekPlan(state, taskList, domain, new ArrayList<>());

        log("\n============================================================");
        if (result.isSuccess()) {
            log("PLANNING SUCCEEDED!");
            log("Final plan: " + result);
        } else {
            log("PLANNING FAILED — no valid plan found.");
        }
        log("============================================================");

        return result;
    }

    // =========================================================================
    // Core SHOP Algorithm
    // =========================================================================

    /**
     * The recursive SHOP planning algorithm (Total-Order Forward Decomposition).
     * 
     * This is a direct implementation of the SHOP pseudocode from the
     * thesis report:
     * 
     *   seekPlan(S, T, D, p):
     *     if T = ∅ then return p
     *     t ← first(T); R ← rest(T)
     *     if t is primitive:
     *       op ← findOperator(t)
     *       if op.applicable(S):
     *         return seekPlan(op.apply(S), R, D, p ∪ {op})
     *       else:
     *         return FAIL
     *     else:
     *       for each method m for t:
     *         if m.preconditions satisfied in S:
     *           result ← seekPlan(S, m.subtasks ++ R, D, p)
     *           if result ≠ FAIL: return result
     *       return FAIL
     * 
     * KEY PROPERTIES:
     * - Depth-first: explores one path completely before alternatives
     * - Backtracking: returns FAIL to try next method at decision points
     * - Forward: applies operators to move state forward during search
     * - Total-order: tasks are processed strictly left-to-right
     * 
     * @param state    current world state (S)
     * @param tasks    remaining task list (T)
     * @param domain   the planning domain (D)
     * @param plan     accumulated plan so far (p)
     * @return PlanResult with success or failure
     */
    private PlanResult seekPlan(WorldState state, List<Task> tasks,
                                 HTNDomain domain, List<Operator> plan) {

        // =====================================================================
        // BASE CASE: No more tasks → plan is complete
        // =====================================================================
        // If the task list is empty, all tasks have been successfully resolved
        // into operators. The accumulated plan is our answer.
        if (tasks.isEmpty()) {
            logIndented("✅ Task list empty — plan complete!");
            return PlanResult.success(plan);
        }

        // =====================================================================
        // RECURSIVE CASE: Process the first task
        // =====================================================================
        // SHOP is a "first-task-first" algorithm: always process the
        // leftmost task in the list. This gives total-order behavior.
        Task currentTask = tasks.get(0);
        List<Task> remainingTasks = new ArrayList<>(tasks.subList(1, tasks.size()));

        logIndented("Processing task: " + currentTask);
        logIndented("Remaining tasks: " + remainingTasks);
        logIndented("Current state: " + state);
        logIndented("Plan so far: " + planNames(plan));

        // =====================================================================
        // CASE 1: Primitive Task → Find and apply Operator
        // =====================================================================
        if (currentTask.isPrimitive()) {
            return handlePrimitiveTask(currentTask, state, remainingTasks, domain, plan);
        }

        // =====================================================================
        // CASE 2: Compound Task → Decompose via Methods
        // =====================================================================
        else {
            return handleCompoundTask(currentTask, state, remainingTasks, domain, plan);
        }
    }

    // =========================================================================
    // Primitive Task Handling
    // =========================================================================

    /**
     * Handles a primitive task by finding its operator and applying it.
     * 
     * In Ibrahim's system, this is analogous to:
     *   ActNode.processIntends() → check conditions → execute
     * 
     * Except here we SIMULATE the execution (update WorldState) rather
     * than actually performing it. Actual execution happens later, after
     * the full plan is computed.
     *
     * @param task           the primitive task to handle
     * @param state          current world state
     * @param remainingTasks tasks remaining after this one
     * @param domain         the planning domain
     * @param plan           accumulated plan so far
     * @return PlanResult with success or failure
     */
    private PlanResult handlePrimitiveTask(Task task, WorldState state,
                                            List<Task> remainingTasks,
                                            HTNDomain domain, List<Operator> plan) {

        // Find the operator that implements this primitive task
        Operator op = domain.findOperator(task.getName());

        if (op == null) {
            logIndented("❌ No operator found for primitive task: " + task.getName());
            return PlanResult.failure();
        }

        // Check if the operator's preconditions are satisfied
        if (op.isApplicable(state)) {
            logIndented("✅ Operator '" + op.getName() + "' is applicable");
            logIndented("   Preconditions " + op.getPreconditions() + " → all satisfied");

            // SIMULATE execution: apply operator effects to get new state
            // (This does NOT modify the original state — a copy is created)
            WorldState newState = op.apply(state);
            logIndented("   Applied effects: +" + op.getAddList() + " / -" + op.getDeleteList());
            logIndented("   New state: " + newState);

            // Add this operator to the plan
            List<Operator> newPlan = new ArrayList<>(plan);
            newPlan.add(op);

            // Recurse: continue planning with the remaining tasks
            traceDepth++;
            PlanResult result = seekPlan(newState, remainingTasks, domain, newPlan);
            traceDepth--;

            return result;

        } else {
            // PRECONDITION FAILURE → this is where backtracking is triggered!
            // The planner returns FAIL, causing the caller to try the next
            // alternative (next method at the parent compound task).
            logIndented("❌ Operator '" + op.getName() + "' NOT applicable!");
            logIndented("   Required: " + op.getPreconditions());
            logIndented("   State has: " + state.getPropositions());
            logIndented("   → FAIL (backtracking...)");
            return PlanResult.failure();
        }
    }

    // =========================================================================
    // Compound Task Handling
    // =========================================================================

    /**
     * Handles a compound task by trying each method in order (DFS).
     * 
     * This is the core of the hierarchical decomposition and the source
     * of backtracking behavior. For each method:
     *   1. Check if the method's preconditions are satisfied
     *   2. If yes, replace the compound task with the method's subtasks
     *   3. Recurse with the expanded task list
     *   4. If the recursion succeeds → return the result (first success wins)
     *   5. If the recursion fails → try the next method (backtrack)
     *   6. If ALL methods fail → return FAIL (propagate backtracking upward)
     * 
     * This is exactly the DFS behavior described in the thesis:
     *   "for every reduction r… if FAIL → try next method"
     *
     * @param task           the compound task to decompose
     * @param state          current world state
     * @param remainingTasks tasks remaining after this one
     * @param domain         the planning domain
     * @param plan           accumulated plan so far
     * @return PlanResult with success or failure
     */
    private PlanResult handleCompoundTask(Task task, WorldState state,
                                           List<Task> remainingTasks,
                                           HTNDomain domain, List<Operator> plan) {

        // Get all methods that can decompose this compound task
        List<Method> methods = domain.getMethods(task.getName());

        if (methods.isEmpty()) {
            logIndented("❌ No methods found for compound task: " + task.getName());
            return PlanResult.failure();
        }

        logIndented("Found " + methods.size() + " method(s) for '" + task.getName() + "'");

        // Try each method in order (DFS: first method first, full depth)
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);

            logIndented("→ Trying method " + (i + 1) + "/" + methods.size()
                    + ": " + method.getMethodName());

            // Check if this method's preconditions are satisfied
            if (method.isApplicable(state)) {
                logIndented("  ✅ Method preconditions " + method.getPreconditions() + " satisfied");

                // DECOMPOSE: replace the compound task with method's subtasks
                // The new task list is: [subtasks...] ++ [remaining tasks...]
                List<Task> expandedTasks = new ArrayList<>(method.getSubtasks());
                expandedTasks.addAll(remainingTasks);

                logIndented("  Expanded tasks: " + expandedTasks);

                // Recurse with the expanded task list
                // (state is unchanged — decomposition doesn't modify state)
                traceDepth++;
                PlanResult result = seekPlan(state, expandedTasks, domain, plan);
                traceDepth--;

                // If this path succeeded, return immediately (DFS: first success wins)
                if (result.isSuccess()) {
                    logIndented("  ✅ Method '" + method.getMethodName() + "' led to a valid plan!");
                    return result;
                }

                // If this path failed, try the next method (BACKTRACKING)
                logIndented("  ❌ Method '" + method.getMethodName()
                        + "' failed → backtracking to try next method...");

            } else {
                // Method's preconditions not satisfied → skip it
                logIndented("  ⏭️  Method preconditions " + method.getPreconditions()
                        + " NOT satisfied → skipping");
            }
        }

        // ALL methods have been tried and failed → propagate failure upward
        logIndented("❌ ALL methods for '" + task.getName() + "' failed → FAIL");
        return PlanResult.failure();
    }

    // =========================================================================
    // Logging Utilities
    // =========================================================================

    /**
     * Logs a message with indentation based on recursion depth.
     * This creates a visual trace of the DFS exploration.
     */
    private void logIndented(String message) {
        if (!verbose) return;
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < traceDepth; i++) {
            indent.append("  ");
        }
        System.out.println(indent.toString() + message);
    }

    /**
     * Logs a top-level message (no indentation).
     */
    private void log(String message) {
        if (!verbose) return;
        System.out.println(message);
    }

    /**
     * Extracts operator names from a plan list for compact display.
     */
    private List<String> planNames(List<Operator> plan) {
        List<String> names = new ArrayList<>();
        for (Operator op : plan) {
            names.add(op.getName());
        }
        return names;
    }
}
