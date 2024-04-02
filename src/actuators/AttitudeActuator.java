package actuators;

import nodes.ActNode;
import nodes.PropositionNode;


public class AttitudeActuator implements ControlActuator{

    private static AttitudeActuator actuator;

    @Override
    public void actOnNode(ActNode node, int AttitudeID) {
        PropositionNode p = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
        //TODO: Assert the proposition node p with the attitude ID to assert it to a certain set in the context

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public static AttitudeActuator init() {
        if(actuator == null) {
            actuator = new AttitudeActuator();
        }
        return actuator;
    }

}
