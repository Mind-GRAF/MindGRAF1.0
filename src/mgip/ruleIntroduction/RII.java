package mgip.ruleIntroduction;

import context.Context;
import mgip.requests.AntecedentToRuleChannel;
import mgip.requests.Request;
import mgip.requests.RuleToConsequentChannel;
import set.NodeSet;
import set.ReportSet;

public class RII {
    private Request request;
    private NodeSet antNodes;
    private NodeSet conqArgNodes;
    private Context context;
    private int attitudeID;
    private ReportSet reportSet;
    private int posCount;
    private int negCount;
    private boolean isSufficent;

    public RII(Request request, NodeSet antNodes, NodeSet conqArgNodes, Context context, int attitudeID) {
        this.request = request;
        this.antNodes = antNodes;
        this.conqArgNodes = conqArgNodes;
        this.context = context;
        this.attitudeID = attitudeID;
        this.reportSet = null;
        this.posCount = 0;
        this.negCount = 0;
        this.isSufficent = false;
    }

    public void setReportSet(ReportSet reportSet) {
        this.reportSet = reportSet;
    }

    public void addPosReport() {
        this.posCount++;
    }

    public void addNegReport() {
        this.negCount++;
    }

    public void setSufficent() {
        this.isSufficent = true;
    }

    public boolean isSufficent() {
        return this.isSufficent;
    }

    public int getPosCount() {
        return this.posCount;
    }

    public int getNegCount() {
        return this.negCount;
    }

    public Request getRequest() {
        return this.request;
    }

    public NodeSet getAntNodes() {
        return this.antNodes;
    }

    public NodeSet getConqArgNodes() {
        return this.conqArgNodes;
    }

    public Context getContext() {
        return this.context;
    }

    public int getAttitudeID() {
        return this.attitudeID;
    }

    public ReportSet getReportSet() {
        return this.reportSet;
    }

}
