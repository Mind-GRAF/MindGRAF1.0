package mindG.mgip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import mindG.mgip.matching.Substitutions;
import mindG.network.PropositionSet;

public class KnownInstanceSet {

    Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances;
    Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances;

    public KnownInstanceSet() {
        positiveKInstances = new Hashtable<Integer, Hashtable<Substitutions, KnownInstance>>();
        positiveKInstances.put(1, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(2, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(3, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(4, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(5, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(6, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(7, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(8, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(9, new Hashtable<Substitutions, KnownInstance>());
        positiveKInstances.put(10, new Hashtable<Substitutions, KnownInstance>());

        negativeKInstances = new Hashtable<Integer, Hashtable<Substitutions, KnownInstance>>();
        negativeKInstances.put(1, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(2, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(3, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(4, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(5, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(6, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(7, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(8, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(9, new Hashtable<Substitutions, KnownInstance>());
        negativeKInstances.put(10, new Hashtable<Substitutions, KnownInstance>());

    }

    public void addKnownInstance(Report Report) {
        Boolean ReportSign = Report.isSign();
        Substitutions ReportSubs = Report.getSubstitutions();
        PropositionSet Supports = Report.getSupport();
        int attitude = Report.getAttitude();
        if (ReportSign) {
            Hashtable<Substitutions, KnownInstance> targetSet = positiveKInstances.get(attitude);
            KnownInstance targetKnownInstance = targetSet.get(ReportSubs);
            if (targetKnownInstance == null) {
                targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                positiveKInstances.put(attitude, targetSet);

            } else {
                PropositionSet supportSet = targetKnownInstance.getSupports();
                supportSet.add(Supports);
                // method hazem haye3melha ye2add in a set of nodes a set of nodes

            }

        } else {
            Hashtable<Substitutions, KnownInstance> targetSet = negativeKInstances.get(attitude);
            KnownInstance targetKnownInstance = targetSet.get(ReportSubs);
            if (targetKnownInstance == null) {
                targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                negativeKInstances.put(attitude, targetSet);

            } else {
                PropositionSet supportSet = targetKnownInstance.getSupports();
                supportSet.add(Supports);
                // method hazem haye3melha ye2add in a set of nodes a set of nodes

            }

        }

    }

    public Collection<KnownInstance> mergeKInstancesBasedOnAtt(
            int i) {
        Collection<KnownInstance> collectionOfSetsPve = this.positiveKInstances.get(i).values();
        Collection<KnownInstance> collectionOfSetsNve = this.negativeKInstances.get(i).values();
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();
        Iterator ReportIteratorPve = collectionOfSetsPve.iterator();
        Iterator ReportIteratorNve = collectionOfSetsNve.iterator();
        for (KnownInstance currentKIPve : collectionOfSetsPve) {

            theKnownInstanceSet.add(currentKIPve);

        }
        for (KnownInstance currentKINve : collectionOfSetsNve) {
            theKnownInstanceSet.add(currentKINve);

        }

        return theKnownInstanceSet;

    }

    public Collection<KnownInstance> getPositiveCollectionbyAttribute(
            int attributeID) {
        Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = this.positiveKInstances.get(attributeID);
        Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsPve.values();
        return theKnownInstanceSet;

    }

    public Collection<KnownInstance> getPositiveCollection(
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances) {
        Collection<Hashtable<Substitutions, KnownInstance>> collectionOfSetsPve = positiveKInstances.values();
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();
        Iterator ReportIteratorPve = collectionOfSetsPve.iterator();
        for (Hashtable<Substitutions, KnownInstance> currentKIPve : collectionOfSetsPve) {
            for (KnownInstance currentKnownInstancePve : currentKIPve.values()) {
                theKnownInstanceSet.add(currentKnownInstancePve);

            }
        }
        return theKnownInstanceSet;

    }

    public Collection<KnownInstance> getNegativeCollectionbyAttribute(
            int attributeID) {
        Hashtable<Substitutions, KnownInstance> collectionOfSetsNve = this.negativeKInstances.get(attributeID);
        Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsNve.values();
        return theKnownInstanceSet;

    }

    public Collection<KnownInstance> getNegativeCollection(
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances) {
        Collection<Hashtable<Substitutions, KnownInstance>> collectionOfSetsNve = negativeKInstances.values();
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();
        Iterator ReportIteratorPve = collectionOfSetsNve.iterator();
        for (Hashtable<Substitutions, KnownInstance> currentKINve : collectionOfSetsNve) {
            for (KnownInstance currentKnownInstanceNve : currentKINve.values()) {
                theKnownInstanceSet.add(currentKnownInstanceNve);

            }
        }
        return theKnownInstanceSet;

    }

    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getPositiveKInstances() {
        return positiveKInstances;
    }

    public void setPositiveKInstances(Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances) {
        this.positiveKInstances = positiveKInstances;
    }

    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getNegativeKInstances() {
        return negativeKInstances;
    }

    public void setNegativeKInstances(Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances) {
        this.negativeKInstances = negativeKInstances;
    }

    public KnownInstance getAlmostReportByAttSubNve(int reportAttitude, Substitutions reportSubs) {
        Hashtable<Substitutions, KnownInstance> collectionOfSetsNve = this.negativeKInstances.get(reportAttitude);

        KnownInstance myKnownInstance = collectionOfSetsNve.get(reportSubs);

        return myKnownInstance;
    }

    public KnownInstance getAlmostReportByAttSubPve(int reportAttitude, Substitutions reportSubs) {
        Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = this.positiveKInstances.get(reportAttitude);

        KnownInstance myKnownInstance = collectionOfSetsPve.get(reportSubs);

        return myKnownInstance;
    }

}
