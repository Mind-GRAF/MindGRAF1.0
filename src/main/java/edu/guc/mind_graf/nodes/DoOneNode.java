package edu.guc.mind_graf.nodes;

/*
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Deterministic sibling-preserving DoOne: the scheduler saves the
 *   remaining alternatives as a choice point and we schedule the first
 *   alternative deterministically.
 * - This preserves siblings for later backtracking instead of collapsing
 *   them randomly.
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