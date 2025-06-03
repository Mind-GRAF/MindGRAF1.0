package edu.guc.mind_graf.informationrev;

import edu.guc.mind_graf.support.Pair;
import java.util.*;
import java.util.stream.Collectors;


public class InformationState {
    private final HashSet<String> validPropositions;
    private final HashSet<String> validContexts;
    private final HashSet<String> validSources;
    private final HashMap<String, Set<String>> propositionsInContext;
    private final HashMap<String, Double> beliefBase;
    private final HashMap<Pair<String, String>, Double> trustBase;
    private final LinkedHashSet<Pair<String, String>> history;

    public InformationState() {
        this.validPropositions = new HashSet<>();
        this.validContexts = new HashSet<>();
        this.validSources = new HashSet<>();
        this.propositionsInContext = new HashMap<>();
        this.beliefBase = new HashMap<>();
        this.trustBase = new HashMap<>();
        this.history = new LinkedHashSet<>();
    }

    public void addInformation(String proposition, String context, String source, double beliefDegree, double trustDegree) {
        if (!validateDegree(beliefDegree)) {
            throw new IllegalArgumentException("Degree of belief must be between 0.0 and 1.0 (got " + beliefDegree + ")");
        }
        if (!validateDegree(trustDegree)) {
            throw new IllegalArgumentException("Degree of trust must be between 0.0 and 1.0 (got " + trustDegree + ")");
        }
        jointRevisionWave(proposition);
        moreEntrenched(proposition, beliefDegree);
        addPropositionToContext(proposition, context);
        moreTrusted(source, context, trustDegree);
        history.add(new Pair<>(proposition, source));
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
        } else if (!propositionsInContext.get(context).contains(proposition)) {
            propositionsInContext.get(context).add(proposition);
        }
    }

    public void moreTrusted(String source, String context, double newTrustDegree) {
        Pair<String, String> key = new Pair<>(source, context);
        if (!trustBase.containsKey(key)) {
            trustBase.put(key, newTrustDegree);
            if (!validSources.contains(source)) validSources.add(source);
        } else if (newTrustDegree > trustBase.get(key)) {
            trustBase.put(key, newTrustDegree);
        }
    }

    public Set<String> getPropositionsBySource(String source) {
        if (!validSources.contains(source)) {
            throw new IllegalArgumentException("The source '" + source + "' is not to be found.");
        }
        Set<String> props = history.stream().filter(pair -> pair.getSecond().equals(source)).map(Pair::getFirst).collect(Collectors.toSet());
        return props;
    }

    public Set<String> getSourcesByProposition(String proposition) {
        if (!validPropositions.contains(proposition)) {
            throw new IllegalArgumentException("The proposition '" + proposition + "' is not to be found.");
        }
        Set<String> sources = history.stream().filter(pair -> pair.getFirst().equals(proposition)).map(Pair::getSecond).collect(Collectors.toSet());
        return sources;
    }

    public HashSet<String> getValidPropositions() {
        return validPropositions;
    }

    public HashSet<String> getValidSources() {
        return validSources;
    }
    public void jointRevisionWave(String proposition) {
        if (proposition.startsWith("¬")) {
            String positiveProp = proposition.substring(0);
            if (validPropositions.contains(positiveProp)) {

                Set<String> negSources = getSourcesByProposition(proposition);
                Set<String> posSources = getSourcesByProposition(positiveProp);


                double maxNegTrust = getAverageTrustForSources(negSources);
                double maxPosTrust = getAverageTrustForSources(posSources);

                if (maxNegTrust > maxPosTrust) {

                    refutationPropagationWave(positiveProp);
                    confirmationPropagationWave(proposition);
                } else {

                    refutationPropagationWave(proposition);
                    confirmationPropagationWave(positiveProp);
                }
            } else {

                confirmationPropagationWave(proposition);
            }
        }
        else {
            String negatedProp = "¬" + proposition;
            if (validPropositions.contains(negatedProp)) {
                Set<String> posSources = getSourcesByProposition(proposition);
                Set<String> negSources = getSourcesByProposition(negatedProp);

                double maxPosTrust = getAverageTrustForSources(posSources);
                double maxNegTrust = getAverageTrustForSources(negSources);

                if (maxPosTrust > maxNegTrust) {

                    refutationPropagationWave(negatedProp);
                    confirmationPropagationWave(proposition);
                } else {

                    refutationPropagationWave(proposition);
                    confirmationPropagationWave(negatedProp);
                }
            } else {

                confirmationPropagationWave(proposition);
            }
        }
    }
    public void refutationPropagationWave(String proposition){
        if(validPropositions.contains(proposition)) {
            Set<String> sources = getSourcesByProposition(proposition);
            for (Map.Entry<Pair<String, String>, Double> entry : trustBase.entrySet()) {
                Pair<String, String> key = entry.getKey();
                String sourcetemp = key.getFirst();
                if (sources.contains(sourcetemp)) {
                    double currentTrust = entry.getValue();
                    double newTrust = Math.max(0.0, Math.min(currentTrust - 0.2, 1.0));
                    trustBase.put(key, newTrust);
                }
            }
        }
    }

    public void confirmationPropagationWave(String proposition){
        if(validPropositions.contains(proposition)) {
            Set<String> sources = getSourcesByProposition(proposition);
            for (Map.Entry<Pair<String, String>, Double> entry : trustBase.entrySet()) {
                Pair<String, String> key = entry.getKey();
                String sourcetemp = key.getFirst();
                if (sources.contains(sourcetemp)) {
                    double currentTrust = entry.getValue();
                    double newTrust = Math.max(0.0, Math.min(currentTrust + 0.2, 1.0));
                    trustBase.put(key, newTrust);
                }
            }
        }
    }

    private double getAverageTrustForSources(Set<String> sources) {
        return sources.stream()
                .flatMap(source -> trustBase.entrySet().stream()
                        .filter(entry -> entry.getKey().getFirst().equals(source)))
                .mapToDouble(Map.Entry::getValue)
                .average()
                .orElse(0.0);
    }
}



