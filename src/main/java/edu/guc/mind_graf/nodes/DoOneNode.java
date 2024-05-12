package edu.guc.mind_graf.nodes;

import java.util.Random;

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
        System.out.println("tmam");
        Random rand = new Random();
        NodeSet possibleActs = this.getDownCableSet().get("obj").getNodeSet();
        int actIndex = rand.nextInt(possibleActs.size());
        ActNode act = (ActNode) possibleActs.getNode(actIndex);
        System.out.println(act.getName());
        act.restartAgenda();
        Scheduler.addToActQueue(act);
    }

}