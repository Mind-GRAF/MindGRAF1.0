package mgip.requests;

import components.Substitutions;
import nodes.Node;

public class ActChannel extends Channel {
    public ActChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
    }

    public ActChannel() {
    }
}
