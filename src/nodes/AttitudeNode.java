package nodes;

public class AttitudeNode extends ActNode {

    public AttitudeNode(String name, Boolean isVariable) {
        super(name, isVariable);
    }

    public void runActuator(ActNode node) {
        PropositionNode p = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
        //TODO: Add the proposition node p with the attitude ID to assert it to a certain set in the context

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    }

    }

}
