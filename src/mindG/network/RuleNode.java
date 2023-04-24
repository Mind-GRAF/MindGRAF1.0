package mindG.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javafx.util.Pair;
import mindG.mgip.AlmostReports;
import mindG.mgip.InferenceTypes;
import mindG.mgip.Report;
import mindG.mgip.RuleResponse;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.AntecedentToRuleChannel;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.ChannelPairSet;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.requests.MatchChannel;
import mindG.mgip.requests.Request;
import mindG.mgip.rules.AndOr;
import mindG.mgip.rules.Thresh;

public abstract class RuleNode extends PropositionNode implements Serializable {

    public RuleNode() {
    }

    public Collection<RuleResponse> applyRuleHandler(Report report, Channel currentRequest) {
        return null;

    }
    // This method is implemented to send requests to antecedents that are not
    // working
    // on a similar request to currentRequest.

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, AlmostReports almostReport) {
        NodeSet antecedentNodeSet = new NodeSet();
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        int currentAttitudeID = currentChannel.getAttitudeID();
        Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        Substitutions reportSubs = almostReport.getSubstitutions();
        Substitutions unionSubs = filterSubs.union(filterSubs, reportSubs);
        NodeSet toBeSentTo = removeAlreadyOpenChannels(antecedentNodeSet, currentRequest, unionSubs, ruleType);
        sendRequestsToNodeSet(toBeSentTo, unionSubs, currentContextName,
                currentAttitudeID, ChannelType.AntRule);
    }

    // This method is implemented to do certain actions after calling
    // applyRuleHandler()
    // and receive ruleResponses
    public void handleResponseOfApplyRuleHandler(Collection<RuleResponse> ruleResponses, Report currentReport,
            Channel currentRequest) {

    }

    /***
     * This method simply filters channelSet by removing any requests identical to
     * any
     * of the already established outgoing channels. * avoid redundancy
     * 
     * @param requestSet
     * @return
     */
    private Collection<Channel> removeExistingOutgoingChannelsFromSet(Collection<Channel> requestSet) {
        return null;
    }

    // This method iterates over all the node’s outgoing channels calling on each
    // processSingleRequestsChannel(outgoingRequest) and handles the exceptions that
    // might be
    // thrown.

    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().peek();
        try {
            processSingleRequests(requestHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    protected static NodeSet removeAlreadyOpenChannels(NodeSet nodes, Request currentRequest,
            Substitutions toBeCompared,
            boolean ruleType) {
        NodeSet nodesToConsider = new NodeSet();
        for (Node currentNode : nodes) {
            // if it is not of AndOr aw thresh eh el beyehssal?
            if (ruleType) {
                boolean notTheSame = true;
                // currentNode.getId() != currentRequest.getRequester().getId();
                if (notTheSame) {
                    ChannelPairSet outgoingChannels = ((PropositionNode) currentNode).getOutgoingChannels();
                    for (Pair<Channel, NodeSet> outgoingChannel : outgoingChannels) {
                        Substitutions processedRequestChannelFilterSubs = outgoingChannel.getKey()
                                .getFilterSubstitutions();
                        notTheSame &= !Substitutions.isSubSet(processedRequestChannelFilterSubs,
                                toBeCompared);
                        // && outgoingRequestChannel.getRequester().getId() ==
                        // currentRequest.getReporter()
                        // .getId();
                        // akher line kan maktoob fe beta3et youssef bas enty mesh fahma lehwe
                    }
                    if (notTheSame) {
                        nodesToConsider.add(currentNode);

                    }
                }
            }

        }
        return nodesToConsider;

    }

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel) {
            super.processSingleRequests(currentRequest);

        } else {
            boolean closedTypeNode = nodeType(this);
            // mehtaga acheck hena el type beta3 el node fa dih men el network
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            if (closedTypeNode) {
                if (super.asserted(currentContext, currentAttitude)) {
                    NodeSet antecedentNodesToMeClose = new NodeSet();
                    // hena el mafrood a7ot fel nodeset dih kol el antecedents leya
                    boolean ruleTypeClose = this instanceof Thresh || this instanceof AndOr;
                    NodeSet antNodesToConsiderClose = removeAlreadyOpenChannels(antecedentNodesToMeClose,
                            currentRequest,
                            filterRuleSubs, ruleTypeClose);
                    sendRequestsToNodeSet(antNodesToConsiderClose, filterRuleSubs, currentContext, currentAttitude,
                            ChannelType.AntRule);

                } else {
                    super.processSingleRequests(currentRequest);
                }

            } else {
                if (super.isVariableNode(this)) {
                    boolean isBound = false;
                    VariableSet freeVariableSet = super.getFreeVariables(this);
                    for (Node freeVariable : freeVariableSet) {
                        isBound = filterRuleSubs.isFreeVariableBound(freeVariable);
                        if (!isBound)
                            break;

                    }

                    Collection<AlmostReports> theAlmostReportsSet = this.getKnownInstances().mergeKInstances(
                            this.getKnownInstances().getPositiveKInstances(),
                            this.getKnownInstances().getNegativeKInstances());
                    for (AlmostReports currentAlmostReport : theAlmostReportsSet) {
                        Substitutions currentAlmostSubs = currentAlmostReport.getSubstitutions();
                        boolean subSetCheck = Substitutions.isSubSet(currentAlmostSubs,
                                currentRequest.getChannel().getFilterSubstitutions());

                        boolean supportCheck = currentAlmostReport.anySupportAssertedInAttitudeContext(
                                currentContext,
                                currentAttitude);
                        if (subSetCheck && supportCheck) {
                            if (isBound) {
                                NodeSet antecedentNodesToMe = new NodeSet();
                                // hena el mafrood a7ot fel nodeset dih kol el antecedents leya
                                boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                                NodeSet antNodesToConsider = removeAlreadyOpenChannels(
                                        antecedentNodesToMe,
                                        currentRequest,
                                        filterRuleSubs, ruleType);
                                sendRequestsToNodeSet(antNodesToConsider, filterRuleSubs, currentContext,
                                        currentAttitude,
                                        ChannelType.AntRule);
                                return;
                            } else
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentAlmostReport);
                        }

                    }
                    super.processSingleRequests(currentRequest);
                    return;

                }

            }

        }
    }

    /***
     * Report handling in Rule proposition nodes.
     */
    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().peek();
        try {
            processSingleReports(reportHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    protected void processSingleReports(Report reportHasTurn) {

    }

}
