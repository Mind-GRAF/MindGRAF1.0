package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
//import edu.guc.mind_graf.components.Adjustability;
//import edu.guc.mind_graf.components.Relation;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * Tests Change 2:
 *
 * Open rule fallback in RuleNode.processSingleRequests().
 *
 * Rule:
 *   Fish(X) ∧ HasGills(X) -> Aquatic(X)
 *
 * Fact:
 *   Fish(nemo)
 *
 * Missing:
 *   HasGills(nemo)
 *
 * Query:
 *   Aquatic(nemo)?
 *
 * Correct result:
 *   Aquatic(nemo) must NOT be inferred because one antecedent is missing.
 *
 * BEFORE Change 2:
 *   If the open-rule fallback is:
 *
 *      super.processSingleRequests(currentRequest);
 *
 *   then the supported open rule may behave like a normal proposition node.
 *
 * AFTER Change 2:
 *   If the fallback is:
 *
 *      if (this.supported(...)) {
 *          requestAntecedentsNotAlreadyWorkingOn(currentRequest);
 *      } else {
 *          super.processSingleRequests(currentRequest);
 *      }
 *
 *   then the rule asks its antecedents and does not fire unless both are supported.
 */
public class OpenRuleFallbackFishTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(
                attitudeNames,
                consistentAttitudes,
                false,
                false,
                false,
                1
        );

        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);
    }

    @Test
    public void openRuleFallback_shouldAskAntecedentsAndNotInferWhenOneAntecedentIsMissing()
            throws Exception {

        /*
         * Constants and classes.
         */
        Node nemo = Network.createNode("nemo", "individualnode");

        Node Fish = Network.createNode("Fish", "propositionnode");
        Node HasGills = Network.createNode("HasGills", "propositionnode");
        Node Aquatic = Network.createNode("Aquatic", "propositionnode");

        /*
         * Variable X.
         */
        Node X = Network.createNode("X", "propositionnode");

        /*
         * Open antecedents and consequent:
         *
         * Fish(X)
         * HasGills(X)
         * Aquatic(X)
         */
        PropositionNode fishX = memberClass(X, Fish);
        PropositionNode hasGillsX = memberClass(X, HasGills);
        PropositionNode aquaticX = memberClass(X, Aquatic);

        /*
         * Closed facts/query:
         *
         * Fish(nemo)
         * HasGills(nemo)
         * Aquatic(nemo)
         */
        PropositionNode fishNemo = memberClass(nemo, Fish);
        PropositionNode hasGillsNemo = memberClass(nemo, HasGills);
        PropositionNode aquaticNemo = memberClass(nemo, Aquatic);

        /*
         * Assert only Fish(nemo).
         * Do NOT assert HasGills(nemo).
         */
        fishNemo.setHyp(CONTEXT, BELIEF);

        /*
         * Open rule:
         *
         * forall X:
         * Fish(X) ∧ HasGills(X) -> Aquatic(X)
         */
        RuleNode openRule = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX, hasGillsX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        ),
                        new DownCable(
                                Network.getRelations().get("forall"),
                                new NodeSet(X)
                        )
                )
        );

        /*
         * The rule itself is supported.
         * This is important because Change 2 decides:
         *
         * supported open rule -> ask antecedents
         * unsupported open rule -> fallback to PropositionNode behavior
         */
        openRule.setHyp(CONTEXT, BELIEF);

        assertTrue(
                fishNemo.supported(CONTEXT, BELIEF, 0),
                "Fish(nemo) should be supported."
        );

        assertFalse(
                hasGillsNemo.supported(CONTEXT, BELIEF, 0),
                "HasGills(nemo) must be missing for this test."
        );

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "Aquatic(nemo) should not be supported before inference."
        );

        System.out.println("\n====================================================");
        System.out.println("CHANGE 2 TEST: Open rule fallback");
        System.out.println("Query: Aquatic(nemo)?");
        System.out.println("Fact: Fish(nemo)");
        System.out.println("Missing fact: HasGills(nemo)");
        System.out.println("Rule: Fish(X) ∧ HasGills(X) -> Aquatic(X)");
        System.out.println("Expected after fix: Aquatic(nemo) is NOT inferred");
        System.out.println("====================================================\n");

        /*
         * Backward query.
         */
        aquaticNemo.deduce(CONTEXT, BELIEF);

        var answers = Scheduler.getBackwardAssertedReplyNodes();

        System.out.println("\n=== DEBUG AFTER deduce(Aquatic(nemo)) ===");
        System.out.println("Backward answers: " + answers.values());
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("HasGills(nemo) supported? " + hasGillsNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(nemo) supported? " + aquaticNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("=========================================\n");

        /*
         * Correct after Change 2:
         * The rule asks antecedents.
         * HasGills(nemo) is missing.
         * Therefore Aquatic(nemo) should not be inferred.
         */
        assertTrue(
                answers.isEmpty(),
                "Aquatic(nemo) was inferred even though HasGills(nemo) is missing. "
                        + "If you changed the open-rule fallback back to only "
                        + "super.processSingleRequests(currentRequest), this failure shows the bug."
        );

        assertFalse(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "Aquatic(nemo) should remain unsupported because HasGills(nemo) is missing."
        );

        System.out.println("✓ Correct: Open rule did not fire because one antecedent is missing.");
    }

    private PropositionNode memberClass(Node member, Node clazz) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("member"),
                                new NodeSet(member)
                        ),
                        new DownCable(
                                Network.getRelations().get("class"),
                                new NodeSet(clazz)
                        )
                )
        );
    }

//     private Relation getOrCreateRelation(String name) throws Exception {
//         Relation relation = Network.getRelations().get(name);

//         if (relation == null) {
//             relation = Network.createRelation(
//                     name,
//                     "propositionnode",
//                     Adjustability.EXPAND,
//                     2
//             );
//         }

//         return relation;
//     }
}
