package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BridgeRuleConnectiveIntegrationTest {

    private static final String CONTEXT = "test";

    private static final int BELIEF = 0;
    private static final int GOAL = 1;
    private static final int INTENTION = 2;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);
        attitudeNames.add("goals", GOAL);
        attitudeNames.add("intentions", INTENTION);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF, GOAL, INTENTION)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void orEntailmentResult_canFeedBridgeRuleForwardInference() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node x = Network.createVariableNode("X", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node possibleFire = Network.createNode("PossibleFire", "propositionnode");
        Node keepPeopleSafe = Network.createNode("KeepPeopleSafe", "propositionnode");
        Node startEvacuation = Network.createNode("StartEvacuation", "propositionnode");

        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);
        PropositionNode keepPeopleSafeBuildingA = memberClass(buildingA, keepPeopleSafe);

        PropositionNode smokeDetectedX = memberClass(x, smokeDetected);
        PropositionNode heatDetectedX = memberClass(x, heatDetected);
        PropositionNode possibleFireX = memberClass(x, possibleFire);
        PropositionNode keepPeopleSafeX = memberClass(x, keepPeopleSafe);
        PropositionNode startEvacuationX = memberClass(x, startEvacuation);

        RuleNode orRule = (RuleNode) Network.createNode(
                "orentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedX, heatDetectedX)),
                        new DownCable(Network.getRelations().get("cq"),
                                new NodeSet(possibleFireX))
                )
        );

        RuleNode bridgeRule = bridgeRule(possibleFireX, keepPeopleSafeX, startEvacuationX);

        orRule.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, BELIEF);

        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> possibleFireInference =
                findForwardInferenceFrom(orRule, "PossibleFire", "BuildingA");

        assertNotNull(
                possibleFireInference,
                "OR-entailment should infer PossibleFire(BuildingA) from one supported warning."
        );
        assertEquals(ReportType.RuleCons, possibleFireInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, possibleFireInference.getKey().getInferenceType());
        assertEquals(buildingA, possibleFireInference.getKey().getSubstitutions().get(x));

        keepPeopleSafeBuildingA.add(CONTEXT, GOAL);

        Map.Entry<Report, PropositionNode> bridgeInference =
                findForwardInferenceFrom(bridgeRule, "StartEvacuation", "BuildingA");

        assertNotNull(
                bridgeInference,
                "The bridge rule should use the OR result PossibleFire(BuildingA) with the goal antecedent."
        );
        assertEquals(INTENTION, bridgeInference.getKey().getAttitude());
        assertEquals(ReportType.RuleCons, bridgeInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, bridgeInference.getKey().getInferenceType());
        assertEquals(buildingA, bridgeInference.getKey().getSubstitutions().get(x));

        assertTrue(smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));
        assertFalse(heatDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("[BRIDGE CONNECTIVE][OR] OR result fed the bridge rule and inferred StartEvacuation(BuildingA).");
    }

    @Test
    void numericalEntailmentResult_canFeedBridgeRuleForwardInference() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node x = Network.createVariableNode("X", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node fireLikely = Network.createNode("FireLikely", "propositionnode");
        Node keepPeopleSafe = Network.createNode("KeepPeopleSafe", "propositionnode");
        Node startEvacuation = Network.createNode("StartEvacuation", "propositionnode");
        Node two = Network.createNode("2", "individualnode");

        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);
        PropositionNode alarmTriggeredBuildingA = memberClass(buildingA, alarmTriggered);
        PropositionNode keepPeopleSafeBuildingA = memberClass(buildingA, keepPeopleSafe);

        PropositionNode smokeDetectedX = memberClass(x, smokeDetected);
        PropositionNode heatDetectedX = memberClass(x, heatDetected);
        PropositionNode alarmTriggeredX = memberClass(x, alarmTriggered);
        PropositionNode fireLikelyX = memberClass(x, fireLikely);
        PropositionNode keepPeopleSafeX = memberClass(x, keepPeopleSafe);
        PropositionNode startEvacuationX = memberClass(x, startEvacuation);

        RuleNode numericalRule = (RuleNode) Network.createNode(
                "numentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("ant"),
                                new NodeSet(smokeDetectedX, heatDetectedX, alarmTriggeredX)),
                        new DownCable(Network.getRelations().get("i"), new NodeSet(two)),
                        new DownCable(Network.getRelations().get("forall"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(fireLikelyX))
                )
        );

        RuleNode bridgeRule = bridgeRule(fireLikelyX, keepPeopleSafeX, startEvacuationX);

        numericalRule.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, BELIEF);

        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        assertNull(
                findForwardInferenceFrom(numericalRule, "FireLikely", "BuildingA"),
                "One numerical-entailment antecedent should not be enough."
        );

        heatDetectedBuildingA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> fireLikelyInference =
                findForwardInferenceFrom(numericalRule, "FireLikely", "BuildingA");

        assertNotNull(
                fireLikelyInference,
                "Numerical-entailment should infer FireLikely(BuildingA) after two compatible antecedents."
        );
        assertEquals(ReportType.RuleCons, fireLikelyInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, fireLikelyInference.getKey().getInferenceType());
        assertEquals(buildingA, fireLikelyInference.getKey().getSubstitutions().get(x));

        keepPeopleSafeBuildingA.add(CONTEXT, GOAL);

        Map.Entry<Report, PropositionNode> bridgeInference =
                findForwardInferenceFrom(bridgeRule, "StartEvacuation", "BuildingA");

        assertNotNull(
                bridgeInference,
                "The bridge rule should use the numerical-entailment result FireLikely(BuildingA)."
        );
        assertEquals(INTENTION, bridgeInference.getKey().getAttitude());
        assertEquals(buildingA, bridgeInference.getKey().getSubstitutions().get(x));

        assertFalse(alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("[BRIDGE CONNECTIVE][NUMERICAL] Numerical result fed the bridge rule and inferred StartEvacuation(BuildingA).");
    }

    @Test
    void threshResult_canFeedBridgeRuleForwardInference() throws Exception {
        Node ahmed = Network.createNode("ahmed", "individualnode");
        Node d = Network.createVariableNode("D", "individualnode");

        Node doctor = Network.createNode("Doctor", "propositionnode");
        Node certified = Network.createNode("Certified", "propositionnode");
        Node qualifiedDoctor = Network.createNode("QualifiedDoctor", "propositionnode");
        Node keepPatientSafe = Network.createNode("KeepPatientSafe", "propositionnode");
        Node treat = Network.createNode("Treat", "propositionnode");
        Node two = Network.createNode("2", "individualnode");

        PropositionNode doctorAhmed = memberClass(ahmed, doctor);
        PropositionNode certifiedAhmed = memberClass(ahmed, certified);
        PropositionNode qualifiedDoctorAhmed = memberClass(ahmed, qualifiedDoctor);
        PropositionNode keepPatientSafeAhmed = memberClass(ahmed, keepPatientSafe);

        PropositionNode qualifiedDoctorD = memberClass(d, qualifiedDoctor);
        PropositionNode keepPatientSafeD = memberClass(d, keepPatientSafe);
        PropositionNode treatD = memberClass(d, treat);

        RuleNode threshRule = (RuleNode) Network.createNode(
                "thresh",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("thresh"), new NodeSet(two)),
                        new DownCable(Network.getRelations().get("threshmax"), new NodeSet(two)),
                        new DownCable(Network.getRelations().get("arg"),
                                new NodeSet(doctorAhmed, certifiedAhmed, qualifiedDoctorAhmed))
                )
        );

        RuleNode bridgeRule = bridgeRule(qualifiedDoctorD, keepPatientSafeD, treatD);

        threshRule.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, BELIEF);

        doctorAhmed.add(CONTEXT, BELIEF);
        certifiedAhmed.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> qualifiedInference =
                findForwardInferenceFrom(threshRule, "QualifiedDoctor", "ahmed");

        assertNotNull(
                qualifiedInference,
                "Thresh should infer QualifiedDoctor(ahmed) after its threshold is reached."
        );
        assertEquals(ReportType.RuleCons, qualifiedInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, qualifiedInference.getKey().getInferenceType());

        keepPatientSafeAhmed.add(CONTEXT, GOAL);

        Map.Entry<Report, PropositionNode> bridgeInference =
                findForwardInferenceFrom(bridgeRule, "Treat", "ahmed");

        assertNotNull(
                bridgeInference,
                "The bridge rule should use the Thresh result QualifiedDoctor(ahmed)."
        );
        assertEquals(INTENTION, bridgeInference.getKey().getAttitude());
        assertEquals(ahmed, bridgeInference.getKey().getSubstitutions().get(d));

        System.out.println("[BRIDGE CONNECTIVE][THRESH] Thresh result fed the bridge rule and inferred Treat(ahmed).");
    }

    @Test
    void andOrNegativeResult_doesNotFeedPositiveBridgeAntecedent() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node x = Network.createVariableNode("X", "individualnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node heatDetected = Network.createNode("HeatDetected", "propositionnode");
        Node alarmTriggered = Network.createNode("AlarmTriggered", "propositionnode");
        Node keepPeopleSafe = Network.createNode("KeepPeopleSafe", "propositionnode");
        Node startEvacuation = Network.createNode("StartEvacuation", "propositionnode");
        Node zero = Network.createNode("0", "individualnode");
        Node two = Network.createNode("2", "individualnode");

        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        PropositionNode heatDetectedBuildingA = memberClass(buildingA, heatDetected);
        PropositionNode alarmTriggeredBuildingA = memberClass(buildingA, alarmTriggered);
        PropositionNode keepPeopleSafeBuildingA = memberClass(buildingA, keepPeopleSafe);

        PropositionNode smokeDetectedX = memberClass(x, smokeDetected);
        PropositionNode heatDetectedX = memberClass(x, heatDetected);
        PropositionNode alarmTriggeredX = memberClass(x, alarmTriggered);
        PropositionNode keepPeopleSafeX = memberClass(x, keepPeopleSafe);
        PropositionNode startEvacuationX = memberClass(x, startEvacuation);

        RuleNode andOrRule = (RuleNode) Network.createNode(
                "andor",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("min"), new NodeSet(zero)),
                        new DownCable(Network.getRelations().get("max"), new NodeSet(two)),
                        new DownCable(Network.getRelations().get("forall"), new NodeSet(x)),
                        new DownCable(Network.getRelations().get("arg"),
                                new NodeSet(smokeDetectedX, heatDetectedX, alarmTriggeredX))
                )
        );

        RuleNode bridgeRule = bridgeRule(alarmTriggeredX, keepPeopleSafeX, startEvacuationX);

        andOrRule.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, BELIEF);

        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        heatDetectedBuildingA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> negativeAlarmInference =
                findForwardInferenceFrom(andOrRule, "AlarmTriggered", "BuildingA");

        assertNotNull(
                negativeAlarmInference,
                "AndOr should infer AlarmTriggered(BuildingA) with sign=false when max=2 is reached."
        );
        assertFalse(
                negativeAlarmInference.getKey().isSign(),
                "The AndOr output in this case is a negative report."
        );

        keepPeopleSafeBuildingA.add(CONTEXT, GOAL);

        assertNull(
                findForwardInferenceFrom(bridgeRule, "StartEvacuation", "BuildingA"),
                "The current bridge rule should not use a negative AndOr output as a positive bridge antecedent."
        );
        assertFalse(
                alarmTriggeredBuildingA.supported(CONTEXT, BELIEF, 0),
                "The closed alarm proposition should not become positively supported from a negative AndOr report."
        );

        System.out.println("[BRIDGE CONNECTIVE][ANDOR] Negative AndOr output was produced, and the positive bridge rule did not fire from it.");
    }

    private RuleNode bridgeRule(
            PropositionNode beliefAntecedent,
            PropositionNode goalAntecedent,
            PropositionNode intentionConsequent
    ) throws Exception {
        return (RuleNode) Network.createNode(
                "bridgerule",
                new DownCableSet(
                        new DownCable(getOrCreateRelation("0-ant"), new NodeSet(beliefAntecedent)),
                        new DownCable(getOrCreateRelation("1-ant"), new NodeSet(goalAntecedent)),
                        new DownCable(getOrCreateRelation("2-cq"), new NodeSet(intentionConsequent))
                )
        );
    }

    private PropositionNode memberClass(Node memberNode, Node classNode) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(memberNode)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(classNode))
                )
        );
    }

    private Relation getOrCreateRelation(String relationName) throws Exception {
        Relation relation = Network.getRelations().get(relationName);
        if (relation == null) {
            relation = Network.createRelation(relationName, "", Adjustability.EXPAND, 1);
        }
        return relation;
    }

    private Map.Entry<Report, PropositionNode> findForwardInferenceFrom(
            RuleNode reporter,
            String... requiredText
    ) {
        return Scheduler.getForwardAssertedNodes().entrySet().stream()
                .filter(entry -> entry.getKey().getReporterNode() == reporter)
                .filter(entry -> entry.getKey().getReportType() == ReportType.RuleCons)
                .filter(entry -> containsAll(entry.getValue(), requiredText))
                .findFirst()
                .orElse(null);
    }

    private boolean containsAll(PropositionNode node, String... requiredText) {
        String text = String.valueOf(node);
        for (String required : requiredText) {
            if (!text.contains(required)) {
                return false;
            }
        }
        return true;
    }
}
