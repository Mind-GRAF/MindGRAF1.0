package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * Tests Change 9:
 *
 * PropositionNode.processSingleReports() should preserve the original support
 * coming from a RuleCons report.
 *
 * Rule 1:
 *   Fish(X) -> Aquatic(X)
 *
 * Rule 2:
 *   Aquatic(X) -> LivesInWater(X)
 *
 * Fact:
 *   Fish(nemo)
 *
 * Expected after the fix:
 *   Fish(nemo) forward-fires Rule 1.
 *   Aquatic(nemo) is inferred with rule support.
 *   That support is not overwritten.
 *   Aquatic(nemo) can continue forward to Rule 2.
 *   LivesInWater(nemo) is inferred.
 */
public class ForwardRuleSupportPreservationFishTest {

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
    public void forwardRuleConsSupport_shouldNotBeOverwrittenDuringPropagation()
            throws Exception {

        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");
        Node livesInWater = Network.createNode("LivesInWater", "propositionnode");

        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishX = memberClass(x, fish);
        PropositionNode aquaticX = memberClass(x, aquatic);
        PropositionNode livesInWaterX = memberClass(x, livesInWater);

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode aquaticNemo = memberClass(nemo, aquatic);
        PropositionNode livesInWaterNemo = memberClass(nemo, livesInWater);

        /*
         * Rule 1:
         *   Fish(X) -> Aquatic(X)
         */
        RuleNode fishToAquatic = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticX)
                        )
                )
        );

        /*
         * Rule 2:
         *   Aquatic(X) -> LivesInWater(X)
         */
        RuleNode aquaticToLivesInWater = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(aquaticX)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(livesInWaterX)
                        )
                )
        );

        fishToAquatic.setHyp(CONTEXT, BELIEF);
        aquaticToLivesInWater.setHyp(CONTEXT, BELIEF);

        assertFalse(aquaticNemo.supported(CONTEXT, BELIEF, 0));
        assertFalse(livesInWaterNemo.supported(CONTEXT, BELIEF, 0));

        System.out.println("\n====================================================");
        System.out.println("CHANGE 9 TEST: Preserve RuleCons support");
        System.out.println("Fact: Fish(nemo)");
        System.out.println("Rule 1: Fish(X) -> Aquatic(X)");
        System.out.println("Rule 2: Aquatic(X) -> LivesInWater(X)");
        System.out.println("Expected after fix: original rule support is not overwritten");
        System.out.println("====================================================\n");

        /*
         * Start forward inference.
         */
        fishNemo.add(CONTEXT, BELIEF);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        System.out.println("\n=== DEBUG AFTER FORWARD INFERENCE ===");
        System.out.println("Forward asserted/inferred nodes: " + forwardNodes.values());
        System.out.println("Fish(nemo) supported? " + fishNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(nemo) supported? " + aquaticNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("LivesInWater(nemo) supported? " + livesInWaterNemo.supported(CONTEXT, BELIEF, 0));
        System.out.println("Aquatic(nemo) support: " + aquaticNemo.getSupport());
        System.out.println("LivesInWater(nemo) support: " + livesInWaterNemo.getSupport());
        System.out.println("=====================================\n");

        assertEquals(
                2,
                forwardNodes.values().size(),
                "Forward chain should infer exactly two new nodes: Aquatic(nemo) and LivesInWater(nemo)."
        );

        PropositionNode inferredAquatic =
                findForwardNode(forwardNodes.values(), "Aquatic", "nemo");

        PropositionNode inferredLivesInWater =
                findForwardNode(forwardNodes.values(), "LivesInWater", "nemo");

        assertNotNull(
                inferredAquatic,
                "Rule 1 should infer Aquatic(nemo)."
        );

        assertNotNull(
                inferredLivesInWater,
                "Rule 2 should infer LivesInWater(nemo)."
        );

        assertSupportContainsOrigins(
                inferredAquatic,
                BELIEF,
                fishNemo,
                fishToAquatic
        );
        assertSupportDoesNotContainOrigins(
                inferredAquatic,
                BELIEF,
                fishX
        );

        assertSupportContainsOrigins(
                inferredLivesInWater,
                BELIEF,
                fishNemo,
                fishToAquatic,
                aquaticToLivesInWater
        );
        assertSupportDoesNotContainOrigins(
                inferredLivesInWater,
                BELIEF,
                fishX
        );

        System.out.println("✓ Check the support logs above.");
        System.out.println("✓ After the fix, the RuleCons report should keep the original rule support.");
    }

    @Test
    public void closedRuleConsWithoutSubstitutions_shouldSupportCurrentConsequentNode()
            throws Exception {

        Node nemo = Network.createNode("nemo", "propositionnode");

        Node fish = Network.createNode("Fish", "propositionnode");
        Node aquatic = Network.createNode("Aquatic", "propositionnode");

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode aquaticNemo = memberClass(nemo, aquatic);

        RuleNode fishToAquatic = (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(fishNemo)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(aquaticNemo)
                        )
                )
        );

        fishToAquatic.setHyp(CONTEXT, BELIEF);

        assertFalse(aquaticNemo.supported(CONTEXT, BELIEF, 0));

        fishNemo.add(CONTEXT, BELIEF);

        var forwardNodes = Scheduler.getForwardAssertedNodes();

        Report aquaticReport = forwardNodes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(aquaticNemo))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        assertNotNull(
                aquaticReport,
                "The closed consequent Aquatic(nemo) should be the forward asserted support node."
        );
        assertEquals(
                ReportType.RuleCons,
                aquaticReport.getReportType(),
                "The inferred closed consequent should be carried by a RuleCons report."
        );
        assertEquals(
                InferenceType.FORWARD,
                aquaticReport.getInferenceType(),
                "The closed consequent report should remain a forward inference report."
        );
        assertTrue(
                aquaticReport.getSubstitutions() == null || aquaticReport.getSubstitutions().isEmpty(),
                "A closed rule with no variables should not require substitutions."
        );
        assertTrue(
                aquaticNemo.supported(CONTEXT, BELIEF, 0),
                "The current closed consequent node should receive the RuleCons support."
        );
        assertSupportContainsOrigins(
                aquaticNemo,
                BELIEF,
                fishNemo,
                fishToAquatic
        );
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

    private PropositionNode findForwardNode(Collection<? extends Node> nodes, String... requiredText) {
        return nodes.stream()
                .filter(PropositionNode.class::isInstance)
                .map(PropositionNode.class::cast)
                .filter(node -> {
                    String text = node.toString();
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

    private void assertSupportContainsOrigins(
            PropositionNode supportedNode,
            int attitude,
            PropositionNode... expectedOrigins
    ) {
        HashSet<Integer> actualOriginIds = supportOriginIds(supportedNode, attitude);

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

    private void assertSupportDoesNotContainOrigins(
            PropositionNode supportedNode,
            int attitude,
            PropositionNode... unexpectedOrigins
    ) {
        HashSet<Integer> actualOriginIds = supportOriginIds(supportedNode, attitude);

        for (PropositionNode unexpectedOrigin : unexpectedOrigins) {
            assertFalse(
                    actualOriginIds.contains(unexpectedOrigin.getId()),
                    "Expected support for " + supportedNode.getName()
                            + " to use real evidence instead of origin node "
                            + unexpectedOrigin.getName()
                            + " with id " + unexpectedOrigin.getId()
                            + ", but actual origin ids were " + actualOriginIds
            );
        }
    }

    private HashSet<Integer> supportOriginIds(PropositionNode supportedNode, int attitude) {
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

        return actualOriginIds;
    }
}
