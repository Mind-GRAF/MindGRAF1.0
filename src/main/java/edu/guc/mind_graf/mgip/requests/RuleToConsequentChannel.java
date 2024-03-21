package edu.guc.mind_graf.mgip.requests;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.nodes.Node;

public class RuleToConsequentChannel extends Channel {
    public RuleToConsequentChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextID, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
    }

    public RuleToConsequentChannel() {
    }
}
