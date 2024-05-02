package nodes;

import cables.DownCableSet;
import context.ContextController;

public class AttitudeNode extends ActNode {

    public AttitudeNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        PropositionNode p = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
        ActNode action = (ActNode) node.getDownCableSet().get("action").getNodeSet().getNode(0);
        int attitudeID = ContextController.getAttitudeNumber(action.getName());
        //TODO: Add the proposition node p with to a certain set in the context using the attitudeID
    }

}
