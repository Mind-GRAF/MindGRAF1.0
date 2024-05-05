package edu.guc.mind_graf.mgip.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Support;

public class KnownInstanceSet implements Iterable<KnownInstance> {

    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances;
    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances;

    public KnownInstanceSet() {
        positiveKInstances = new Hashtable<>();
        negativeKInstances = new Hashtable<>();

    }

    public boolean addKnownInstance(Report Report) {
        Boolean ReportSign = Report.isSign();
        Substitutions ReportSubs = Report.getSubstitutions();
        Support Supports = Report.getSupport();
        int attitude = Report.getAttitude();
        if (ReportSign) {
            Hashtable<Substitutions, KnownInstance> targetSet = positiveKInstances.remove(attitude);
            if (targetSet == null) {
                targetSet = new Hashtable<Substitutions, KnownInstance>();
                KnownInstance targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                positiveKInstances.put(attitude, targetSet);
                return false;// ya3ni it didn't get handledd
            } else {
                KnownInstance targetKnownInstance;
                for (Substitutions key : targetSet.keySet()) {
                    if (key.compatible(ReportSubs)) {
                        targetKnownInstance = targetSet.remove(key);
                        if (targetKnownInstance == null) {
                            targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                            targetSet.put(ReportSubs, targetKnownInstance);
                            positiveKInstances.put(attitude, targetSet);
                            return false;

                        } else {
                            Support supportSet = targetKnownInstance.getSupports();
                            targetKnownInstance.setSupports(Supports.union(supportSet));
                            targetSet.put(ReportSubs, targetKnownInstance);
                            positiveKInstances.put(attitude, targetSet);
                            return true;
                        }

                    }
                }

            }

        }

        else {
            Hashtable<Substitutions, KnownInstance> targetSet = negativeKInstances.remove(attitude);
            if (targetSet == null) {
                targetSet = new Hashtable<>();
                KnownInstance targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                targetSet.put(ReportSubs, targetKnownInstance);
                negativeKInstances.put(attitude, targetSet);
                return false;
            } else {
                KnownInstance targetKnownInstance;
                for (Substitutions key : targetSet.keySet()) {
                    if (key.compatible(ReportSubs)) {
                        targetKnownInstance = targetSet.remove(key);
                        if (targetKnownInstance == null) {
                            targetKnownInstance = new KnownInstance(ReportSubs, Supports, attitude);
                            targetSet.put(ReportSubs, targetKnownInstance);
                            positiveKInstances.put(attitude, targetSet);

                            return false;
                        } else {
                            Support supportSet = targetKnownInstance.getSupports();
                            targetKnownInstance.setSupports(Supports.union(supportSet));
                            targetSet.put(ReportSubs, targetKnownInstance);
                            positiveKInstances.put(attitude, targetSet);
                            return true;
                        }

                    }
                }
            }

        }
        return false;

    }

    public Collection<KnownInstance> mergeKInstancesBasedOnAtt(
            int i) {
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

        if (getPositiveKInstances().containsKey(i)) {
            Collection<KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(i).values();
            for (KnownInstance currentKIPve : collectionOfSetsPve) {
                theKnownInstanceSet.add(currentKIPve);
            }

        }

        if (getNegativeKInstances().containsKey(i)) {

            Collection<KnownInstance> collectionOfSetsNve = getNegativeKInstances().get(i).values();
            for (KnownInstance currentKINve : collectionOfSetsNve) {
                theKnownInstanceSet.add(currentKINve);
            }

        }

        return theKnownInstanceSet;

    }

    public void printKnownInstanceSet(Collection<KnownInstance> theKnownInstanceSet) {
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

    public Collection<KnownInstance> getPositiveCollectionbyAttribute(
            int attributeID) {
        if (getPositiveKInstances().containsKey(attributeID)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(attributeID);
            Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsPve.values();
            return theKnownInstanceSet;

        }

        return null;

    }

    public Collection<KnownInstance> getPositiveCollection(
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances) {
        Collection<Hashtable<Substitutions, KnownInstance>> collectionOfSetsPve = positiveKInstances.values();
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();
        for (Hashtable<Substitutions, KnownInstance> currentKIPve : collectionOfSetsPve) {
            for (KnownInstance currentKnownInstancePve : currentKIPve.values()) {
                theKnownInstanceSet.add(currentKnownInstancePve);

            }
        }
        return theKnownInstanceSet;

    }

    public Collection<KnownInstance> getNegativeCollectionbyAttribute(
            int attributeID) {

        if (getNegativeKInstances().containsKey(attributeID)) {

            Hashtable<Substitutions, KnownInstance> collectionOfSetsNve = getNegativeKInstances().get(attributeID);
            Collection<KnownInstance> theKnownInstanceSet = collectionOfSetsNve.values();
            return theKnownInstanceSet;
        }

        return null;

    }

    public Collection<KnownInstance> getNegativeCollection(
            Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> negativeKInstances) {
        Collection<Hashtable<Substitutions, KnownInstance>> collectionOfSetsNve = negativeKInstances.values();
        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();
        for (Hashtable<Substitutions, KnownInstance> currentKINve : collectionOfSetsNve) {
            for (KnownInstance currentKnownInstanceNve : currentKINve.values()) {
                theKnownInstanceSet.add(currentKnownInstanceNve);

            }
        }
        return theKnownInstanceSet;

    }

    public void setPositiveKInstances(Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> positiveKInstances) {
        this.positiveKInstances = positiveKInstances;
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

    public KnownInstance getKnownInstanceByAttSubPve(int reportAttitude, Substitutions reportSubs) {
        if (getPositiveKInstances().containsKey(reportAttitude)) {
            System.out.println("ana dakhalttttttttttt");
            Hashtable<Substitutions, KnownInstance> collectionOfSetsPve = getPositiveKInstances().get(reportAttitude);
            for (Substitutions key : collectionOfSetsPve.keySet()) {

                if (key.compatible(reportSubs)) {
                    System.out.println("ana nege7ttttt");

                    KnownInstance myKnownInstance = collectionOfSetsPve.get(key);
                    System.out.println(myKnownInstance.toString());
                    return myKnownInstance;

                }

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

    public void printKnownInstances(
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

    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getPositiveKInstances() {
        return positiveKInstances;
    }

    public Hashtable<Integer, Hashtable<Substitutions, KnownInstance>> getNegativeKInstances() {
        return negativeKInstances;
    }

}
