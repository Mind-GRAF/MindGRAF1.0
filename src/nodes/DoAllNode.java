package nodes;

import java.util.Random;

import mgip.Scheduler;
import set.NodeSet;

public class DoAllNode extends ActNode {

    public DoAllNode(String name, Boolean isVariable) {
        super(name, isVariable);
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
