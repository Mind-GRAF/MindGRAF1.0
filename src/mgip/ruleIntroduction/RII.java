package mgip.ruleIntroduction;

import context.Context;
import mgip.requests.AntecedentToRuleChannel;
import mgip.requests.Request;
import mgip.requests.RuleToConsequentChannel;
import set.ReportSet;

public class RII {
    Request request;
    AntecedentToRuleChannel channel;
    RuleToConsequentChannel ruleToConsequentChannel;
    Context context;
    ReportSet reportSet;
    int posCount;
    int negCount;
    boolean isSufficent;

    public RII(Request request, AntecedentToRuleChannel channel, RuleToConsequentChannel ruleToConsequentChannel,
            Context context, ReportSet reportSet, int posCount, int negCount, boolean isSufficent) {
        this.request = request;
        this.channel = channel;
        this.ruleToConsequentChannel = ruleToConsequentChannel;
        this.context = context;
        this.reportSet = reportSet;
        this.posCount = posCount;
        this.negCount = negCount;
        this.isSufficent = isSufficent;
    }

}
