package edu.guc.mind_graf.htn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PlanResult — Wrapper for the outcome of the SHOP planning algorithm.
 * 
 * ==========================================================================
 * MAPPING FROM MARWA'S ACTING SYSTEM:
 * ==========================================================================
 * In Ibrahim's system, there is no "plan" object. An ActNode either executes
 * successfully or doesn't — the system doesn't produce a sequence of
 * actions to execute later.
 * 
 * In HTN/SHOP:
 *   "A plan is a list of operator instances"
 *   (Report PDF, Section on SHOP plans)
 * 
 * The PlanResult wraps either:
 *   - A successful plan: an ordered list of Operators to execute
 *   - A failure: indicating no valid decomposition was found
 * 
 * ==========================================================================
 * WHAT THIS ADDS:
 * ==========================================================================
 * The PlanResult decouples PLANNING from EXECUTION:
 *   1. The planner produces a PlanResult
 *   2. If successful, the plan is handed to the Scheduler for execution
 *   3. If failed, the system can try alternative reasoning
 * 
 * This is the fundamental transformation of Ibrahim's system:
 *   BEFORE: Act → Execute immediately
 *   AFTER:  Goal → Plan → PlanResult → Execute sequence
 * 
 * ==========================================================================
 * TRANSPORTATION DOMAIN EXAMPLE:
 * ==========================================================================
 * Successful plan:
 *   PlanResult{success=true, plan=[walkToStop, takeBus, walkToDestination]}
 * 
 * Failed plan (if bus was also unavailable):
 *   PlanResult{success=false, plan=null}
 */
public class PlanResult {

    /** Whether the planning succeeded */
    private boolean success;

    /**
     * The ordered list of operators that constitute the plan.
     * 
     * If success is false, this is null.
     * If success is true, this is a non-empty list of operators
     * in the order they should be executed.
     * 
     * This list is what gets handed to Ibrahim's Scheduler for execution.
     */
    private List<Operator> plan;

    // =========================================================================
    // Private Constructor (use factory methods)
    // =========================================================================

    private PlanResult(boolean success, List<Operator> plan) {
        this.success = success;
        this.plan = plan;
    }

    // =========================================================================
    // Factory Methods
    // =========================================================================

    /**
     * Creates a successful plan result.
     *
     * @param plan the list of operators constituting the plan
     * @return a successful PlanResult
     */
    public static PlanResult success(List<Operator> plan) {
        return new PlanResult(true, new ArrayList<>(plan));
    }

    /**
     * Creates a failure result (no valid plan found).
     * 
     * This is returned when:
     *   - A primitive task has no applicable operator
     *   - ALL methods for a compound task have been exhausted
     *   - Backtracking has reached the root with no alternatives left
     *
     * @return a failed PlanResult
     */
    public static PlanResult failure() {
        return new PlanResult(false, null);
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the plan (list of operators in execution order).
     *
     * @return the plan, or an empty list if planning failed
     */
    public List<Operator> getPlan() {
        if (plan == null) return Collections.emptyList();
        return Collections.unmodifiableList(plan);
    }

    // =========================================================================
    // Display
    // =========================================================================

    /**
     * Human-readable plan summary for debugging and thesis presentation.
     */
    @Override
    public String toString() {
        if (!success) {
            return "PlanResult{FAILURE — no valid plan found}";
        }
        StringBuilder sb = new StringBuilder("PlanResult{SUCCESS, plan=[\n");
        for (int i = 0; i < plan.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(plan.get(i).getName());
            if (i < plan.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]}");
        return sb.toString();
    }
}
