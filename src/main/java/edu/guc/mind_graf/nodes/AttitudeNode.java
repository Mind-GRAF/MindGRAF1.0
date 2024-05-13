package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;

public class AttitudeNode extends ActNode {

    public AttitudeNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public void runActuator() {
        System.out.println("tmam");
        Context context = ContextController.getContext(ContextController.getCurrContextName());
        PropositionNode prop = (PropositionNode) this.getDownCableSet().get("obj").getNodeSet().getNode(0);
        ActNode attitude = (ActNode) this.getDownCableSet().get("action").getNodeSet().getNode(0);
        int attitudeID = ContextController.getAttitudeNumber(attitude.getName());
        System.out.println(attitudeID);
        context.addHypothesisToContext(attitudeID, prop);
    }

}