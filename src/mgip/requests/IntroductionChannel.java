package mgip.requests;

import components.Substitutions;
import nodes.Node;
import nodes.RuleNode;

public class IntroductionChannel extends Channel {
    public IntroductionChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextName, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextName, attitudeID, requesterNode);
    }
}
