package edu.guc.mind_graf.nodes;

import java.util.Random;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoAllNode extends ActNode {

    int doAllCount;

    public DoAllNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
    Random rand = new Random();
	NodeSet acts = node.getDownCableSet().get("obj").getNodeSet();
	NodeSet actsCopy = new NodeSet();
	actsCopy.addAllTo(acts);
        while(!actsCopy.isEmpty()) {
            int nextActIndex = rand.nextInt(actsCopy.size());
            ActNode nextAct = (ActNode) actsCopy.getNode(nextActIndex);
            nextAct.restartAgenda();
            Scheduler.addToActQueue(nextAct);
            actsCopy.remove(nextAct);
        }
    }
    
}
