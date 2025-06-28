package edu.guc.mind_graf.informationrev;

import java.util.*;
import java.util.stream.Collectors;
import edu.guc.mind_graf.support.Pair;

public class InformationState {
    private final HashSet<String> validPropositions;
    private final HashSet<String> validContexts;
    private final HashSet<String> validSources;
    private final HashMap<String, Set<String>> propositionsInContext;
    private final HashMap<String, Double> beliefBase;
    private final HashMap<Pair<String, String>, Double> trustBase;
    private final HashSet<String> supportForPropositions;
    private final LinkedHashSet<Pair<String, Pair<String, String>>> history;

    public InformationState() {
        this.validPropositions = new HashSet<>();
        this.validContexts = new HashSet<>();
        this.validSources = new HashSet<>();
        this.propositionsInContext = new HashMap<>();
        this.beliefBase = new HashMap<>();
        this.trustBase = new HashMap<>();
        this.supportForPropositions = new HashSet<>();
        this.history = new LinkedHashSet<>();
    }

    public void addInformation(String proposition, String context, String source, double beliefDegree, double trustDegree, String support) {
        if (!validateDegree(beliefDegree)) {
            throw new IllegalArgumentException("Degree of belief must be between 0.0 and 1.0 (got " + beliefDegree + ")");
        }
        if (!validateDegree(trustDegree)) {
            throw new IllegalArgumentException("Degree of trust must be between 0.0 and 1.0 (got " + trustDegree + ")");
        }
        addSupportForProposition(proposition, support);
        String contradiction;
        if (proposition.startsWith("¬")) {
            contradiction = proposition.substring(1);
        } else {
            contradiction = "¬" + proposition;
        }
        jointRevisionWave(proposition, context, source, contradiction, beliefDegree, trustDegree);
    }

    public static boolean validateDegree(double value) {
        return value >= 0.0 && value <= 1.0;
    }

    public void moreEntrenched(String proposition, double newBeliefDegree) {
        if (!validPropositions.contains(proposition)) {
            validPropositions.add(proposition);
            beliefBase.put(proposition, newBeliefDegree);
        } else {
            double currentBeliefDegree = beliefBase.get(proposition);
            if (newBeliefDegree > currentBeliefDegree) {
                beliefBase.put(proposition, newBeliefDegree);
            }
        }
    }

    public void addPropositionToContext(String proposition, String context) {
        if (!validContexts.contains(context)) {
            validContexts.add(context);
            propositionsInContext.put(context, new HashSet<>(Set.of(proposition)));
        } else propositionsInContext.get(context).add(proposition);
    }

    public void addSourceWithContext(String context, String source, double trustDegree) {
        Pair<String, String> key = new Pair<>(source, context);
        trustBase.put(key, trustDegree);
        validSources.add(source);
    }

    public void moreTrusted(String source, String context, double newTrustDegree) {
        Pair<String, String> key = new Pair<>(source, context);
        if (!trustBase.containsKey(key) || newTrustDegree > trustBase.get(key)) {
            trustBase.put(key, newTrustDegree);
        }
    }

    public void addSupportForProposition(String proposition, String support) {
        if(!support.isEmpty()) {
            supportForPropositions.add(proposition);
        }
    }

    public Set<String> getPropositionsBySource(String source) {
        if (!validSources.contains(source)) {
            return Collections.emptySet();
        }
        return history.stream().filter(pair -> pair.getSecond().getFirst().equals(source)).map(Pair::getFirst).collect(Collectors.toSet());
    }

    public Set<Pair<String, String>> getSourcesByProposition(String proposition) {
        if (!validPropositions.contains(proposition)) {
            return Collections.emptySet();
        }
        return history.stream().filter(pair -> pair.getFirst().equals(proposition)).map(Pair::getSecond).collect(Collectors.toSet());
    }

    public HashSet<String> getValidPropositions() {
        return validPropositions;
    }

    public HashSet<String> getValidSources() {
        return validSources;
    }

