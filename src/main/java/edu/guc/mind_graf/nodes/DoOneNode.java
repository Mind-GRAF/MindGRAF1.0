package edu.guc.mind_graf.nodes;

/*
 * DoOneNode — a control act node that picks ONE child act from alternatives.
 * This is a non-leaf node whose outcome is determined during planning
 * (decomposition), not during real-world execution.
 *
 * EXECUTION SEMANTICS:
 * - runActuator() picks one child act and adds it to Scheduler.actQueue
 *   (the PLANNING queue), NOT to Scheduler.executionQueue (real-world).
 * - ALL remaining siblings are saved in a PlanChoicePoint for backtracking.
 * - The picked act goes through the full processIntends() lifecycle.
 * - If the picked act fails (NoPlansExistForTheActException), the Scheduler
 *   backtracks to the next sibling via backtrackToNextAlternative().
 *
 * WHY actQueue and not executionQueue?
 * - DoOneNode is a control/decomposition node — we know its semantics during
 *   planning ("pick one from these alternatives"). We don't need the real
 *   world to resolve it.
 * - Leaf primitives go to executionQueue because their outcome depends on
 *   real-world execution (actuator side effects).
 *
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Sibling-preserving DoOne: saves remaining alternatives as a choice point
 *   instead of discarding them (pre-thesis behavior).
 *
 * DESIGN NOTE (Dr.'s feedback, 2026-05-28):
 * An alternative to Scheduler-driven backtracking is per-node-type semantics:
 * DoOneNode could manage its own agenda cycle internally:
 *   1. Pick one act from alternatives → schedule it
 *   2. If it fails → retry with next alternative (new agenda state)
 *   3. Done when one succeeds or all exhausted
 * This would require DoOneNode to have its own agenda states (e.g., TRYING,
 * RETRYING) and new agenda entries, but would produce cleaner per-type
 * semantics instead of relying on the Scheduler's global
 * backtrackToNextAlternative() mechanism. Each node type would define its
 * own retry/failure semantics, similar to how each already defines its own
 * runActuator(). This is left as future work — the Scheduler approach works
 * correctly for now.
 */

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoOneNode extends ActNode {

    static int doOneCount;

    public DoOneNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public void runActuator() {
        NodeSet possibleActs = this.getDownCableSet().get("obj").getNodeSet();
        if (possibleActs.isEmpty()) {
            return;
        }

        // NEW: keep every sibling alternative available for later backtracking.
        // The scheduler stores the remaining siblings as a choice point and we take
        // the first branch in deterministic order.
        Scheduler.pushPlanChoicePoint(this, possibleActs, Scheduler.getActQueue().size());

        ActNode act = (ActNode) possibleActs.getNode(0);
        System.out.println(act.getName());
        act.restartAgenda();
        Scheduler.addToActQueue(act);
    }

}