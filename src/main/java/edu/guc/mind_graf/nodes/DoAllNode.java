package edu.guc.mind_graf.nodes;

import java.util.Random;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.NodeSet;

public class DoAllNode extends ActNode {

    static int doAllCount;

    public DoAllNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public void runActuator() throws NoSuchTypeException {
        Random rand = new Random();
        NodeSet acts = this.getDownCableSet().get("obj").getNodeSet();
        NodeSet actsCopy = new NodeSet();
        acts.addAllTo(actsCopy);
        while(!actsCopy.isEmpty()) {
            int nextActIndex = rand.nextInt(actsCopy.size());
            ActNode nextAct = (ActNode) actsCopy.getNode(nextActIndex);
            System.out.println(nextAct.getName() + " is selected");
            nextAct.restartAgenda();
            Scheduler.addToActQueue(nextAct);
            actsCopy.remove(nextAct);
        }
    }
    
}
