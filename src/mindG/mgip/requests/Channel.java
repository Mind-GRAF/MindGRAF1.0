package mindG.mgip.requests;

import mindG.mgip.Report;
import mindG.mgip.ReportSet;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Substitutions;
import mindG.network.Node;
import mindG.network.PropositionNode;

public class Channel {
    static int count = 0;
    private int idCount;
    private Substitutions filterSubstitutions;
    private Substitutions switcherSubstitutions;
    private String contextName;
    private int attitudeID;
    private boolean valve;
    private Node requesterNode;
    // private Node reporter;
    // private ReportSet reportsBuffer;

    public Channel(Substitutions switcherSubstitution, Substitutions filterSubstitutions, String contextID,
            int attitudeID, boolean v, Node requesterNode) {
        idCount = count++;
        this.filterSubstitutions = new Substitutions(filterSubstitutions);
        this.switcherSubstitutions = new Substitutions(switcherSubstitution);
        this.contextName = contextID;
        this.attitudeID = attitudeID;
        this.valve = v;
        this.requesterNode = requesterNode;
        // this.requester = requester;
        // this.reporter = reporter;
        // setReportsBuffer(new ReportSet());
    }
    // i need another type of channel called ActChannel nothing special about it

    public Channel() {
    }

    // This method is responsible for trying to send a report over a
    // channel.
    public boolean testReportToSend(Report report, Node node) {

        boolean passTest = Substitutions.canPass(report.getSubstitutions(), getFilterSubstitutions());

        if (passTest && report.anySupportAssertedInAttitudeContext(contextName, attitudeID)) {
            Substitutions newReportSubs = Substitutions.switchReport(report.getSubstitutions(),
                    getSwitcherSubstitutions());
            report.setSubstitutions(newReportSubs);
            report.setRequesterNode(node);
            Scheduler.addToHighQueue(report);
            // this.getReportsBuffer().addReport(report);

            return true;
        }
        return false;
    }
    // This method is when we have substitutions subset of another and being
    // requested by the same node so it is a more generalized request

    public boolean processedGeneralizedRequest(Substitutions currentRequestfilterSubs) {

        return false;

    }

    public ChannelType getChannelType(Channel channel) {
        ChannelType channelType;
        if (channel instanceof AntecedentToRuleChannel)
            channelType = ChannelType.AntRule;
        else if (channel instanceof RuleToConsequentChannel)
            channelType = ChannelType.RuleCons;
        else
            channelType = ChannelType.MATCHED;
        return channelType;
    }

    public Substitutions getFilterSubstitutions() {
        return filterSubstitutions;
    }

    public void setFilterSubstitutions(Substitutions filterSubstitutions) {
        this.filterSubstitutions = filterSubstitutions;
    }

    public Substitutions getSwitcherSubstitutions() {
        return switcherSubstitutions;
    }

    public void setSwitcherSubstitutions(Substitutions switcherSubstitutions) {
        this.switcherSubstitutions = switcherSubstitutions;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public int getAttitudeID() {
        return attitudeID;
    }

    public void setAttitudeID(int attitudeID) {
        this.attitudeID = attitudeID;
    }

    private void setReportsBuffer(ReportSet reportSet) {
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        Channel.count = count;
    }

    public int getIdCount() {
        return idCount;
    }

    public void setIdCount(int idCount) {
        this.idCount = idCount;
    }

    public boolean isValve() {
        return valve;
    }

    public void setValve(boolean valve) {
        this.valve = valve;
    }

    public String stringifyChannelID() {
        String channelContextName = this.getContextName();
        int channelAttitudeId = this.getAttitudeID();
        Substitutions filterSubs = this.getFilterSubstitutions();
        Substitutions switchSubs = this.getSwitcherSubstitutions();
        Node requesterNode = this.getRequesterNode();
        String channelId = channelContextName + "" + channelAttitudeId + "" + filterSubs.toString()
                + switchSubs.toString() + " requestedFrom  " + requesterNode.getName();
        return channelId;
    }

    public Node getRequesterNode() {
        return requesterNode;
    }

    public void setRequesterNode(Node requesterNode) {
        this.requesterNode = requesterNode;
    }

}
