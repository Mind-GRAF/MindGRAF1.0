package edu.guc.mind_graf.nodes;

/*
 * DoOneNode — a control act node that picks ONE child act from alternatives and
 * manages its OWN retry agenda (the Dr.'s per-node-type design, now implemented).
 *
 * EXECUTION SEMANTICS:
 * - runActuator() gathers this node's alternatives, sets its own controlAgenda
 *   to RETRYING, schedules the first alternative on Scheduler.actQueue (the
 *   PLANNING queue, not the real-world executionQueue), and registers a choice
 *   point that points back at THIS node.
 * - This node OWNS its alternatives and the index of the one currently being
 *   tried. When a chosen branch fails (NoPlansExistForTheActException), the
 *   Scheduler trims the queues and asks THIS node for its next alternative via
 *   advanceToNextAlternative(); the node advances its own index and agenda.
 * - When the alternatives are exhausted the node's agenda becomes DONE and the
 *   Scheduler unwinds to an outer choice point (or reports overall failure).
 *
 * This realises the per-node retry agenda: each control node defines its own
 * retry/failure semantics (here, "try the next alternative on failure"), the
 * same way each defines its own runActuator(). The Scheduler still catches the
 * deep failure and performs the queue trimming, but the decision of WHICH
 * alternative to try next now lives in the node, driven by its controlAgenda
 * (START -> RETRYING -> DONE).
 */

import java.util.ArrayList;
import java.util.Arrays;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoOneNode extends ActNode {

    static int doOneCount;

    /** The alternatives this DoOne chooses among, in the order it will try them. */
    private ArrayList<ActNode> alternatives;
    /** Index of the alternative currently being tried. */
    private int altIndex;
    /** This node's own agenda: START -> RETRYING (alternatives remain) -> DONE. */
    private ActAgenda controlAgenda = ActAgenda.START;

    public DoOneNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public boolean isControlAct() {
        return true;
    }

    /**
     * Fixes the alternatives (and their order) explicitly, instead of reading
     * them from the node's "obj" cable. Useful for deterministic ordering (the
     * "obj" NodeSet is HashMap-backed, so its iteration order is unspecified).
     */
    public void primeAlternatives(ActNode... alts) {
        this.alternatives = new ArrayList<ActNode>(Arrays.asList(alts));
    }

    @Override
    public void runActuator() {
        // First entry: gather the alternatives (unless primed) and start this
        // node's own retry agenda.
        if (alternatives == null) {
            alternatives = new ArrayList<ActNode>();
            NodeSet possibleActs = this.getDownCableSet().get("obj").getNodeSet();
            for (Node n : possibleActs) {
                alternatives.add((ActNode) n);
            }
        }
        if (alternatives.isEmpty()) {
            controlAgenda = ActAgenda.DONE;
            return;
        }
        altIndex = 0;
        controlAgenda = ActAgenda.RETRYING;
        // Register a choice point that delegates the "next alternative" decision
        // back to THIS node and snapshots the queue depths for trimming.
        Scheduler.pushPlanChoicePoint(this, Scheduler.getActQueue().size());
        scheduleCurrentAlternative();
    }

    private void scheduleCurrentAlternative() {
        ActNode act = alternatives.get(altIndex);
        System.out.println(act.getName());
        act.restartAgenda();
        Scheduler.addToActQueue(act);
    }

    /** True if there is at least one more alternative to try after the current one. */
    public boolean hasNextAlternative() {
        return alternatives != null && altIndex + 1 < alternatives.size();
    }

    /**
     * Advances this node's own agenda to the next alternative and returns it, or
     * null when the alternatives are exhausted (agenda -> DONE). Called by the
     * Scheduler during backtracking.
     */
    public ActNode advanceToNextAlternative() {
        altIndex++;
        if (alternatives != null && altIndex < alternatives.size()) {
            controlAgenda = ActAgenda.RETRYING;
            return alternatives.get(altIndex);
        }
        controlAgenda = ActAgenda.DONE;
        return null;
    }
}
