package nodes;

import cables.DownCableSet;
import context.ContextController;
import context.Context;

public class AttitudeNode extends ActNode {

    public AttitudeNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        Context context = new Context(ContextController.getCurrContextName(), ContextController.getAttitudes());
        PropositionNode prop = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
        ActNode action = (ActNode) node.getDownCableSet().get("action").getNodeSet().getNode(0);
        int attitudeID = ContextController.getAttitudeNumber(action.getName());
        context.addHypothesisToContext(attitudeID, prop);
    }

}
