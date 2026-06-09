package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NumericalAndBackwardMedicalChainTest {

    private static final String CONTEXT = "test";

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    void backwardChain_NumericalEntailment_ThenAndEntailment_DoctorPatientHospital() throws Exception {

        // ── variables ─────────────────────────────────────
        Node X = Network.createVariableNode("X", "propositionnode");
        Node Y = Network.createVariableNode("Y", "propositionnode");
        Node Z = Network.createVariableNode("Z", "propositionnode");

        // ── constants ─────────────────────────────────────
        Node ahmed = Network.createNode("ahmed", "propositionnode");
        Node sara = Network.createNode("sara", "propositionnode");
        Node nileHospital = Network.createNode("nileHospital", "propositionnode");

        // ── class nodes ───────────────────────────────────
        Node Doctor = Network.createNode("Doctor", "propositionnode");
        Node Patient = Network.createNode("Patient", "propositionnode");
        Node Hospital = Network.createNode("Hospital", "propositionnode");
        Node NeedsFollowUp = Network.createNode("NeedsFollowUp", "propositionnode");
        Node FollowUpDoctor = Network.createNode("FollowUpDoctor", "propositionnode");

        // numerical-entailment threshold: 5 antecedents must be satisfied
        Node five = Network.createNode("5", "propositionnode");

        // ── custom binary relations ───────────────────────
        Relation worksAt = getOrCreateRelation("worksAt");
        Relation treatedAt = getOrCreateRelation("treatedAt");
        Relation treats = getOrCreateRelation("treats");
        Relation followUpDoctor = getOrCreateRelation("followUpDoctor");

        // ──────────────────────────────────────────────────
        // Pattern nodes for the numerical-entailment rule
        // ∀x,y,z [
        //   { Doctor(x), Patient(y), Hospital(z), WorksAt(x,z), TreatedAt(y,z) }
        //   &=> { Treats(x,y) }
        // ]
        // ──────────────────────────────────────────────────

        PropositionNode doctorX = memberClass(X, Doctor);
        PropositionNode patientY = memberClass(Y, Patient);
        PropositionNode hospitalZ = memberClass(Z, Hospital);

        PropositionNode worksAtXZ = binaryProp(worksAt, X, Z);
        PropositionNode treatedAtYZ = binaryProp(treatedAt, Y, Z);

        PropositionNode treatsXY = binaryProp(treats, X, Y);

        // ──────────────────────────────────────────────────
        // Extra pattern nodes for the and-entailment chain
        // Treats(x,y) AND NeedsFollowUp(y) => FollowUpDoctor(x,y)
        // ──────────────────────────────────────────────────

        PropositionNode needsFollowUpY = memberClass(Y, NeedsFollowUp);
        PropositionNode followUpDoctorXY = binaryProp(followUpDoctor, X, Y);

        // ──────────────────────────────────────────────────
        // Ground known facts
        // These should satisfy the numerical-entailment rule
        // with substitution:
        //   X = ahmed
        //   Y = sara
        //   Z = nileHospital
        // ──────────────────────────────────────────────────

        PropositionNode doctorAhmed = memberClass(ahmed, Doctor);
        doctorAhmed.setHyp(CONTEXT, 0);

        PropositionNode patientSara = memberClass(sara, Patient);
        patientSara.setHyp(CONTEXT, 0);

        PropositionNode hospitalNile = memberClass(nileHospital, Hospital);
        hospitalNile.setHyp(CONTEXT, 0);

        PropositionNode worksAtAhmedNile = binaryProp(worksAt, ahmed, nileHospital);
        worksAtAhmedNile.setHyp(CONTEXT, 0);

        PropositionNode treatedAtSaraNile = binaryProp(treatedAt, sara, nileHospital);
        treatedAtSaraNile.setHyp(CONTEXT, 0);

        // Extra fact used only by the and-entailment rule
        PropositionNode needsFollowUpSara = memberClass(sara, NeedsFollowUp);
        needsFollowUpSara.setHyp(CONTEXT, 0);

        // ──────────────────────────────────────────────────
        // Rule 1: numerical-entailment
        //
        // At least 5 of the 5 antecedents:
        //   Doctor(X)
        //   Patient(Y)
        //   Hospital(Z)
        //   WorksAt(X,Z)
        //   TreatedAt(Y,Z)
        //
        // entail:
        //   Treats(X,Y)
        // ──────────────────────────────────────────────────

        RuleNode numericalRule = (RuleNode) Network.createNode("numentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"),
                    new NodeSet(doctorX, patientY, hospitalZ, worksAtXZ, treatedAtYZ)),
                new DownCable(Network.getRelations().get("i"),
                    new NodeSet(five)),
                new DownCable(Network.getRelations().get("cq"),
                    new NodeSet(treatsXY))));

        numericalRule.setHyp(CONTEXT, 0);

        // ──────────────────────────────────────────────────
        // Rule 2: and-entailment
        //
        // Treats(X,Y) AND NeedsFollowUp(Y)
        // entail:
        //   FollowUpDoctor(X,Y)
        //
        // This forces the test to reuse the inferred known instance:
        //   Treats(ahmed, sara)
        // ──────────────────────────────────────────────────

        RuleNode andRule = (RuleNode) Network.createNode("andentailment",
            new DownCableSet(
                new DownCable(Network.getRelations().get("ant"),
                    new NodeSet(treatsXY, needsFollowUpY)),
                new DownCable(Network.getRelations().get("cq"),
                    new NodeSet(followUpDoctorXY))));

        andRule.setHyp(CONTEXT, 0);

        // ──────────────────────────────────────────────────
        // Warm-up query:
        // Ask Treats(X,Y) first so Treats(ahmed,sara)
        // becomes an inferred known instance before the final query.
        // ──────────────────────────────────────────────────

        System.out.println("\n=== Warm-up query: who satisfies Treats(X,Y)? ===\n");
        treatsXY.deduce(CONTEXT, 0);

        var intermediateAnswers = Scheduler.getBackwardAssertedReplyNodes();
        System.out.println("\n=== Intermediate answers: " + intermediateAnswers.values() + " ===");

        assertFalse(intermediateAnswers.isEmpty(),
            "Numerical-entailment failed — Treats(ahmed, sara) was not inferred.");

        assertTrue(
            containsAnswer(intermediateAnswers.values(), "treats", "ahmed", "sara"),
            "Numerical-entailment should infer the specific known instance Treats(ahmed, sara)."
        );

        // ──────────────────────────────────────────────────
        // Final backward query:
        // Ask FollowUpDoctor(X,Y).
        //
        // Expected chain:
        //   FollowUpDoctor(X,Y)
        //   ← andRule needs Treats(X,Y) and NeedsFollowUp(Y)
        //   ← Treats(ahmed,sara) already inferred by numerical-entailment
        //   ← NeedsFollowUp(sara) is a known fact
        //   ⇒ FollowUpDoctor(ahmed,sara)
        // ──────────────────────────────────────────────────

        System.out.println("\n=== Final query: who satisfies FollowUpDoctor(X,Y)? ===\n");
        followUpDoctorXY.deduce(CONTEXT, 0);

        var finalAnswers = Scheduler.getBackwardAssertedReplyNodes();
        System.out.println("\n=== Final answers found: " + finalAnswers.values() + " ===");

        assertFalse(finalAnswers.isEmpty(),
            "No final answer found — numerical-entailment + and-entailment backward chain is broken.");

        assertTrue(
            containsAnswer(finalAnswers.values(), "followUpDoctor", "ahmed", "sara"),
            "Final backward chain should infer the specific answer FollowUpDoctor(ahmed, sara)."
        );

        System.out.println("✓ Chain worked: numerical-entailment inferred Treats(ahmed, sara), then and-entailment inferred FollowUpDoctor(ahmed, sara).");
    }

    // ──────────────────────────────────────────────────────
    // Helper: member/class proposition
    // Example:
    //   memberClass(ahmed, Doctor) means Doctor(ahmed)
    // ──────────────────────────────────────────────────────

    private PropositionNode memberClass(Node member, Node clazz) throws Exception {
        return (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(Network.getRelations().get("member"), new NodeSet(member)),
                new DownCable(Network.getRelations().get("class"), new NodeSet(clazz))));
    }

    // ──────────────────────────────────────────────────────
    // Helper: binary proposition
    // Example:
    //   binaryProp(worksAt, ahmed, nileHospital)
    // means:
    //   WorksAt(ahmed, nileHospital)
    // ──────────────────────────────────────────────────────

    private PropositionNode binaryProp(Relation relation, Node arg1, Node arg2) throws Exception {
        return (PropositionNode) Network.createNode("propositionnode",
            new DownCableSet(
                new DownCable(relation, new NodeSet(arg1, arg2))));
    }

    // ──────────────────────────────────────────────────────
    // Helper: create relation only if it does not already exist
    // ──────────────────────────────────────────────────────

    private Relation getOrCreateRelation(String name) throws Exception {
        Relation relation = Network.getRelations().get(name);

        if (relation == null) {
            relation = Network.createRelation(name, "", Adjustability.EXPAND, 2);
        }

        return relation;
    }
    private boolean containsAnswer(Collection<? extends Node> answers, String... requiredText) {
        return answers.stream().anyMatch(answer -> {
            String text = answer.toString();
            for (String required : requiredText) {
                if (!text.contains(required)) {
                    return false;
                }
            }
            return true;
        });
    }
}
