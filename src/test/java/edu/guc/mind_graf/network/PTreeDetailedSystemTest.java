package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PTreeDetailedSystemTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void ptree_sameVariableJoin_combinesOnlyMatchingDoctorBindings() throws Exception {
        Node ahmed = Network.createNode("ahmed", "individualnode");
        Node mona = Network.createNode("mona", "individualnode");
        Node d = Network.createVariableNode("D", "individualnode");

        Node doctor = Network.createNode("Doctor", "propositionnode");
        Node certified = Network.createNode("Certified", "propositionnode");
        Node qualifiedDoctor = Network.createNode("QualifiedDoctor", "propositionnode");

        PropositionNode doctorD = memberClass(d, doctor);
        PropositionNode certifiedD = memberClass(d, certified);
        PropositionNode qualifiedDoctorD = memberClass(d, qualifiedDoctor);

        PropositionNode doctorAhmed = memberClass(ahmed, doctor);
        PropositionNode certifiedAhmed = memberClass(ahmed, certified);
        PropositionNode qualifiedDoctorMona = memberClass(mona, qualifiedDoctor);

        PropositionNode doctorMona = memberClass(mona, doctor);

        RuleNode rule = andEntailmentRule(
                new NodeSet(d),
                new NodeSet(doctorD, certifiedD),
                qualifiedDoctorD
        );
        rule.setHyp(CONTEXT, BELIEF);

        doctorAhmed.add(CONTEXT, BELIEF);
        certifiedAhmed.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> ahmedInference =
                findForwardInferenceFrom(rule, "QualifiedDoctor", "ahmed");
        assertRuleConsForwardInference(
                ahmedInference,
                rule,
                "QualifiedDoctor(ahmed) should be inferred when both antecedents bind D to ahmed."
        );
        assertEquals(ahmed, ahmedInference.getKey().getSubstitutions().get(d));

        doctorMona.add(CONTEXT, BELIEF);

        assertFalse(
                qualifiedDoctorMona.supported(CONTEXT, BELIEF, 0),
                "QualifiedDoctor(mona) should not be supported because Certified(mona) was not added."
        );
        assertNull(
                findForwardInferenceFrom(rule, "QualifiedDoctor", "mona"),
                "The P-Tree should not infer QualifiedDoctor(mona) from Doctor(mona) alone."
        );
    }

    @Test
    void ptree_sharedMiddleVariableJoin_combinesOnlySameLocation() throws Exception {
        Node alice = Network.createNode("Alice", "individualnode");
        Node bob = Network.createNode("Bob", "individualnode");
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        Node p = Network.createVariableNode("P", "individualnode");
        Node b = Network.createVariableNode("B", "individualnode");

        Node person = Network.createNode("Person", "propositionnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node personAtRisk = Network.createNode("PersonAtRisk", "propositionnode");

        PropositionNode personP = memberClass(p, person);
        PropositionNode locatedInPB = memberClass(p, b);
        PropositionNode smokeDetectedB = memberClass(b, smokeDetected);
        PropositionNode personAtRiskP = memberClass(p, personAtRisk);

        PropositionNode personAlice = memberClass(alice, person);
        PropositionNode locatedInAliceBuildingA = memberClass(alice, buildingA);
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);

        PropositionNode personBob = memberClass(bob, person);
        PropositionNode locatedInBobBuildingB = memberClass(bob, buildingB);
        PropositionNode personAtRiskBob = memberClass(bob, personAtRisk);

        RuleNode rule = andEntailmentRule(
                new NodeSet(p, b),
                new NodeSet(personP, locatedInPB, smokeDetectedB),
                personAtRiskP
        );
        rule.setHyp(CONTEXT, BELIEF);

        personAlice.add(CONTEXT, BELIEF);
        locatedInAliceBuildingA.add(CONTEXT, BELIEF);
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> aliceInference =
                findForwardInferenceFrom(rule, "PersonAtRisk", "Alice");
        assertRuleConsForwardInference(
                aliceInference,
                rule,
                "PersonAtRisk(Alice) should be inferred because Alice is in the smoky building."
        );
        assertEquals(alice, aliceInference.getKey().getSubstitutions().get(p));
        assertEquals(buildingA, aliceInference.getKey().getSubstitutions().get(b));

        personBob.add(CONTEXT, BELIEF);
        locatedInBobBuildingB.add(CONTEXT, BELIEF);

        assertFalse(
                personAtRiskBob.supported(CONTEXT, BELIEF, 0),
                "PersonAtRisk(Bob) should not be supported because SmokeDetected(BuildingB) is missing."
        );
        assertNull(
                findForwardInferenceFrom(rule, "PersonAtRisk", "Bob"),
                "The P-Tree should not reuse SmokeDetected(BuildingA) for Bob in BuildingB."
        );
    }

    @Test
    void ptree_multiAntecedentJoin_combinesPersonLocationSmokeAndWorker() throws Exception {
        Node alice = Network.createNode("Alice", "individualnode");
        Node omar = Network.createNode("Omar", "individualnode");
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        Node p = Network.createVariableNode("P", "individualnode");
        Node b = Network.createVariableNode("B", "individualnode");

        Node person = Network.createNode("Person", "propositionnode");
        Node emergencyWorker = Network.createNode("EmergencyWorker", "propositionnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node evacuationNeeded = Network.createNode("EvacuationNeeded", "propositionnode");

        PropositionNode personP = memberClass(p, person);
        PropositionNode emergencyWorkerP = memberClass(p, emergencyWorker);
        PropositionNode locatedInPB = memberClass(p, b);
        PropositionNode smokeDetectedB = memberClass(b, smokeDetected);
        PropositionNode evacuationNeededP = memberClass(p, evacuationNeeded);

        PropositionNode personAlice = memberClass(alice, person);
        PropositionNode emergencyWorkerAlice = memberClass(alice, emergencyWorker);
        PropositionNode locatedInAliceBuildingA = memberClass(alice, buildingA);
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);

        PropositionNode personOmar = memberClass(omar, person);
        PropositionNode emergencyWorkerOmar = memberClass(omar, emergencyWorker);
        PropositionNode locatedInOmarBuildingB = memberClass(omar, buildingB);
        PropositionNode evacuationNeededOmar = memberClass(omar, evacuationNeeded);

        RuleNode rule = andEntailmentRule(
                new NodeSet(p, b),
                new NodeSet(personP, emergencyWorkerP, locatedInPB, smokeDetectedB),
                evacuationNeededP
        );
        rule.setHyp(CONTEXT, BELIEF);

        personAlice.add(CONTEXT, BELIEF);
        emergencyWorkerAlice.add(CONTEXT, BELIEF);
        locatedInAliceBuildingA.add(CONTEXT, BELIEF);
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> aliceInference =
                findForwardInferenceFromWithSubs(rule, p, alice, b, buildingA);
        assertRuleConsForwardInference(
                aliceInference,
                rule,
                "EvacuationNeeded(Alice) should be inferred when all four antecedents match P=Alice and B=BuildingA."
        );
        assertEquals(alice, aliceInference.getKey().getSubstitutions().get(p));
        assertEquals(buildingA, aliceInference.getKey().getSubstitutions().get(b));

        personOmar.add(CONTEXT, BELIEF);
        emergencyWorkerOmar.add(CONTEXT, BELIEF);
        locatedInOmarBuildingB.add(CONTEXT, BELIEF);

        assertFalse(
                evacuationNeededOmar.supported(CONTEXT, BELIEF, 0),
                "EvacuationNeeded(Omar) should not be supported because SmokeDetected(BuildingB) was not added."
        );
        assertNull(
                findForwardInferenceFrom(rule, "EvacuationNeeded", "Omar"),
                "The P-Tree should not reuse SmokeDetected(BuildingA) for Omar in BuildingB."
        );
    }

    @Test
    void ptree_incompatibleSubstitutionRejection_blocksCrossPersonMerge() throws Exception {
        Node ahmed = Network.createNode("ahmed", "individualnode");
        Node mona = Network.createNode("mona", "individualnode");
        Node case42 = Network.createNode("case42", "individualnode");

        Node d = Network.createVariableNode("D", "individualnode");
        Node c = Network.createVariableNode("C", "individualnode");

        Node cleared = Network.createNode("Cleared", "propositionnode");

        Node handlesCase = Network.createNode("HandlesCase", "propositionnode");

        PropositionNode assignedToDC = memberClass(d, c);
        PropositionNode clearedD = memberClass(d, cleared);
        PropositionNode handlesD = memberClass(d, handlesCase);

        PropositionNode assignedToAhmedCase = memberClass(ahmed, case42);
        PropositionNode clearedMona = memberClass(mona, cleared);
        PropositionNode handlesAhmedCase = memberClass(ahmed, handlesCase);
        PropositionNode handlesMonaCase = memberClass(mona, handlesCase);

        RuleNode rule = andEntailmentRule(
                new NodeSet(d, c),
                new NodeSet(assignedToDC, clearedD),
                handlesD
        );
        rule.setHyp(CONTEXT, BELIEF);

        assignedToAhmedCase.add(CONTEXT, BELIEF);
        clearedMona.add(CONTEXT, BELIEF);

        assertFalse(
                handlesAhmedCase.supported(CONTEXT, BELIEF, 0),
                "Handles(ahmed,case42) should not be supported because clearance belongs to mona."
        );
        assertFalse(
                handlesMonaCase.supported(CONTEXT, BELIEF, 0),
                "Handles(mona,case42) should not be supported because assignment belongs to ahmed."
        );
        assertNull(
                findForwardInferenceFrom(rule, "HandlesCase", "ahmed"),
                "The P-Tree should not merge D=ahmed with D=mona."
        );
        assertNull(
                findForwardInferenceFrom(rule, "HandlesCase", "mona"),
                "The P-Tree should not produce the opposite invalid merge either."
        );
    }

    @Test
    void ptree_backwardQuery_combinesAntecedentRepliesAndRejectsMissingSharedEvidence() throws Exception {
        Node alice = Network.createNode("Alice", "individualnode");
        Node bob = Network.createNode("Bob", "individualnode");
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        Node p = Network.createVariableNode("P", "individualnode");
        Node b = Network.createVariableNode("B", "individualnode");

        Node person = Network.createNode("Person", "propositionnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node personAtRisk = Network.createNode("PersonAtRisk", "propositionnode");

        PropositionNode personP = memberClass(p, person);
        PropositionNode locatedInPB = memberClass(p, b);
        PropositionNode smokeDetectedB = memberClass(b, smokeDetected);
        PropositionNode personAtRiskP = memberClass(p, personAtRisk);

        PropositionNode personAlice = memberClass(alice, person);
        PropositionNode locatedInAliceBuildingA = memberClass(alice, buildingA);
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        PropositionNode personAtRiskAlice = memberClass(alice, personAtRisk);

        PropositionNode personBob = memberClass(bob, person);
        PropositionNode locatedInBobBuildingB = memberClass(bob, buildingB);
        PropositionNode personAtRiskBob = memberClass(bob, personAtRisk);

        RuleNode rule = andEntailmentRule(
                new NodeSet(p, b),
                new NodeSet(personP, locatedInPB, smokeDetectedB),
                personAtRiskP
        );
        rule.setHyp(CONTEXT, BELIEF);

        personAlice.setHyp(CONTEXT, BELIEF);
        locatedInAliceBuildingA.setHyp(CONTEXT, BELIEF);
        smokeDetectedBuildingA.setHyp(CONTEXT, BELIEF);
        personBob.setHyp(CONTEXT, BELIEF);
        locatedInBobBuildingB.setHyp(CONTEXT, BELIEF);

        personAtRiskP.deduce(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> aliceAnswer =
                findBackwardAnswerFrom(rule, "PersonAtRisk", "Alice");
        assertRuleConsBackwardAnswer(
                aliceAnswer,
                rule,
                "PersonAtRisk(Alice) should be proved backward because all antecedent replies are compatible."
        );
        assertEquals(alice, aliceAnswer.getKey().getSubstitutions().get(p));
        assertEquals(buildingA, aliceAnswer.getKey().getSubstitutions().get(b));

        assertFalse(
                personAtRiskBob.supported(CONTEXT, BELIEF, 0),
                "PersonAtRisk(Bob) should not be supported because SmokeDetected(BuildingB) is missing."
        );
        assertNull(
                findBackwardAnswerFrom(rule, "PersonAtRisk", "Bob"),
                "The backward P-Tree should not reuse SmokeDetected(BuildingA) for Bob in BuildingB."
        );
    }

    private RuleNode andEntailmentRule(NodeSet variables, NodeSet antecedents, PropositionNode consequent)
            throws Exception {
        return (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("forall"), variables),
                        new DownCable(Network.getRelations().get("ant"), antecedents),
                        new DownCable(Network.getRelations().get("cq"), new NodeSet(consequent))
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

    private void assertRuleConsForwardInference(
            Map.Entry<Report, PropositionNode> inference,
            RuleNode reporter,
            String message
    ) {
        assertNotNull(inference, message);
        assertEquals(ReportType.RuleCons, inference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, inference.getKey().getInferenceType());
        assertSame(reporter, inference.getKey().getReporterNode());
        assertTrue(inference.getKey().isSign());
    }

    private void assertRuleConsBackwardAnswer(
            Map.Entry<Report, PropositionNode> answer,
            RuleNode reporter,
            String message
    ) {
        assertNotNull(answer, message);
        assertEquals(ReportType.RuleCons, answer.getKey().getReportType());
        assertEquals(InferenceType.BACKWARD, answer.getKey().getInferenceType());
        assertSame(reporter, answer.getKey().getReporterNode());
        assertTrue(answer.getKey().isSign());
    }

    private Map.Entry<Report, PropositionNode> findForwardInferenceFrom(
            RuleNode reporter,
            String... requiredText
    ) {
        return Scheduler.getForwardAssertedNodes().entrySet().stream()
                .filter(entry -> entry.getKey().getReporterNode() == reporter)
                .filter(entry -> entry.getKey().getReportType() == ReportType.RuleCons)
                .filter(entry -> {
                    String text = String.valueOf(entry.getValue());
                    for (String required : requiredText) {
                        if (!text.contains(required)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    private Map.Entry<Report, PropositionNode> findForwardInferenceFromWithSubs(
            RuleNode reporter,
            Node... variableValuePairs
    ) {
        return Scheduler.getForwardAssertedNodes().entrySet().stream()
                .filter(entry -> entry.getKey().getReporterNode() == reporter)
                .filter(entry -> entry.getKey().getReportType() == ReportType.RuleCons)
                .filter(entry -> {
                    for (int i = 0; i < variableValuePairs.length; i += 2) {
                        Node variable = variableValuePairs[i];
                        Node value = variableValuePairs[i + 1];
                        if (!value.equals(entry.getKey().getSubstitutions().get(variable))) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    private Map.Entry<Report, PropositionNode> findBackwardAnswerFrom(
            RuleNode reporter,
            String... requiredText
    ) {
        return Scheduler.getBackwardAssertedReplyNodes().entrySet().stream()
                .filter(entry -> entry.getKey().getReporterNode() == reporter)
                .filter(entry -> entry.getKey().getReportType() == ReportType.RuleCons)
                .filter(entry -> {
                    String text = String.valueOf(entry.getValue());
                    for (String required : requiredText) {
                        if (!text.contains(required)) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }
}
