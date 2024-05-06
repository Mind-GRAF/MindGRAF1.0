package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.context.Context;

public class AttitudeNode extends ActNode {

    public AttitudeNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        Context context = ContextController.getContext(ContextController.getCurrContextName());
        PropositionNode prop = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
        ActNode action = (ActNode) node.getDownCableSet().get("action").getNodeSet().getNode(0);
        int attitudeID = ContextController.getAttitudeNumber(action.getName());
        context.addHypothesisToContext(attitudeID, prop);
    }

}
