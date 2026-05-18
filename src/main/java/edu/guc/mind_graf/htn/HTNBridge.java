package edu.guc.mind_graf.htn;

import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.ActAgenda;
import edu.guc.mind_graf.mgip.Scheduler;

/**
 * HTNBridge — Connects the HTN Planner to Marwa's Acting System.
 * 
 * This is the INTEGRATION POINT between the two systems.
 * 
 * It provides:
 *   1. A way to plan for a compound ActNode before executing it
 *   2. A way to push planned operators onto Marwa's Scheduler act stack
 *   3. A static entry point callable from ActNode.processIntends()
 * 
 * Usage from ActNode:
 *   HTNBridge bridge = new HTNBridge(domain);
 *   PlanResult result = bridge.planForAct(actNode, currentWorldState);
 *   if (result.isSuccess()) {
 *       bridge.executePlan(result);
 *   }
 */
public class HTNBridge {

    /** The HTN domain containing all operators and methods */
    private HTNDomain domain;

    /** The planner instance */
    private HTNPlanner planner;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a bridge with the given domain.
     * 
     * @param domain  the HTN domain (operators + methods)
     * @param verbose whether to print planning trace
     */
    public HTNBridge(HTNDomain domain, boolean verbose) {
        this.domain = domain;
        this.planner = new HTNPlanner(verbose);
    }

    public HTNBridge(HTNDomain domain) {
        this(domain, true);
    }

    // =========================================================================
    // Planning Entry Point
    // =========================================================================

    /**
     * Plans for a compound ActNode by converting it to an HTN Task,
     * running the SHOP planner, and returning the result.
     * 
     * This is the bridge between Marwa's ActNode and the HTN planner.
     * 
     * In Marwa's agenda lifecycle, this replaces the FIND_PLANS stage:
     *   BEFORE: backward chaining via Plan-Act transformer → DoOneNode
     *   AFTER:  HTNPlanner.seekPlan() → PlanResult with backtracking
     * 
     * @param actName   the name of the compound act (e.g., "travel")
     * @param state     the current world state (derived from beliefs)
     * @return PlanResult with the plan or failure
     */
    public PlanResult planForAct(String actName, WorldState state) {
        // Convert the ActNode name to an HTN compound Task
        Task goalTask = new Task(actName, false);

        // Run the SHOP planner
        return planner.plan(goalTask, state, domain);
    }

    /**
     * Overload that accepts args for the task.
     */
    public PlanResult planForAct(String actName, String[] args, WorldState state) {
        Task goalTask = new Task(actName, args, false);
        return planner.plan(goalTask, state, domain);
    }

    // =========================================================================
    // Execution Bridge
    // =========================================================================

    /**
     * Pushes a successful plan's operators onto Marwa's Scheduler act stack.
     * 
     * Each Operator in the plan is wrapped in an ActNode and pushed
     * onto the act queue in reverse order (since it's a stack — LIFO),
     * so they execute in the correct sequence.
     * 
     * This is where the HTN planner's output feeds back into
     * Marwa's three-queue Scheduler:
     *   plan operators → ActNode wrappers → Scheduler.addToActQueue()
     * 
     * @param result a successful PlanResult
     */
    public void executePlan(PlanResult result) {
        if (!result.isSuccess()) {
            System.out.println("[HTNBridge] Cannot execute — planning failed.");
            return;
        }

        System.out.println("[HTNBridge] Pushing planned operators to Scheduler act stack:");

        java.util.List<Operator> plan = result.getPlan();

        // Push in REVERSE order because Scheduler uses a Stack (LIFO)
        // So the first operator ends up on top and executes first
        for (int i = plan.size() - 1; i >= 0; i--) {
            Operator op = plan.get(i);

            // Create an ActNode wrapper for this operator
            // Mark as PRIMITIVE + agenda=EXECUTE so processIntends()
            // runs the actuator directly (planning already verified preconditions)
            ActNode actNode = new ActNode(op.getName(), false);
            actNode.setPrimitive(true);
            actNode.setAgenda(ActAgenda.EXECUTE);

            // Push onto Marwa's act stack
            Scheduler.addToActQueue(actNode);

            System.out.println("  → Pushed: " + op.getName() + " [primitive, agenda=EXECUTE]");
        }

        System.out.println("[HTNBridge] All " + plan.size() + " operators scheduled.");
    }

    // =========================================================================
    // Convenience: Plan + Execute in one call
    // =========================================================================

    /**
     * Plans and immediately schedules execution if successful.
     * 
     * This is the single-call integration point:
     *   HTNBridge bridge = new HTNBridge(domain);
     *   bridge.planAndExecute("travel", worldState);
     * 
     * @param actName the compound act name
     * @param state   the current world state
     * @return true if planning succeeded and operators were scheduled
     */
    public boolean planAndExecute(String actName, WorldState state) {
        PlanResult result = planForAct(actName, state);

        if (result.isSuccess()) {
            executePlan(result);
            return true;
        } else {
            System.out.println("[HTNBridge] Planning failed for '" + actName
                    + "' — no operators scheduled.");
            return false;
        }
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public HTNDomain getDomain() {
        return domain;
    }

    public HTNPlanner getPlanner() {
        return planner;
    }
}
