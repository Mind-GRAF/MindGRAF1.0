package mindG.mgip.requests;

import mindG.mgip.ReportSet;
import mindG.mgip.matching.Substitutions;
import mindG.network.Node;

public class AntecedentToRuleChannel extends Channel {
    public AntecedentToRuleChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, boolean v, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, v, requesterNode);
    }

    public AntecedentToRuleChannel() {
    }

}
