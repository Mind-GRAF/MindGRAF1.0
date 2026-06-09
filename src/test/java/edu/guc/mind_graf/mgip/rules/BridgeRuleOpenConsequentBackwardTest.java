package edu.guc.mind_graf.network;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BridgeRuleOpenConsequentBackwardTest {

    private static final String CONTEXT = "EmergencyTest";

    private static final int BELIEF = 0;
    private static final int GOAL = 1;
    private static final int INTENTION = 2;

    @BeforeEach
    void setUp() throws Exception {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);
        attitudeNames.add("goals", GOAL);
        attitudeNames.add("intentions", INTENTION);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));
        consistentAttitudes.add(new ArrayList<>(List.of(GOAL)));
        consistentAttitudes.add(new ArrayList<>(List.of(INTENTION)));
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF, GOAL)));
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF, GOAL, INTENTION)));

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
    void bridgeRule_openConsequent_closedQuery_backwardProvesGroundedIntention() throws Exception {

        /*
         * -------------------------------------------------------
         * Test idea:
         *
         * Bridge rule:
         *
         * Belief:
         * SmokeDetected(X)
         *
         * Goal:
         * KeepPeopleSafe(X)
         *
         * Intention:
         * StartEvacuation(X)
         *
         * Added facts:
         * SmokeDetected(BuildingA)  in beliefs
         * KeepPeopleSafe(BuildingA) in goals
         * 
         * Query:
         * StartEvacuation(BuildingA)? in intentions
         *
         * Expected:
         * The closed query should be proven through the open bridge
         * consequent using the substitution X = BuildingA.
         *
         * Important:
         * This test does NOT add an extra normal cq relation.
         * It uses only the bridge consequent relation 2-cq.
         * -------------------------------------------------------
         */

        // ── relations ──────────────────────────────────────────
        Relation smokeDetected = getOrCreateRelation("SmokeDetected");
        Relation keepPeopleSafe = getOrCreateRelation("KeepPeopleSafe");
        Relation startEvacuation = getOrCreateRelation("StartEvacuation");

        // ── variable and individual ────────────────────────────
        Node x = Network.createVariableNode("X", "propositionnode");
        Node buildingA = Network.createNode("BuildingA", "propositionnode");

        // ── open bridge patterns ───────────────────────────────
        PropositionNode smokeDetectedX =
                unaryProposition(smokeDetected, x);

        PropositionNode keepPeopleSafeX =
                unaryProposition(keepPeopleSafe, x);

        PropositionNode startEvacuationX =
                unaryProposition(startEvacuation, x);

        // ── closed facts and closed query ──────────────────────
        PropositionNode smokeDetectedBuildingA =
                unaryProposition(smokeDetected, buildingA);

        PropositionNode keepPeopleSafeBuildingA =
                unaryProposition(keepPeopleSafe, buildingA);

        PropositionNode startEvacuationBuildingA =
                unaryProposition(startEvacuation, buildingA);

        /*
         * -------------------------------------------------------
         * Bridge rule:
         *
         * 0-ant -> SmokeDetected(X)
         * 1-ant -> KeepPeopleSafe(X)
         * 2-cq  -> StartEvacuation(X)
         *
         * No extra cq is used here.
         * -------------------------------------------------------
         */
        RuleNode bridgeRule = (RuleNode) Network.createNode(
                "bridgerule",
                new DownCableSet(
                        new DownCable(
                                getOrCreateRelation("0-ant"),
                                new NodeSet(smokeDetectedX)
                        ),
                        new DownCable(
                                getOrCreateRelation("1-ant"),
                                new NodeSet(keepPeopleSafeX)
                        ),
                        new DownCable(
                                getOrCreateRelation("2-cq"),
                                new NodeSet(startEvacuationX)
                        )
                )
        );

        // Support the bridge rule in all involved attitudes.
        bridgeRule.setHyp(CONTEXT, BELIEF);
        bridgeRule.setHyp(CONTEXT, GOAL);
        bridgeRule.setHyp(CONTEXT, INTENTION);

        assertFalse(
                startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0),
                "StartEvacuation(BuildingA) should not be supported before inference."
        );

        // Add supporting facts normally.
        smokeDetectedBuildingA.add(CONTEXT, BELIEF);
        keepPeopleSafeBuildingA.add(CONTEXT, GOAL);

        assertTrue(
                smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0),
                "SmokeDetected(BuildingA) should be supported as a belief."
        );

        assertTrue(
                keepPeopleSafeBuildingA.supported(CONTEXT, GOAL, 0),
                "KeepPeopleSafe(BuildingA) should be supported as a goal."
        );

        /*
         * Backward query:
         * Ask for the closed intention.
         */
        startEvacuationBuildingA.deduce(CONTEXT, INTENTION);

        System.out.println("\n=== DEBUG BRIDGE OPEN CONSEQUENT + CLOSED QUERY TEST ===");

        System.out.println("SmokeDetected(BuildingA) as belief supported? "
                + smokeDetectedBuildingA.supported(CONTEXT, BELIEF, 0));

        System.out.println("KeepPeopleSafe(BuildingA) as goal supported? "
                + keepPeopleSafeBuildingA.supported(CONTEXT, GOAL, 0));

        System.out.println("StartEvacuation(X) as intention supported? "
                + startEvacuationX.supported(CONTEXT, INTENTION, 0));

        System.out.println("StartEvacuation(BuildingA) as intention supported? "
                + startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0));

        assertTrue(
                startEvacuationBuildingA.supported(CONTEXT, INTENTION, 0),
                "StartEvacuation(BuildingA) should be supported after backward bridge inference with X = BuildingA."
        );

        System.out.println("✓ Bridge rule worked: open consequent StartEvacuation(X) proved closed query StartEvacuation(BuildingA).");
        assertSupportContainsOrigins(
                startEvacuationBuildingA,
                INTENTION,
                bridgeRule
        );
    }

    private PropositionNode unaryProposition(Relation relation, Node argument) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(
                                relation,
                                new NodeSet(argument)
                        )
                )
        );
    }

    private Relation getOrCreateRelation(String relationName) throws Exception {
        Relation relation = Network.getRelations().get(relationName);

        if (relation == null) {
            relation = Network.createRelation(
                    relationName,
                    "",
                    Adjustability.EXPAND,
                    2
            );
        }

        return relation;
    }

    private void assertSupportContainsOrigins(
            PropositionNode supportedNode,
            int attitude,
            PropositionNode... expectedOrigins
    ) {
        HashSet<Integer> actualOriginIds = new HashSet<>();

        var supportByLevel = supportedNode.getSupport().getJustificationSupport();

        for (var levelEntry : supportByLevel.entrySet()) {
            var supportsForAttitude = levelEntry.getValue().get(attitude);

            if (supportsForAttitude == null) {
                continue;
            }

            for (var supportEntry : supportsForAttitude) {
                for (var attitudeSupport : supportEntry.getFirst().values()) {
                    actualOriginIds.addAll(attitudeSupport.getFirst().getValues());
                }

                actualOriginIds.addAll(supportEntry.getSecond().getValues());
            }
        }

        for (PropositionNode expectedOrigin : expectedOrigins) {
            assertTrue(
                    actualOriginIds.contains(expectedOrigin.getId()),
                    "Expected support for " + supportedNode.getName()
                            + " to include origin node " + expectedOrigin.getName()
                            + " with id " + expectedOrigin.getId()
                            + ", but actual origin ids were " + actualOriginIds
            );
        }
    }
}
