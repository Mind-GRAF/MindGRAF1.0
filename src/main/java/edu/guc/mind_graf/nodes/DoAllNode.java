package edu.guc.mind_graf.nodes;

/*
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Deterministic DoAll scheduling: iterate in insertion order and schedule
 *   acts sequentially. This avoids randomization so plan traces are stable.
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
