package edu.guc.mind_graf.nodes;

import java.util.Stack;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;

/*
 * SNSequenceNode — a control act node that schedules its child acts in
 * strict ORDER. This is a non-leaf node whose outcome is determined during
 * planning (decomposition), not during real-world execution.
 *
 * EXECUTION SEMANTICS:
 * - runActuator() pushes child acts onto Scheduler.actQueue (the PLANNING
 *   queue, which is a Stack/LIFO), NOT the executionQueue (real-world).
 * - Children are collected via numbered relations (obj1, obj2, ...) and
 *   pushed in reverse order so the first child ends up on top of the stack
 *   and executes first.
 * - Each child goes through the full processIntends() agenda lifecycle
 *   (START → FIND_PRECONDITIONS → TEST → EXECUTE → DONE).
 * - Only leaf primitive acts eventually reach the executionQueue.
 *
 * WHY actQueue and not executionQueue?
 * - Sequence is a control/decomposition node — we know its semantics during
 *   planning ("do these in order"). The real world doesn't determine the
 *   outcome of "execute in sequence."
 * - Leaf primitives go to executionQueue because their outcome depends on
 *   real-world execution (actuator side effects).
 *
 * NOTE: Unlike DoAllNode/DoOneNode which use a single "obj" relation with
 * a multi-node NodeSet, SNSequenceNode uses numbered relations (obj1, obj2, ...)
 * to encode strict ordering.
 */
public class SNSequenceNode extends ActNode {

    public SNSequenceNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public boolean isControlAct() {
        return true;
    }

    //howa eshm3na fl SNSequence byb2a el node 3obara 3n object1 w object2 wl ba2y object bas

    @Override
    public void runActuator() {
        Stack<ActNode> acts = new Stack<>();
        DownCable next = this.getDownCableSet().get("obj" + 1);
        ActNode act;
        for(int i=2; next!=null; i++) {
			act = (ActNode) next.getNodeSet().getNode(0);
			act.restartAgenda();
			acts.push(act);
			next = this.getDownCableSet().get("obj" + i);
            System.out.println(i);
		}
        while(!acts.isEmpty()) {
            System.out.println(acts.peek().getName());
            Scheduler.addToActQueue(acts.pop());
        }
    }

}
