package edu.guc.mind_graf.mgip.requests;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.support.Support;

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

        return toBeSentFlag && super.testReportToSend(report);

    }
}
