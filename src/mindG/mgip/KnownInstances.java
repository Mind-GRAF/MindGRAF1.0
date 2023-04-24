package mindG.mgip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import mindG.mgip.matching.Substitutions;
import mindG.network.*;

public class KnownInstances {

    Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> positiveKInstances;
    Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> negativeKInstances;

    public KnownInstances() {
        positiveKInstances = new Hashtable<Integer, Hashtable<Substitutions, AlmostReports>>();
        positiveKInstances.put(1, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(2, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(3, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(4, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(5, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(6, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(7, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(8, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(9, new Hashtable<Substitutions, AlmostReports>());
        positiveKInstances.put(10, new Hashtable<Substitutions, AlmostReports>());

        negativeKInstances = new Hashtable<Integer, Hashtable<Substitutions, AlmostReports>>();
        negativeKInstances.put(1, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(2, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(3, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(4, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(5, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(6, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(7, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(8, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(9, new Hashtable<Substitutions, AlmostReports>());
        negativeKInstances.put(10, new Hashtable<Substitutions, AlmostReports>());

    }

    public void addAlmostReport(Report Report) {
        Boolean ReportSign = Report.isSign();
        Substitutions ReportSubs = Report.getSubstitutions();
        PropositionSet Supports = Report.getSupport();
        int attitude = Report.getAttitude();
        if (ReportSign) {
            Hashtable<Substitutions, AlmostReports> targetSet = positiveKInstances.get(attitude);
            AlmostReports targetAlmostReport = targetSet.get(ReportSubs);
            if (targetAlmostReport == null) {
                targetAlmostReport = new AlmostReports(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetAlmostReport);
                positiveKInstances.put(attitude, targetSet);

            } else {
                PropositionSet supportSet = targetAlmostReport.getSupports();
                supportSet.add(Supports);
                // method hazem haye3melha ye2add in a set of nodes a set of nodes

            }

        } else {
            Hashtable<Substitutions, AlmostReports> targetSet = negativeKInstances.get(attitude);
            AlmostReports targetAlmostReport = targetSet.get(ReportSubs);
            if (targetAlmostReport == null) {
                targetAlmostReport = new AlmostReports(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetAlmostReport);
                negativeKInstances.put(attitude, targetSet);

            } else {
                PropositionSet supportSet = targetAlmostReport.getSupports();
                supportSet.add(Supports);
                // method hazem haye3melha ye2add in a set of nodes a set of nodes

            }

        }

    }

    public Collection<AlmostReports> mergeKInstances(
            Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> positiveKInstances,
            Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> negativeKInstances) {
        Collection<Hashtable<Substitutions, AlmostReports>> collectionOfSetsPve = positiveKInstances.values();
        Collection<Hashtable<Substitutions, AlmostReports>> collectionOfSetsNve = negativeKInstances.values();
        Collection<AlmostReports> theAlmostReportSet = new ArrayList<AlmostReports>();
        Iterator ReportIteratorPve = collectionOfSetsPve.iterator();
        Iterator ReportIteratorNve = collectionOfSetsNve.iterator();
        for (Hashtable<Substitutions, AlmostReports> currentAlmostPveReport : collectionOfSetsPve) {
            for (AlmostReports currentAlmostReportPve : currentAlmostPveReport.values()) {
                theAlmostReportSet.add(currentAlmostReportPve);

            }
        }
        for (Hashtable<Substitutions, AlmostReports> currentAlmostNveReport : collectionOfSetsNve) {
            for (AlmostReports currentAlmostReportNve : currentAlmostNveReport.values()) {
                theAlmostReportSet.add(currentAlmostReportNve);

            }
        }

        return theAlmostReportSet;

    }

    public Collection<AlmostReports> getPositiveCollectionbyAttribute(
            int attributeID) {
        Hashtable<Substitutions, AlmostReports> collectionOfSetsPve = this.getPositiveKInstances().get(attributeID);
        Collection<AlmostReports> theAlmostReportSet = collectionOfSetsPve.values();
        return theAlmostReportSet;

    }

    public Collection<AlmostReports> getPositiveCollection(
            Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> positiveKInstances) {
        Collection<Hashtable<Substitutions, AlmostReports>> collectionOfSetsPve = positiveKInstances.values();
        Collection<AlmostReports> theAlmostReportSet = new ArrayList<AlmostReports>();
        Iterator ReportIteratorPve = collectionOfSetsPve.iterator();
        for (Hashtable<Substitutions, AlmostReports> currentAlmostPveReport : collectionOfSetsPve) {
            for (AlmostReports currentAlmostReportPve : currentAlmostPveReport.values()) {
                theAlmostReportSet.add(currentAlmostReportPve);

            }
        }
        return theAlmostReportSet;

    }

    public Collection<AlmostReports> getNegativeCollectionbyAttribute(
            int attributeID) {
        Hashtable<Substitutions, AlmostReports> collectionOfSetsNve = this.getNegativeKInstances().get(attributeID);
        Collection<AlmostReports> theAlmostReportSet = collectionOfSetsNve.values();
        return theAlmostReportSet;

    }

    public Collection<AlmostReports> getNegativeCollection(
            Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> negativeKInstances) {
        Collection<Hashtable<Substitutions, AlmostReports>> collectionOfSetsNve = negativeKInstances.values();
        Collection<AlmostReports> theAlmostReportSet = new ArrayList<AlmostReports>();
        Iterator ReportIteratorPve = collectionOfSetsNve.iterator();
        for (Hashtable<Substitutions, AlmostReports> currentAlmostNveReport : collectionOfSetsNve) {
            for (AlmostReports currentAlmostReportNve : currentAlmostNveReport.values()) {
                theAlmostReportSet.add(currentAlmostReportNve);

            }
        }
        return theAlmostReportSet;

    }

    public Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> getPositiveKInstances() {
        return positiveKInstances;
    }

    public void setPositiveKInstances(Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> positiveKInstances) {
        this.positiveKInstances = positiveKInstances;
    }

    public Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> getNegativeKInstances() {
        return negativeKInstances;
    }

    public void setNegativeKInstances(Hashtable<Integer, Hashtable<Substitutions, AlmostReports>> negativeKInstances) {
        this.negativeKInstances = negativeKInstances;
    }

}
