package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Pair;

public class WhenDoNode extends RuleNode {

    public WhenDoNode(DownCableSet downCableSet) {
        super(downCableSet);
        // TODO Auto-generated constructor stub
    }

    protected void processSingleReports(Report currentReport) throws NoSuchTypeException, DirectCycleException {
        System.out.println(this.getName() + " Processing Reports as a WhenDo node");
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;
        String currentReportContextName = currentReport.getContextName();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(currentReportSubs);
        Substitutions switchSubs = new Substitutions();

        Channel tempChannel = new Channel(switchSubs, currentReportSubs, currentReportContextName,
                0, this);
        Request tempRequest = new Request(tempChannel, null);
        if (forwardReportType) {
            if (!this.isOpen()) {
                if (!supported(currentReportContextName, 0, 0)
                        && currentReport.getReportType() == ReportType.WhenRule) {
                            
                    // The rule is not asserted and the "when" part is asserted with forward
                    // inference
                    if (this.isForwardReport() == false) {
                        this.setForwardReport(true);
                    }

                    super.processSingleRequests(tempRequest);
                } else if (supported(currentReportContextName, 0, 0)) {

                    if (currentReport.getReportType() == ReportType.WhenRule) {
                        
                        // The rule is already asserted and the "when" part is asserted with forward
                        // inference.
                        
                        currentReport.getSupport().combineNode(0, this);
                        ((WhenDoNode) this).applyRuleHandler(currentReport);
                    } else {
                        // The "when" part is already asserted and the rule is asserted with forward
                        // inference.
                        // The "when" part is not asserted and the rule is asserted with forward
                        // inference.
                        NodeSet whenNodes = getDownWhenNodeSet(currentReportAttitudeID);
                        if (this.isForwardReport() == false) {
                            this.setForwardReport(true);
                        }
                        sendRequestsToNodeSet(whenNodes, currentReportSubs, null, currentReportContextName,
                                currentReportAttitudeID,
                                ChannelType.WhenRule, this);

                    }
                }

            } else {

                Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                if (knownInstances.getPositiveKInstances().containsKey(currentReportAttitudeID)) {
                    Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                            .get(currentReportAttitudeID).values();
                    for (KnownInstance currentKIPve : collectionOfSetsPve) {
                        theKnownInstanceSet.add(currentKIPve);
                    }

                }
                knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                boolean validKnownInstanceFound = false;
                for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                    Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                    boolean compatibilityCheck = currentKISubs
                            .compatible(onlySubsBindFreeVar);
                    boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                            currentReportContextName,
                            currentReportAttitudeID);
                    if (compatibilityCheck && supportCheck) {
                        validKnownInstanceFound = true;

                        if (notBound) {
                            currentReport.setSubstitutions(Substitutions.union(currentReportSubs,
                                    currentKnownInstance.getSubstitutions()));
                        }
                        if (currentReport.getSupport() != null) {
                            currentReport.getSupport().combine(0, currentKnownInstance.getSupports());

                        }

                        if (currentReport.getReportType() == ReportType.WhenRule) {
                            applyRuleHandler(currentReport);
                        } else {
                            NodeSet whenNodes = getDownWhenNodeSet(currentReportAttitudeID);
                            if (this.isForwardReport() == false) {
                                this.setForwardReport(true);
                            }
                            sendRequestsToNodeSet(whenNodes, currentReportSubs, null, currentReportContextName,
                                    currentReportAttitudeID,
                                    ChannelType.WhenRule, this);
                        }
                    }
                }
                if (!validKnownInstanceFound) {
                    if (this.isForwardReport() == false) {
                        this.setForwardReport(true);
                    }
                    super.processSingleRequests(tempRequest);
                }

            }
        } else if (isForwardReport()) {
           // System.out.println(currentReport.getReportType());
            if (currentReport.getReportType() == ReportType.WhenRule) {
                System.out.println("Hey");
                if (!this.isOpen()) {
                    if (currentReport.isSign()) {
                        // backwardInference of when part
                        currentReport.getSupport().combineNode(0, this);
                        ((WhenDoNode) this).applyRuleHandler(currentReport);
                    }
                } else {
                    Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                    if (knownInstances.getPositiveKInstances().containsKey(currentReportAttitudeID)) {
                        Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                                .get(currentReportAttitudeID).values();
                        for (KnownInstance currentKIPve : collectionOfSetsPve) {
                            theKnownInstanceSet.add(currentKIPve);
                        }

                    }
                    knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean compatibilityCheck = currentKISubs
                                .compatible(onlySubsBindFreeVar);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);

                        if (compatibilityCheck && supportCheck) {
                            if (notBound) {
                                currentReport.setSubstitutions(Substitutions.union(currentReportSubs,
                                        currentKnownInstance.getSubstitutions()));
                            }
                            if (currentReport.getSupport() != null) {
                                currentReport.getSupport().combine(0,currentKnownInstance.getSupports());
                            }
                            this.applyRuleHandler(currentReport);
                        }
                    }

                }
            } else {
                // backwardInference of when part
                
                if (!this.isOpen()) {
                    Scheduler.addNodeAssertionThroughBReport(currentReport, this);
                }
                if (supported(currentReportContextName, 0, 0)) {
                    currentReport.getSupport().combineNode(0, this);
                    ((WhenDoNode) this).applyRuleHandler(currentReport);
                }
            
            }
        } else {
            super.processSingleReports(currentReport);
        }

    }

    public void applyRuleHandler(Report report) throws NoSuchTypeException {
        NodeSet acts = this.getDownDoNodeSet();
        int currentAttitudeId = report.getAttitude();
        if (report.isSign()) {
            ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>> support = new ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>>();
            if (report.getSupport() != null) {
                for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> currSupport : report
                        .getSupport()
                        .getJustificationSupport().get(0).get(currentAttitudeId)) {
                    HashMap<Integer, PropositionNodeSet> hash = new HashMap<>();
                    for (Integer innerAttitude : currSupport.getFirst().keySet()) {
                        PropositionNodeSet currNodeSet = currSupport.getFirst().get(currentAttitudeId).getFirst();
                        hash.put(innerAttitude, currNodeSet);
                    }
                    support.add(new Pair(hash, new HashMap<Integer, PropositionNodeSet>()));
                }

            }

            for (Node act : acts) {
                if (act instanceof ActNode) {
                    if (!act.isOpen()) {
                        ((ActNode) act).addToSupports(support);
                        if (!Scheduler.getHighActQueue().contains((ActNode) act)) {
                            if (Scheduler.getActQueue().contains((ActNode) act)) {
                                Scheduler.getActQueue().remove((ActNode) act);
                            }
                            ((ActNode) act).restartAgenda();
                            Scheduler.addToHighActQueue((ActNode) act);
                            System.out
                                    .println("Act" + act + " scheduled successfully on the high act stack");
                        }
                    } else {
                        ActNode newAct = (ActNode) ((ActNode) act).applySubstitution(report.getSubstitutions());
                        newAct.addToSupports(support);
                        if (!Scheduler.getHighActQueue().contains((ActNode) newAct)) {
                            if (Scheduler.getActQueue().contains((ActNode) newAct)) {
                                Scheduler.getActQueue().remove((ActNode) newAct);
                            }
                            newAct.restartAgenda();
                            Scheduler.addToHighActQueue((ActNode) newAct);
                            System.out
                                    .println("Act after applying substitutions" + newAct + " scheduled successfully on the high act stack");
                        }
                    }
                }
            }
        }
    }

    @Override
    public RuleInfoSet[] mayInfer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mayInfer'");
    }

    @Override
    public void sendInferenceReports(HashMap<RuleInfo, edu.guc.mind_graf.mgip.reports.Report> reports) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendInferenceReports'");
    }

}
