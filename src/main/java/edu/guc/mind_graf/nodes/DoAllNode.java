package edu.guc.mind_graf.nodes;

/*
 * DoAllNode — a control act node that schedules ALL its child acts for
 * execution. This is a non-leaf node whose outcome is determined during
 * planning (decomposition), not during real-world execution.
 *
 * EXECUTION SEMANTICS:
 * - runActuator() adds each child act to Scheduler.actQueue (the PLANNING
 *   queue), NOT to Scheduler.executionQueue (the REAL-WORLD execution queue).
 * - Each child act still goes through the full processIntends() agenda
 *   lifecycle (START → FIND_PRECONDITIONS → TEST → EXECUTE → DONE).
 * - Only leaf primitive acts eventually reach the executionQueue at the
 *   EXECUTE stage of processIntends().
 * - This is the same pattern as DoOneNode and SNSequenceNode — control nodes
 *   decompose into the planning queue; only terminal primitives reach the
 *   execution queue where they are handled by the real world.
 *
 * WHY actQueue and not executionQueue?
 * - Control nodes (DoAll, DoOne, Sequence) represent decomposition decisions
 *   whose outcomes we know during planning. We don't need the real world to
 *   tell us the result of "do all of these" — we just schedule them.
 * - Leaf primitives go to executionQueue because their outcome depends on
 *   real-world execution (actuator side effects).
 *
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Deterministic DoAll scheduling: iterate and schedule acts sequentially.
 */

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoAllNode extends ActNode {

    static int doAllCount;

    public DoAllNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public boolean isControlAct() {
        return true;
    }

    @Override
    public void runActuator() {
        NodeSet acts = this.getDownCableSet().get("obj").getNodeSet();
        for (Node node : acts) {
            ActNode nextAct = (ActNode) node;
            System.out.println(nextAct.getName());
            nextAct.restartAgenda();
            Scheduler.addToActQueue(nextAct);
        }
    }
    
}
