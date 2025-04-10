package mgip.requests;

import components.Substitutions;
import nodes.Node;

public class AntecedentToRuleChannel extends Channel {
    public AntecedentToRuleChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
    }

    public AntecedentToRuleChannel() {
    }

}
