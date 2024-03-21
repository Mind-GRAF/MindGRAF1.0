package mgip.ruleIntroduction;

import context.Context;
import mgip.requests.AntecedentToRuleChannel;
import mgip.requests.Request;
import mgip.requests.RuleToConsequentChannel;
import set.NodeSet;
import set.ReportSet;

public class RII {
    Request request;
    NodeSet argNodes;
    NodeSet conqNodes;
    Context context;
    int attitudeID;
    ReportSet reportSet;
    int posCount;
    int negCount;
    boolean isSufficent;

    public RII(Request request, NodeSet argNodes, NodeSet conqNodes, Context context, int attitudeID) {
        this.request = request;
        this.argNodes = argNodes;
        this.conqNodes = conqNodes;
        this.context = context;
        this.attitudeID = attitudeID;
        this.reportSet = null;
        this.posCount = 0;
        this.negCount = 0;
        this.isSufficent = false;
    }

}
