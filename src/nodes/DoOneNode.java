package nodes;

import java.util.Random;

import cables.DownCableSet;
import mgip.Scheduler;
import set.NodeSet;

public class DoOneNode extends ActNode {

    static int doOneCount;

    public DoOneNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        Random rand = new Random();
        NodeSet possibleActs = node.getDownCableSet().get("obj").getNodeSet();
        int actIndex = rand.nextInt(possibleActs.size());
        ActNode act = (ActNode) possibleActs.getNode(actIndex);
        act.restartAgenda();
        Scheduler.addToActQueue(act);
    }
    
}
