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

public class MixedConnectiveBridgeChainSystemTest {

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
    void mixedConnectiveNormalAndBridgeInference_preservesBindingsAndBlocksDistractors() throws Exception {
        Node ahmed = Network.createNode("ahmed", "individualnode");
        Node mona = Network.createNode("mona", "individualnode");
        Node sara = Network.createNode("sara", "individualnode");
        Node omar = Network.createNode("omar", "individualnode");

        Node d = Network.createVariableNode("D", "individualnode");
        Node p = Network.createVariableNode("P", "individualnode");

        Node doctor = Network.createNode("Doctor", "propositionnode");
        Node certified = Network.createNode("Certified", "propositionnode");
        Node qualifiedDoctor = Network.createNode("QualifiedDoctor", "propositionnode");
        Node criticalPatient = Network.createNode("CriticalPatient", "propositionnode");
        Node hasConsent = Network.createNode("HasConsent", "propositionnode");
        Node keepPatientSafe = Network.createNode("KeepPatientSafe", "propositionnode");

        Relation treatmentCandidateRelation = getOrCreateRelation("treatmentCandidate");
        Relation requiresBackupRelation = getOrCreateRelation("requiresBackup");
        Relation treatsRelation = getOrCreateRelation("treats");

        PropositionNode doctorAhmed = memberClass(ahmed, doctor);
        PropositionNode certifiedAhmed = memberClass(ahmed, certified);
        PropositionNode qualifiedDoctorAhmed = memberClass(ahmed, qualifiedDoctor);

        PropositionNode doctorMona = memberClass(mona, doctor);
        PropositionNode qualifiedDoctorMona = memberClass(mona, qualifiedDoctor);

        PropositionNode criticalPatientSara = memberClass(sara, criticalPatient);
        PropositionNode hasConsentSara = memberClass(sara, hasConsent);
        PropositionNode keepPatientSafeSara = memberClass(sara, keepPatientSafe);

        PropositionNode criticalPatientOmar = memberClass(omar, criticalPatient);
        PropositionNode keepPatientSafeOmar = memberClass(omar, keepPatientSafe);

        PropositionNode qualifiedDoctorD = memberClass(d, qualifiedDoctor);
        PropositionNode criticalPatientP = memberClass(p, criticalPatient);
        PropositionNode hasConsentP = memberClass(p, hasConsent);
        PropositionNode keepPatientSafeP = memberClass(p, keepPatientSafe);

        PropositionNode treatmentCandidateDP = binaryProp(treatmentCandidateRelation, d, p);
        PropositionNode treatmentCandidateAhmedSara = binaryProp(treatmentCandidateRelation, ahmed, sara);
        PropositionNode treatmentCandidateMonaSara = binaryProp(treatmentCandidateRelation, mona, sara);

        PropositionNode requiresBackupDP = binaryProp(requiresBackupRelation, d, p);
        PropositionNode requiresBackupAhmedSara = binaryProp(requiresBackupRelation, ahmed, sara);

        PropositionNode treatsDP = binaryProp(treatsRelation, d, p);
        PropositionNode treatsAhmedSara = binaryProp(treatsRelation, ahmed, sara);
        PropositionNode treatsMonaSara = binaryProp(treatsRelation, mona, sara);
        PropositionNode treatsAhmedOmar = binaryProp(treatsRelation, ahmed, omar);

        Node two = Network.createNode("2", "propositionnode");

        RuleNode qualificationThresh = (RuleNode) Network.createNode(
                "thresh",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("thresh"), new NodeSet(two)),
                        new DownCable(Network.getRelations().get("threshmax"), new NodeSet(two)),
                        new DownCable(
                                Network.getRelations().get("arg"),
                                new NodeSet(doctorAhmed, certifiedAhmed, qualifiedDoctorAhmed)
                        )
                )
        );

        RuleNode treatmentRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("forall"),
                                new NodeSet(d, p)
                        ),
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(qualifiedDoctorD, criticalPatientP, hasConsentP)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(treatmentCandidateDP)
                        )
                )
        );

        RuleNode backupAndOr = (RuleNode) Network.createNode(
                "AndOr",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("min"), new NodeSet(Network.createNode("0", "propositionnode"))),
                        new DownCable(Network.getRelations().get("max"), new NodeSet(two)),
                        new DownCable(
                                Network.getRelations().get("forall"),
                                new NodeSet(d, p)
                        ),
                        new DownCable(
                                Network.getRelations().get("arg"),
                                new NodeSet(treatmentCandidateDP, hasConsentP, requiresBackupDP)
                        )
                )
        );

        RuleNode bridgeRule = (RuleNode) Network.createNode(
                "bridgerule",
                new DownCableSet(
                        new DownCable(getOrCreateRelation("0-ant"), new NodeSet(treatmentCandidateDP)),
                        new DownCable(getOrCreateRelation("1-ant"), new NodeSet(keepPatientSafeP)),
                        new DownCable(getOrCreateRelation("2-cq"), new NodeSet(treatsDP))
                )
        );

        qualificationThresh.setHyp(CONTEXT, BELIEF);
        treatmentRule.setHyp(CONTEXT, BELIEF);
        backupAndOr.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, BELIEF);

        assertFalse(qualifiedDoctorAhmed.supported(CONTEXT, BELIEF, 0));
        assertFalse(treatmentCandidateAhmedSara.supported(CONTEXT, BELIEF, 0));
        assertFalse(treatsAhmedSara.supported(CONTEXT, INTENTION, 0));

        doctorAhmed.add(CONTEXT, BELIEF);
        certifiedAhmed.add(CONTEXT, BELIEF);
        Map.Entry<Report, PropositionNode> qualifiedInference =
                findForwardInferenceFrom(qualificationThresh, "QualifiedDoctor", "ahmed");
        assertNotNull(
                qualifiedInference,
                "Thresh should infer QualifiedDoctor(ahmed) after Doctor(ahmed) and Certified(ahmed)."
        );
        assertEquals(ReportType.RuleCons, qualifiedInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, qualifiedInference.getKey().getInferenceType());
        assertSame(qualificationThresh, qualifiedInference.getKey().getReporterNode());

        criticalPatientSara.add(CONTEXT, BELIEF);
        hasConsentSara.add(CONTEXT, BELIEF);
        Map.Entry<Report, PropositionNode> treatmentInference =
                findForwardInferenceFrom(treatmentRule, "treatmentCandidate", "ahmed", "sara");
        assertNotNull(
                treatmentInference,
                "The normal open rule should infer treatmentCandidate(ahmed,sara)."
        );
        assertEquals(ReportType.RuleCons, treatmentInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, treatmentInference.getKey().getInferenceType());
        assertSame(treatmentRule, treatmentInference.getKey().getReporterNode());
        assertEquals(ahmed, treatmentInference.getKey().getSubstitutions().get(d));
        assertEquals(sara, treatmentInference.getKey().getSubstitutions().get(p));

        Map.Entry<Report, PropositionNode> backupInference =
                findForwardInferenceFrom(backupAndOr, "requiresBackup", "ahmed", "sara");
        assertNotNull(
                backupInference,
                "AndOr should infer requiresBackup(ahmed,sara) with sign=false after max=2 is reached."
        );
        assertFalse(
                backupInference.getKey().isSign(),
                "requiresBackup(ahmed,sara) should be inferred negatively by AndOr."
        );
        assertSame(backupAndOr, backupInference.getKey().getReporterNode());

        keepPatientSafeSara.add(CONTEXT, GOAL);
        Map.Entry<Report, PropositionNode> intentionInference =
                findForwardInferenceFrom(bridgeRule, "treats", "ahmed", "sara");
        assertNotNull(
                intentionInference,
                "The bridge rule should infer treats(ahmed,sara) as an intention."
        );
        assertEquals(INTENTION, intentionInference.getKey().getAttitude());
        assertEquals(ReportType.RuleCons, intentionInference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, intentionInference.getKey().getInferenceType());
        assertSame(bridgeRule, intentionInference.getKey().getReporterNode());
        assertEquals(ahmed, intentionInference.getKey().getSubstitutions().get(d));
        assertEquals(sara, intentionInference.getKey().getSubstitutions().get(p));

        doctorMona.add(CONTEXT, BELIEF);
        criticalPatientOmar.add(CONTEXT, BELIEF);
        keepPatientSafeOmar.add(CONTEXT, GOAL);

        assertFalse(
                qualifiedDoctorMona.supported(CONTEXT, BELIEF, 0),
                "Mona should not become a qualified doctor because Certified(mona) was never added."
        );
        assertNull(
                findForwardAssertion("treatmentCandidate", "mona", "sara"),
                "The scheduler should not record treatmentCandidate(mona,sara)."
        );
        assertNull(
                findForwardAssertion("treats", "mona", "sara"),
                "The scheduler should not record treats(mona,sara)."
        );
        assertNull(
                findForwardAssertion("treats", "ahmed", "omar"),
                "The scheduler should not record treats(ahmed,omar) without treatmentCandidate(ahmed,omar)."
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

    private PropositionNode binaryProp(Relation relation, Node arg1, Node arg2) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(relation, new NodeSet(arg1, arg2))
                )
        );
    }

    private Relation getOrCreateRelation(String name) throws Exception {
        Relation relation = Network.getRelations().get(name);
        if (relation == null) {
            relation = Network.createRelation(name, "", Adjustability.EXPAND, 2);
        }
        return relation;
    }

    private Map.Entry<Report, PropositionNode> findForwardInference(String... requiredText) {
        return Scheduler.getForwardAssertedNodes().entrySet().stream()
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

    private Map.Entry<Report, PropositionNode> findForwardAssertion(String... requiredText) {
        return findForwardInference(requiredText);
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
}
