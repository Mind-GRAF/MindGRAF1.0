package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;

public class AttitudeNode extends ActNode {

    public AttitudeNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    public void runActuator() {
        Context context = ContextController.getContext(ContextController.getCurrContextName());
        PropositionNode prop = (PropositionNode) this.getDownCableSet().get("obj").getNodeSet().getNode(0);
        IndividualNode action = (IndividualNode) this.getDownCableSet().get("action").getNodeSet().getNode(0);
        int attitudeID = ContextController.getAttitudeNumber(action.getName());
        context.addHypothesisToContext(0, attitudeID, prop);
        if(context.isHypothesis(0, attitudeID, prop)) {
            System.out.println(prop.getName() + " is added to context as an attitude of " + action.getName());
        }
    }

}