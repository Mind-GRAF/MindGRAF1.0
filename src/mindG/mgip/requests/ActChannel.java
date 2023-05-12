package mindG.mgip.requests;

import mindG.mgip.matching.Substitutions;
import mindG.network.Node;

public class ActChannel extends Channel {
    public ActChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
    }

    public ActChannel() {
    }
}
