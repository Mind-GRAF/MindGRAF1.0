package nodes;

import actuators.AttitudeActuator;
import actuators.ControlActuator;
import actuators.DoAllActuator;
import actuators.DoOneActuator;
import actuators.SNSequenceActuator;
import network.Network;

public class ControlActionNode extends ActionNode{

    public static ControlActionNode SNSEQUENCE, SNIF, SNITERATE, ACHIEVE, DO_ONE, DO_ALL, GUARDED_ACT, ATTITUDE;

    public ControlActionNode(String name) {
        super(name);
    }

    public boolean needsDeliberation() {
		switch(this.getName()) {
		case "SNIF":
		case "SNITERATE":
		case "ACHIEVE":
			return true;
			default: return false;
		}
	}

    	public static void initControlActions() {
		//TODO
		try {
			SNSEQUENCE = (ControlActionNode) Network.createNode("SNSEQUECE", "individualnode");
			SNSEQUENCE.setActuator(SNSequenceActuator.init());
						
			DO_ONE = (ControlActionNode) Network.createNode("DO_ONE", "individualnode");
			DO_ONE.setActuator(DoOneActuator.init());
			
			DO_ALL = (ControlActionNode) Network.createNode("DO_ALL", "individualnode");
			DO_ALL.setActuator(DoAllActuator.init());
						
			ATTITUDE = (ControlActionNode) Network.createNode("ATTITUDE", "individualnode");
			ATTITUDE.setActuator(AttitudeActuator.init());

			SNIF = (ControlActionNode) Network.createNode("SNIF", "individualnode");
			
			SNITERATE = (ControlActionNode) Network.createNode("SNITERATE", "individualnode");
			
			ACHIEVE = (ControlActionNode) Network.createNode("ACHIEVE", "individualnode");
			
			GUARDED_ACT = (ControlActionNode) Network.createNode("GUARDED_ACT", "individualnode");

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
    
}
