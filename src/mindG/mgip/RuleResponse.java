package mindG.mgip;

import java.util.ArrayList;
import java.util.Collection;

import mindG.mgip.requests.Channel;

public class RuleResponse {
    private Report report;
    private Collection<Channel> withConsequentRequests;

    public RuleResponse() {
        withConsequentRequests = new ArrayList<Channel>();
    }

    public void addReport(Report report) {
        this.report = report;
    }

    public void addRequest(Channel Request) {
        this.withConsequentRequests.add(Request);
    }
}
