package mgip.requests;

import components.Substitutions;
import mgip.ruleIntroduction.RII;
import nodes.Node;
import set.NodeSet;

public class IntroductionChannel extends Channel {
    public IntroductionChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
    }

    public IntroductionChannel() {
    }
}
