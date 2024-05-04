package edu.guc.mind_graf.mgip.ruleIntroduction;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.ReportSet;


public class RII {
    private Request request;
    private NodeSet antNodes;
    private NodeSet conqArgNodes;
    private Context context;
    private int attitudeID;
    private ReportSet reportSet;
    private int posCount;
    private int negCount;
    private boolean isSufficient;

    public RII(Request request, NodeSet antNodes, NodeSet conqArgNodes, Context context, int attitudeID) {
        this.request = request;
        this.antNodes = antNodes;
        this.conqArgNodes = conqArgNodes;
        this.context = context;
        this.attitudeID = attitudeID;
        this.reportSet = new ReportSet();
        this.posCount = 0;
        this.negCount = 0;
        this.isSufficient = false;
    }

    public void addPosReport() {
        this.posCount++;
    }

    public void addNegReport() {
        this.negCount++;
    }

    public void setSufficent() {
        this.isSufficient = true;
    }

    public boolean isSufficient() {
        return this.isSufficient;
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

    public void update(Report report) {
        if(report.isSign()){
            addPosReport();
            reportSet.add(report);
        }
        else{
            addNegReport();
            reportSet.add(report);
        }
    }
}

