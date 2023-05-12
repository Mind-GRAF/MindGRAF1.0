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
        return false;

    }
}
