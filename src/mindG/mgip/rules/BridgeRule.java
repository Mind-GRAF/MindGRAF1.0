package mindG.mgip.rules;

import java.lang.ModuleLayer.Controller;
import java.util.Collection;

import mindG.mgip.KnownInstance;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.AntecedentToRuleChannel;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.requests.MatchChannel;
import mindG.mgip.requests.Request;
import mindG.network.Context;
import mindG.network.Node;
import mindG.network.NodeSet;
import mindG.network.RuleNode;
import mindG.network.cables.DownCableSet;

public class BridgeRule extends RuleNode {

    public BridgeRule(String name, Boolean isVariable) {
        super(name, isVariable);
        // TODO Auto-generated constructor stub
    }

    public BridgeRule(DownCableSet downCableSet) {
        super(downCableSet);

    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, KnownInstance knownInstance) {
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
        Substitutions reportSubs = knownInstance.getSubstitutions();
        Substitutions unionSubs = Substitutions.union(filterSubs, reportSubs);

        NodeSet argAntCloseToMe = getDownAntArgNodeSet();
        NodeSet argAntNodesToConsiderClose = removeAlreadyEstablishedChannels(argAntCloseToMe,
                currentRequest,
                unionSubs, false);
        Context currContext = mindG.network.Controller.getContext(currentContextName);
        for (Node currentNode : argAntNodesToConsiderClose) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            sendRequestsToNodeSet(argAntNodesToConsiderClose, unionSubs, switchSubs, currentContextName,
                    currentNodeAttitude,
                    ChannelType.AntRule, this);
        }
    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest) {
        Substitutions filterRuleSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchRuleSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        String currentContext = currentRequest.getChannel().getContextName();

        NodeSet antArgNodeSet = getDownAntArgNodeSet();
        NodeSet remainingAntArgNodeSet = removeAlreadyEstablishedChannels(antArgNodeSet,
                currentRequest,
                filterRuleSubs, false);
        Context currContext = mindG.network.Controller.getContext(currentContext);
        for (Node currentNode : remainingAntArgNodeSet) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            sendRequestsToNodeSet(remainingAntArgNodeSet, filterRuleSubs, switchRuleSubs, currentContext,
                    currentNodeAttitude,
                    ChannelType.AntRule, this);
        }

    }

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel)
            super.processSingleRequests(currentRequest);

        else {
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();

            if (!this.isOpen()) {
                if (this.asserted(currentContext, currentAttitude)) {
                    NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                    NodeSet antArgNodesToConsiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                            currentRequest,
                            filterRuleSubs, false);
                    Context currContext = mindG.network.Controller.getContext(currentContext);
                    for (Node currentNode : antArgNodesToConsiderClose) {
                        int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
                        sendRequestsToNodeSet(antArgNodesToConsiderClose, filterRuleSubs, switchRuleSubs,
                                currentContext,
                                currentNodeAttitude,
                                ChannelType.AntRule, this);

                    }

                } else
                    super.processSingleRequests(currentRequest);

            } else {
                boolean isNotBound = isOpenNodeNotBound(filterRuleSubs);
                Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                        currentChannel.getAttitudeID());
                for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                    Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                    Substitutions onlySubsBindFreeVar = onlyRelevantSubs(filterRuleSubs);
                    boolean subSetCheck = onlySubsBindFreeVar
                            .compatible(currentKISubs);
                    boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                            currentContext,
                            currentAttitude);
                    if (subSetCheck && supportCheck) {
                        if (!isNotBound) {
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                            return;
                        } else
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentKnownInstance);
                        return;
                    }

                }
                super.processSingleRequests(currentRequest);
                return;

            }

        }

    }

}
