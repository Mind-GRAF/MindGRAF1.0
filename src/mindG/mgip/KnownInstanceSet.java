package mindG.mgip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import mindG.mgip.matching.Substitutions;
import mindG.network.PropositionSet;

public class KnownInstanceSet implements Iterable<KnownInstance> {

    static Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances;
    static Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances;

    public KnownInstanceSet() {
        positiveKInstances = new Hashtable<Integer, Hashtable<Substitutions, KnownInstance>>();
        negativeKInstances = new Hashtable<Integer, Hashtable<Substitutions, KnownInstance>>();

    }

    public void addKnownInstance(Report Report) {
        Boolean ReportSign = Report.isSign();
        Substitutions ReportSubs = Report.getSubstitutions();
        PropositionSet Supports = Report.getSupport();
        int attitude = Report.getAttitude();
        if (ReportSign) {
            Hashtable<Substitutions, KnownInstance> targetSet = positiveKInstances.remove(attitude);
            if (targetSet == null) {
                targetSet = new Hashtable<Substitutions, KnownInstance>();
                KnownInstance targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                positiveKInstances.put(attitude, targetSet);
            } else {
                KnownInstance targetKnownInstance = targetSet.remove(ReportSubs);
                if (targetKnownInstance == null) {
                    targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);

                } else {
                    PropositionSet supportSet = targetKnownInstance.getSupports();
                    // targetKnownInstance.setSupports(union(Supports, supportSet));
                    // method hazem haye3melha ye2add in a set of nodes a set of nodes

                }

                targetSet.put(ReportSubs, targetKnownInstance);
                positiveKInstances.put(attitude, targetSet);
            }

        }

        else {
            Hashtable<Substitutions, KnownInstance> targetSet = negativeKInstances.remove(attitude);
            if (targetSet == null) {
                targetSet = new Hashtable<Substitutions, KnownInstance>();
                KnownInstance targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                negativeKInstances.put(attitude, targetSet);
            } else {
                KnownInstance targetKnownInstance = targetSet.remove(ReportSubs);
                if (targetKnownInstance == null) {
                    targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                    targetSet.put(ReportSubs, targetKnownInstance);
                    negativeKInstances.put(attitude, targetSet);
                } else {
                    PropositionSet supportSet = targetKnownInstance.getSupports();
                    // supportSet = union(supportSet, Supports);
                    // method hazem haye3melha ye2add in a set of nodes a set of nodes

                }
            }

        }

    }

    public static int[] union(int[] arr1, int[] arr2) {
        int[] result = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static Collection<KnownInstance> mergeKInstancesBasedOnAtt(
            int i) {
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

        if (getPositiveKInstances().containsKey(i)) {
            Collection<KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(i).values();
            Iterator ReportIteratorPve = collectionOfSetsPve.iterator();
            for (KnownInstance currentKIPve : collectionOfSetsPve) {
                theKnownInstanceSet.add(currentKIPve);
            }

        }

        if (getNegativeKInstances().containsKey(i)) {

            Collection<KnownInstance> collectionOfSetsNve = getNegativeKInstances().get(i).values();
            Iterator ReportIteratorNve = collectionOfSetsNve.iterator();
            for (KnownInstance currentKINve : collectionOfSetsNve) {
                theKnownInstanceSet.add(currentKINve);
            }

        }

        return theKnownInstanceSet;

    }

    public static void printKnownInstanceSet(Collection<KnownInstance> theKnownInstanceSet) {
        if (theKnownInstanceSet == null) {
            System.out.println("Know Instance set is null");
        } else {
            for (KnownInstance ki : theKnownInstanceSet) {
                System.out.println("Substitutions: " + ki.getSubstitutions());
                // System.out.println("Supports: " + Arrays.deepToString(ki.getSupports()));
                System.out.println("AttitudeID: " + ki.getAttitudeID());
                System.out.println();
            }
        }

    }

    public static Collection<KnownInstance> getPositiveCollectionbyAttribute(
            int attributeID) {
        if (getPositiveKInstances().containsKey(attributeID)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(attributeID);
            Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsPve.values();
            return theKnownInstanceSet;

        }
        System.out.println("there is no Pve Known Instance with the attitude you are looking for");

        return null;

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

    public static Collection<KnownInstance> getNegativeCollectionbyAttribute(
            int attributeID) {

        if (getNegativeKInstances().containsKey(attributeID)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsNve = getNegativeKInstances().get(attributeID);
            Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsNve.values();
            return theKnownInstanceSet;
        }
        System.out.println("there is no Nve Known Instance with the attitude you are looking for");

        return null;

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

    public static Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getPositiveKInstances() {
        return positiveKInstances;
    }

    public void setPositiveKInstances(Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances) {
        this.positiveKInstances = positiveKInstances;
    }

    public static Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getNegativeKInstances() {
        return negativeKInstances;
    }

    public void setNegativeKInstances(Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances) {
        this.negativeKInstances = negativeKInstances;
    }

    public KnownInstance getKnownInstanceByAttSubNve(int reportAttitude, Substitutions reportSubs) {
        if (getNegativeKInstances().containsKey(reportAttitude)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsNve = getNegativeKInstances().get(reportAttitude);
            if (collectionOfSetsNve.containsKey(reportSubs)) {
                KnownInstance myKnownInstance = collectionOfSetsNve.get(reportSubs);
                return myKnownInstance;
            }

        }
        System.out.println(
                "there is no Nve Known Instance with either the attitude or the substitutions you are looking for");

        return null;
    }

    public static KnownInstance getKnownInstanceByAttSubPve(int reportAttitude, Substitutions reportSubs) {
        if (getPositiveKInstances().containsKey(reportAttitude)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(reportAttitude);
            if (collectionOfSetsPve.containsKey(reportSubs)) {
                KnownInstance myKnownInstance = collectionOfSetsPve.get(reportSubs);
                return myKnownInstance;
            }

        }
        System.out.println(
                "there is no Pve Known Instance with either the attitude or the substitutions you are looking for");
        return null;
    }

    @Override
    public Iterator<KnownInstance> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    public static void printKnownInstances(
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances,
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances) {
        System.out.println("Positive Known Instances:");
        for (int key : positiveKInstances.keySet()) {
            System.out.println("Attitude " + key + ":");
            Hashtable<Substitutions, KnownInstance> instances = positiveKInstances.get(key);
            for (Substitutions instance : instances.keySet()) {
                System.out.println(instance + " : " + instances.get(instance).toString());
            }
        }

        System.out.println("Negative Known Instances:");
        for (int key : negativeKInstances.keySet()) {
            System.out.println("Attitude " + key + ":");
            Hashtable<Substitutions, KnownInstance> instances = negativeKInstances.get(key);
            for (Substitutions instance : instances.keySet()) {
                System.out.println(instance + " : " + instances.get(instance).toString());
            }
        }
    }

}
