package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TwoVariableJoiningForwardInferenceTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() throws Exception {
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
    void twoVariableJoining_onlyPersonInsideSmokyBuildingIsAtRisk() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Known:
         * Person(Alice)
         * LocatedIn(Alice, BuildingA)
         * SmokeDetected(BuildingA)
         *
         * Person(Bob)
         * LocatedIn(Bob, BuildingB)
         *
         * Missing:
         * SmokeDetected(BuildingB)
         *
         * Open Rule:
         * Person(P) ∧ LocatedIn(P, B) ∧ SmokeDetected(B)
         *      => PersonAtRisk(P)
         *
         * Expected:
         * PersonAtRisk(Alice) is inferred.
         * PersonAtRisk(Bob) is NOT inferred.
         *
         * Purpose:
         * The system must join two variables:
         *
         * P = Alice
         * B = BuildingA
         *
         * It must not combine:
         *
         * P = Bob
         * B = BuildingB
         *
         * with:
         *
         * SmokeDetected(BuildingA)
         * -------------------------------------------------------
         */

        // ── individuals ────────────────────────────────────────
        Node alice = Network.createNode("Alice", "individualnode");
        Node bob = Network.createNode("Bob", "individualnode");

        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node buildingB = Network.createNode("BuildingB", "individualnode");

        // ── classes / predicates ───────────────────────────────
        Node person = Network.createNode("Person", "propositionnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node personAtRisk = Network.createNode("PersonAtRisk", "propositionnode");

        /*
         * Instead of using rel/arg1/arg2, we represent location
         * using member/class:
         *
         * LocatedIn(P, B) is represented as:
         *
         * member -> P
         * class  -> B
         *
         * This allows the rule to join two variables using only the
         * relations that are already available in the current setup.
         */

        // ── variables ──────────────────────────────────────────
        Node p = Network.createVariableNode("P", "individualnode");
        Node b = Network.createVariableNode("B", "individualnode");

        // ── open antecedent pattern: Person(P) ─────────────────
        PropositionNode personP = memberClass(p, person);

        // ── open antecedent pattern: LocatedIn(P, B) ───────────
        PropositionNode locatedInPB = memberClass(p, b);

        // ── open antecedent pattern: SmokeDetected(B) ──────────
        PropositionNode smokeDetectedB = memberClass(b, smokeDetected);

        // ── open consequent pattern: PersonAtRisk(P) ───────────
        PropositionNode personAtRiskP = memberClass(p, personAtRisk);

        // ── closed fact: Person(Alice) ─────────────────────────
        PropositionNode personAlice = memberClass(alice, person);

        // ── closed fact: LocatedIn(Alice, BuildingA) ───────────
        PropositionNode locatedInAliceBuildingA = memberClass(alice, buildingA);

        // ── closed fact: SmokeDetected(BuildingA) ──────────────
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);

        // ── closed fact: Person(Bob) ───────────────────────────
        PropositionNode personBob = memberClass(bob, person);

        // ── closed fact: LocatedIn(Bob, BuildingB) ─────────────
        PropositionNode locatedInBobBuildingB = memberClass(bob, buildingB);

        // ── closed proposition that should NOT be inferred ─────
        PropositionNode personAtRiskBob = memberClass(bob, personAtRisk);

        // ── open AND rule:
        //    Person(P) ∧ LocatedIn(P, B) ∧ SmokeDetected(B)
        //    → PersonAtRisk(P)
        RuleNode riskRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(personP, locatedInPB, smokeDetectedB)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(personAtRiskP)
                        )
                )
        );

        // Support the open rule.
        riskRule.setHyp(CONTEXT, BELIEF);

        // Bob should not be at risk before inference.
        assertFalse(
                personAtRiskBob.supported(CONTEXT, BELIEF, 0),
                "PersonAtRisk(Bob) should not be supported before inference."
        );

        /*
         * -------------------------------------------------------
         * First: add Alice's complete situation.
         * -------------------------------------------------------
         */

        personAlice.add(CONTEXT, BELIEF);
        locatedInAliceBuildingA.add(CONTEXT, BELIEF);
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);

        /*
         * Copy the latest forward nodes immediately.
         * For open rules, the inferred result is produced as a
         * forward known instance, so we check the inferred node text.
         */
        List<Node> forwardNodesAfterAlice =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        boolean inferredRiskForAlice = forwardNodesAfterAlice.stream()
                .anyMatch(node ->
                        node.toString().contains("Alice")
                                && node.toString().contains("PersonAtRisk")
                );

        assertTrue(
                inferredRiskForAlice,
                "A forward inferred node for PersonAtRisk(Alice) should be produced."
        );

        /*
         * -------------------------------------------------------
         * Second: add Bob's incomplete situation.
         *
         * SmokeDetected(BuildingB) is intentionally missing.
         * -------------------------------------------------------
         */

        personBob.add(CONTEXT, BELIEF);
        locatedInBobBuildingB.add(CONTEXT, BELIEF);

        List<Node> forwardNodesAfterBob =
                new ArrayList<>(Scheduler.getForwardAssertedNodes().values());

        // ── debug output ───────────────────────────────────────
        System.out.println("\n=== DEBUG AFTER TWO-VARIABLE JOINING TEST ===");

        System.out.println("Forward nodes after Alice completed the rule: "
                + forwardNodesAfterAlice);

        System.out.println("Forward nodes after Bob incomplete situation: "
                + forwardNodesAfterBob);

        System.out.println("Person(Alice) supported? "
                + personAlice.supported(CONTEXT, BELIEF, 0));

        System.out.println("LocatedIn(Alice, BuildingA) supported? "
                + locatedInAliceBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("SmokeDetected(BuildingA) supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("Person(Bob) supported? "
                + personBob.supported(CONTEXT, BELIEF, 0));

        System.out.println("LocatedIn(Bob, BuildingB) supported? "
                + locatedInBobBuildingB.supported(CONTEXT, BELIEF, 0));

        System.out.println("PersonAtRisk(Bob) supported? "
                + personAtRiskBob.supported(CONTEXT, BELIEF, 0));

        // ── verify ─────────────────────────────────────────────

        assertTrue(
                personAlice.supported(CONTEXT, BELIEF, 0),
                "Person(Alice) should be supported after add()."
        );

        assertTrue(
                locatedInAliceBuildingA.supported(CONTEXT, BELIEF, 0),
                "LocatedIn(Alice, BuildingA) should be supported after add()."
        );

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported after add()."
        );

        assertTrue(
                personBob.supported(CONTEXT, BELIEF, 0),
                "Person(Bob) should be supported after add()."
        );

        assertTrue(
                locatedInBobBuildingB.supported(CONTEXT, BELIEF, 0),
                "LocatedIn(Bob, BuildingB) should be supported after add()."
        );

        assertTrue(
                forwardNodesAfterBob.isEmpty(),
                "No new forward nodes should be inferred for Bob because SmokeDetected(BuildingB) is missing."
        );

        assertFalse(
                personAtRiskBob.supported(CONTEXT, BELIEF, 0),
                "PersonAtRisk(Bob) should NOT be supported because SmokeDetected(BuildingB) was never added."
        );

        System.out.println("✓ Two-variable joining worked: Alice was inferred at risk, Bob was not.");
    }

    /*
     * -------------------------------------------------------
     * Helper:
     * Builds a proposition using member/class.
     *
     * Example:
     * memberClass(Alice, Person)
     * represents Person(Alice).
     *
     * Also used here for:
     * memberClass(Alice, BuildingA)
     * representing LocatedIn(Alice, BuildingA)
     * for this test.
     * -------------------------------------------------------
     */
    private PropositionNode memberClass(Node memberNode, Node classNode) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(memberNode)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(classNode))
                )
        );
    }
}