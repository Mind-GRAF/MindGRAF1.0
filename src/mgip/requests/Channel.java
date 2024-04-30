package mgip.requests;

import components.Substitutions;
import mgip.Report;
import mgip.Scheduler;
import nodes.Node;

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
        idCount = ++count;
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
        System.out.println("Testing report to be sent:");
        boolean passTest = filterSubstitutions.filtertest(report.getSubstitutions());
        if (passTest == true) {
            System.out.println("It passed the filter test");
        } else {
            System.out.println("It failed the filter test");
        }

        if (passTest && report.anySupportSupportedInAttitudeContext(contextName, attitudeID)) {

            Substitutions newReportSubs = report.getSubstitutions().switchReport(getSwitcherSubstitutions());
            if (report.getSubstitutions().size() == 0)
                report.setSubstitutions(getSwitcherSubstitutions());

            else if (getSwitcherSubstitutions().size() == 0)
                report.setSubstitutions(report.getSubstitutions());
            else
                report.setSubstitutions(newReportSubs);
            report.setRequesterNode(this.getRequesterNode());
            report.setReportType(getChannelType());
            report.setAttitude(attitudeID);
            report.setContextName(contextName);
            Scheduler.addToHighQueue(report);
            System.out.println("The report to " + report.getRequesterNode().getName()
                    + " was just enqueued in the high priority queue to be processed");
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
        else if (this instanceof IntroductionChannel)
            channelType = ChannelType.Introduction;
        else
            channelType = ChannelType.Matched;
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
        String channelId = "Context: " + channelContextName + " and Attitude: " + channelAttitudeId + " of filter: "
                + filterSubs + " and switch: "
                + switchSubs +
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
