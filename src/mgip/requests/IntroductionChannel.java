package mgip.requests;

import components.Substitutions;
import mgip.ruleIntroduction.RII;
import nodes.Node;
import set.NodeSet;
import context.Context;

public class IntroductionChannel extends Channel {
    public IntroductionChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions,
            String contextName, int attitudeID, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextName, attitudeID, requesterNode);
    }

    // public static IntroductionChannel intiateIntroChannel(Channel channel, NodeSet ants, NodeSet conq) {
    //     IntroductionChannel introChannel = new IntroductionChannel(channel.getSwitcherSubstitutions(), channel.getFilterSubstitutions(), channel.getContext(), channel.getAttitudeID(), channel.getRequesterNode());
    //     RII rii = new RII(channel, ants, conq, channel.getContextName(), channel.getAttitudeID());
    //     return introChannel;
    // }
}