    public void chooseValidProposition(String proposition, String contradiction, double beliefDegree, double trustDegree) {
        double contradictionTrust = 0.0;
        double propositionTrust = trustDegree;
        for(Pair<String, Pair<String, String>> entry : history) {
            double trustValue = trustBase.getOrDefault(entry.getSecond(), 0.0);
            if(entry.getFirst().equals(contradiction)) {
                contradictionTrust = Math.max(contradictionTrust, trustValue);
            }
            if(entry.getFirst().equals(proposition)) {
                propositionTrust = Math.max(propositionTrust, trustValue);
            }
        }
        if(propositionTrust <= 0.5 && contradictionTrust <= 0.5) {
            beliefBase.put(proposition, 0.5);
            beliefBase.put(contradiction, 0.5);
            return;
        }
        moreEntrenched(proposition, beliefDegree);
        double propositionBelief = beliefBase.get(proposition);
        double contradictionBelief = beliefBase.get(contradiction);
        if(propositionTrust > contradictionTrust) {
            confirmationPropagationWave(proposition, true);
            refutationPropagationWave(contradiction, false);
        }else if(propositionTrust < contradictionTrust) {
            confirmationPropagationWave(contradiction, false);
            refutationPropagationWave(proposition, true);
        }else {
            boolean supportedProposition = supportForPropositions.contains(proposition);
            boolean supportedContradiction = supportForPropositions.contains(contradiction);

            if(supportedProposition ^ supportedContradiction) {
                if(supportedProposition) {
                    confirmationPropagationWave(proposition, true);
                    refutationPropagationWave(contradiction, false);
                }else {
                    confirmationPropagationWave(contradiction, false);
                    refutationPropagationWave(proposition, true);
                }
                return;
            }
            if(propositionBelief > contradictionBelief) {
                confirmationPropagationWave(proposition, true);
                refutationPropagationWave(contradiction, false);
            }else if(propositionBelief < contradictionBelief) {
                confirmationPropagationWave(contradiction, false);
                refutationPropagationWave(proposition, true);
            }else {
                beliefBase.put(proposition, 0.5);
                beliefBase.put(contradiction, 0.5);
            }
        }
    }
    public void jointRevisionWave(String proposition, String context, String source, String contradiction, double beliefDegree, double trustDegree) {
        boolean propositionExists = propositionsInContext.getOrDefault(context, new HashSet<>()).contains(proposition);
        boolean contradictionExists = propositionsInContext.getOrDefault(context, new HashSet<>()).contains(contradiction);
        history.add(new Pair<>(proposition, new Pair<>(source, context)));
        if(!propositionExists && !contradictionExists) {
            moreEntrenched(proposition, beliefDegree);
            addPropositionToContext(proposition, context);
            validPropositions.add(proposition);
        } else if(!contradictionExists) {
            if(trustDegree > 0.5) {
                confirmationPropagationWave(proposition, true);
            }
            moreEntrenched(proposition, beliefDegree);
        } else {
            if(trustDegree > 0.5)
                chooseValidProposition(proposition, contradiction, beliefDegree, trustDegree);
            addPropositionToContext(proposition, context);
            validPropositions.add(proposition);
        }
        moreTrusted(source, context, trustDegree);
        addSourceWithContext(context, source, trustDegree);
    }

    public void refutationPropagationWave(String proposition, boolean sameProposition){
        Set<String> sources = getSourcesByProposition(proposition)
                .stream()
                .map(Pair::getFirst)
                .collect(Collectors.toSet());

        HashMap<Pair<String, String>, Double> updatedTrustDegrees = new HashMap<>();
        for (Map.Entry<Pair<String, String>, Double> entry : trustBase.entrySet()) {
            Pair<String, String> key = entry.getKey();
            String sourceTemp = key.getFirst();
            if (sources.contains(sourceTemp)) {
                double currentTrust = entry.getValue();
                double newTrust = Math.max(0.0, Math.min(currentTrust - 0.1, 1.0));
                updatedTrustDegrees.put(key, newTrust);
            }
        }
        trustBase.putAll(updatedTrustDegrees);

        HashMap<String, Double> updatedBeliefDegrees = new HashMap<>();
        for(Pair<String, Pair<String, String>> entry : history) {
            if(sources.contains(entry.getSecond().getFirst())) {
                String otherProposition = entry.getFirst();
                double currentBelief = beliefBase.get(otherProposition);
                if(proposition.equals(otherProposition)) {
                    if(currentBelief >= 0.5) {
                        updatedBeliefDegrees.put(proposition, 0.4);
                        continue;
                    }
                    if(sameProposition) continue;
                }
                double newBelief = Math.max(0.0, Math.min(currentBelief - 0.1, 1.0));
                updatedBeliefDegrees.put(otherProposition, newBelief);
            }
        }
        beliefBase.putAll(updatedBeliefDegrees);
    }

    public void confirmationPropagationWave(String proposition, boolean sameProposition){
        Set<String> sources = getSourcesByProposition(proposition)
                .stream()
                .map(Pair::getFirst)
                .collect(Collectors.toSet());

        HashMap<Pair<String, String>, Double> updatedTrustDegrees = new HashMap<>();
        for (Map.Entry<Pair<String, String>, Double> entry : trustBase.entrySet()) {
            Pair<String, String> key = entry.getKey();
            String sourceTemp = key.getFirst();
            if (sources.contains(sourceTemp)) {
                double currentTrust = entry.getValue();
                double newTrust = Math.max(0.0, Math.min(currentTrust + 0.1, 1.0));
                updatedTrustDegrees.put(key, newTrust);
            }
        }
        trustBase.putAll(updatedTrustDegrees);

        HashMap<String, Double> updatedBeliefDegrees = new HashMap<>();
        for(Pair<String, Pair<String, String>> entry : history) {
            if(sources.contains(entry.getSecond().getFirst())) {
                String otherProposition = entry.getFirst();
                double currentBelief = beliefBase.get(otherProposition);
                if(proposition.equals(otherProposition)) {
                    if(currentBelief <= 0.5) {
                        updatedBeliefDegrees.put(proposition, 0.6);
                        continue;
                    }
                    if(sameProposition) continue;
                }
                double newBelief = Math.max(0.0, Math.min(currentBelief + 0.1, 1.0));
                updatedBeliefDegrees.put(otherProposition, newBelief);
            }
        }
        beliefBase.putAll(updatedBeliefDegrees);
    }
}




