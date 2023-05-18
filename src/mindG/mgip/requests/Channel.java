package mindG.mgip.requests;

import mindG.mgip.Report;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Substitutions;
import mindG.network.Node;

public class Channel {
    static int count = 0;
    private int idCount;
    private Substitutions filterSubstitutions;
    private Substitutions switcherSubstitutions;
    private String contextName;
    private int attitudeID;
    private Node requesterNode;

    public Channel(Substitutions switcherSubstitution, Substitutions filterSubstitutions, String contextID,
            int attitudeID, Node requesterNode) {
        idCount = count++;
        this.filterSubstitutions = filterSubstitutions;
        this.switcherSubstitutions = switcherSubstitution;
        this.contextName = contextID;
        this.attitudeID = attitudeID;
        this.requesterNode = requesterNode;
    }

    public Channel() {
    }

    /***
     * Used to test sending a report through a certain channel
     * 
     * @param report
     * @return boolean
     */
    public boolean testReportToSend(Report report) {
        boolean passTest = filterSubstitutions.filtertest(report.getSubstitutions());
        if (passTest && report.anySupportAssertedInAttitudeContext(contextName, attitudeID)) {
            Substitutions newReportSubs = report.getSubstitutions().switchReport(getSwitcherSubstitutions());
            report.setSubstitutions(newReportSubs);
            Scheduler.addToHighQueue(report);

            return true;
        }
        return false;
    }

    public ChannelType getChannelType() {
        ChannelType channelType;
        if (this instanceof AntecedentToRuleChannel)
            channelType = ChannelType.AntRule;
        else if (this instanceof RuleToConsequentChannel)
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

    public String stringifyChannelID() {
        String channelContextName = this.getContextName();
        int channelAttitudeId = this.getAttitudeID();
        Substitutions filterSubs = this.getFilterSubstitutions();
        Substitutions switchSubs = this.getSwitcherSubstitutions();
        Node requesterNode = this.getRequesterNode();
        String channelId = channelContextName + " " + channelAttitudeId + " " + filterSubs.toString() + " "
                + switchSubs.toString() +
                " requestedFrom " + requesterNode.getName();
        return channelId;
    }

    public Node getRequesterNode() {
        return requesterNode;
    }

    public void setRequesterNode(Node requesterNode) {
        this.requesterNode = requesterNode;
    }

}
