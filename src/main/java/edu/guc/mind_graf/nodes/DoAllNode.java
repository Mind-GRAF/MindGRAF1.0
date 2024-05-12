package edu.guc.mind_graf.nodes;

import java.util.Random;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoAllNode extends ActNode {

    static int doAllCount;

    public DoAllNode(DownCableSet downCables) {
        super(downCables);
    }

    @Override
    public void runActuator() {
        Random rand = new Random();
        NodeSet acts = this.getDownCableSet().get("obj").getNodeSet();
        NodeSet actsCopy = new NodeSet();
        acts.addAllTo(actsCopy);
        while(!actsCopy.isEmpty()) {
            System.out.println("tmam");
            int nextActIndex = rand.nextInt(actsCopy.size());
            ActNode nextAct = (ActNode) actsCopy.getNode(nextActIndex);
            System.out.println(nextAct.getName());
            nextAct.restartAgenda();
            Scheduler.addToActQueue(nextAct);
            actsCopy.remove(nextAct);
        }
    }
    
}
