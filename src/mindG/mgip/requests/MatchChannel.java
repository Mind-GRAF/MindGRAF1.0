package mindG.mgip.requests;

import mindG.mgip.Report;
import mindG.mgip.matching.Substitutions;
import mindG.network.Node;

public class MatchChannel extends Channel {
    private int matchType;

    public MatchChannel() {
        super();
    }

    public MatchChannel(Substitutions switchSubstitution, Substitutions filterSubstitutions, String contextID,
            int attitudeID,
            int matchType, Node requesterNode) {
        super(switchSubstitution, filterSubstitutions, contextID, attitudeID, requesterNode);
        this.setMatchType(matchType);
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public boolean testReportToSend(Report report) {
        int channelMatchType = getMatchType();
        boolean toBeSentFlag = (channelMatchType == 0) || (channelMatchType == 1 && report.isSign())
                || (channelMatchType == 2 && !report.isSign());
        if (toBeSentFlag == true) {
            System.out.println("The report passed the channel match type test");
            System.out.println();
        } else {
            System.out.println("The report failed the channel match type test since match type = " + channelMatchType
                    + " and report's sign " + report.isSign());
            System.out.println();
        }

        return toBeSentFlag && super.testReportToSend(report);

    }
}
