package edu.guc.mind_graf.mgip.requests;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.nodes.Node;

public class IntroductionChannel extends Channel {
    public IntroductionChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextName, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextName, attitudeID, requesterNode);
    }
}

