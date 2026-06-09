package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class OpenForwardChainSupportAndLeakageRegressionTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    @Test
    public void openForwardChain_preservesSupportAndBlocksLeakage() throws Exception {
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", BELIEF);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(BELIEF)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext(CONTEXT);
        ContextController.setCurrContext(CONTEXT);

        /*
         * This regression test checks several implementation changes together:
         *
         * 1. Forwarded reports keep the correct open antecedent reporter.
         * 2. Substitutions are preserved through matched reports.
         * 3. AntRule reports are inserted into the rule handler.
         * 4. RuleCons reports stay alive during forward propagation.
         * 5. Support is preserved across a forward rule chain.
         * 6. Open consequents do not leak RuleCons support to incompatible closed nodes.
         * 7. add() supports newly added facts before broadcasting.
         *
         * Rule 1:
         * SmokeDetected(X) AND PeopleInside(X) -> DangerInBuilding(X)
         *
         * Rule 2:
         * DangerInBuilding(X) AND HasExitPlan(X) -> EvacuationReady(X)
         *
         * BuildingA has the complete chain.
         * BuildingB is created to make sure support does not leak.
         */

        // ============================================================
        // Relations
        // ============================================================

        Relation member = Network.getRelations().get("member");
        Relation clazz = Network.getRelations().get("class");
        Relation ant = Network.getRelations().get("ant");
        Relation cq = Network.getRelations().get("cq");
        Relation forall = Network.getRelations().get("forall");

        // ============================================================
        // Base and variable nodes
        // ============================================================

        Node x = Network.createVariableNode("X", "propositionnode");

        Node buildingA = Network.createNode("BuildingA", "propositionnode");
        Node buildingB = Network.createNode("BuildingB", "propositionnode");

        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node peopleInside = Network.createNode("PeopleInside", "propositionnode");
        Node dangerInBuilding = Network.createNode("DangerInBuilding", "propositionnode");
        Node hasExitPlan = Network.createNode("HasExitPlan", "propositionnode");
        Node evacuationReady = Network.createNode("EvacuationReady", "propositionnode");

        // ============================================================
        // Open patterns
        // ============================================================

        PropositionNode smokeX =
                buildMemberClassProposition(member, clazz, x, smokeDetected);

        PropositionNode peopleX =
                buildMemberClassProposition(member, clazz, x, peopleInside);

        PropositionNode dangerX =
                buildMemberClassProposition(member, clazz, x, dangerInBuilding);

        PropositionNode exitPlanX =
                buildMemberClassProposition(member, clazz, x, hasExitPlan);

        PropositionNode evacuationX =
                buildMemberClassProposition(member, clazz, x, evacuationReady);

        // ============================================================
        // Closed BuildingA propositions
        // ============================================================

        PropositionNode smokeA =
                buildMemberClassProposition(member, clazz, buildingA, smokeDetected);

        PropositionNode peopleA =
                buildMemberClassProposition(member, clazz, buildingA, peopleInside);

        PropositionNode dangerA =
                buildMemberClassProposition(member, clazz, buildingA, dangerInBuilding);

        PropositionNode exitPlanA =
                buildMemberClassProposition(member, clazz, buildingA, hasExitPlan);

        PropositionNode evacuationA =
                buildMemberClassProposition(member, clazz, buildingA, evacuationReady);

        // ============================================================
        // Closed BuildingB propositions
        // These are created to catch wrong leakage.
        // ============================================================

        PropositionNode smokeB =
                buildMemberClassProposition(member, clazz, buildingB, smokeDetected);

        PropositionNode dangerB =
                buildMemberClassProposition(member, clazz, buildingB, dangerInBuilding);

        PropositionNode evacuationB =
                buildMemberClassProposition(member, clazz, buildingB, evacuationReady);

        // ============================================================
        // Rules
        // ============================================================

        PropositionNode rule1 = buildAndEntailmentRule(
                forall,
                ant,
                cq,
                x,
                new PropositionNode[] {
                        smokeX,
                        peopleX
                },
                dangerX
        );

        PropositionNode rule2 = buildAndEntailmentRule(
                forall,
                ant,
                cq,
                x,
                new PropositionNode[] {
                        dangerX,
                        exitPlanX
                },
                evacuationX
        );

        // The rules must be supported so they are available during inference.
        rule1.setHyp(CONTEXT, BELIEF);
        rule2.setHyp(CONTEXT, BELIEF);

        // ============================================================
        // Initial checks before inference
        // ============================================================

        assertFalse(
                dangerA.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingA) should not be supported before inference."
        );

        assertFalse(
                evacuationA.supported(CONTEXT, BELIEF, 0),
                "EvacuationReady(BuildingA) should not be supported before inference."
        );

        assertFalse(
                dangerB.supported(CONTEXT, BELIEF, 0),
                "DangerInBuilding(BuildingB) should not be supported before inference."
        );

        assertFalse(
                evacuationB.supported(CONTEXT, BELIEF, 0),
                "EvacuationReady(BuildingB) should not be supported before inference."
        );

        // ============================================================
        // Forward inference for BuildingA
        // ============================================================

        smokeA.add(CONTEXT, BELIEF);

        assertFalse(
                dangerA.supported(CONTEXT, BELIEF, 0),
                "Rule 1 must not fire after only SmokeDetected(BuildingA)."
        );

        peopleA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> dangerEntry =
                findForwardAssertedEntry("BuildingA", "DangerInBuilding");
        PropositionNode inferredDangerA =
                dangerEntry == null ? null : dangerEntry.getValue();

        assertNotNull(
                inferredDangerA,
                "Rule 1 should infer DangerInBuilding(BuildingA)."
        );

        assertFalse(
                forwardAssertedNodeExists("BuildingB", "DangerInBuilding"),
                "DangerInBuilding(BuildingB) must not be supported by the BuildingA inference."
        );

        exitPlanA.add(CONTEXT, BELIEF);

        Map.Entry<Report, PropositionNode> evacuationEntry =
                findForwardAssertedEntry("BuildingA", "EvacuationReady");
        PropositionNode inferredEvacuationA =
                evacuationEntry == null ? null : evacuationEntry.getValue();

        assertNotNull(
                inferredEvacuationA,
                "Rule 2 should infer EvacuationReady(BuildingA)."
        );

        assertFalse(
                forwardAssertedNodeExists("BuildingB", "EvacuationReady"),
                "EvacuationReady(BuildingB) must not be supported by the BuildingA inference."
        );

        // ============================================================
        // Add incomplete BuildingB evidence
        // ============================================================

        smokeB.add(CONTEXT, BELIEF);

        assertFalse(
                forwardAssertedNodeExists("BuildingB", "DangerInBuilding"),
                "BuildingB has only SmokeDetected(BuildingB), so DangerInBuilding(BuildingB) must not be inferred."
        );

        assertFalse(
                forwardAssertedNodeExists("BuildingB", "EvacuationReady"),
                "BuildingB does not have a complete chain, so EvacuationReady(BuildingB) must not be inferred."
        );

        // ============================================================
        // Support checks
        // ============================================================

        Report dangerReport = dangerEntry.getKey();
        Report evacuationReport = evacuationEntry.getKey();

        assertEquals(
                ReportType.RuleCons,
                dangerReport.getReportType(),
                "DangerInBuilding(BuildingA) should be carried by a RuleCons report."
        );

        assertEquals(
                ReportType.RuleCons,
                evacuationReport.getReportType(),
                "EvacuationReady(BuildingA) should be carried by a RuleCons report."
        );

        assertEquals(
                rule1,
                dangerReport.getReporterNode(),
                "DangerInBuilding(BuildingA) should be reported by Rule 1."
        );

        assertEquals(
                rule2,
                evacuationReport.getReporterNode(),
                "EvacuationReady(BuildingA) should be reported by Rule 2."
        );

        assertSupportContainsOrigins(
                inferredDangerA,
                BELIEF,
                rule1
        );

        assertSupportContainsOrigins(
                inferredEvacuationA,
                BELIEF,
                rule2
        );

        System.out.println("\n=== OPEN FORWARD CHAIN SUPPORT AND LEAKAGE TEST PASSED ===");
        System.out.println("DangerInBuilding(BuildingA) supported? "
                + dangerA.supported(CONTEXT, BELIEF, 0));
        System.out.println("EvacuationReady(BuildingA) supported? "
                + evacuationA.supported(CONTEXT, BELIEF, 0));
        System.out.println("DangerInBuilding(BuildingB) supported? "
                + dangerB.supported(CONTEXT, BELIEF, 0));
        System.out.println("EvacuationReady(BuildingB) supported? "
                + evacuationB.supported(CONTEXT, BELIEF, 0));
    }

    private boolean forwardAssertedNodeExists(String memberName, String className) {
        return findForwardAssertedEntry(memberName, className) != null;
    }

    private Map.Entry<Report, PropositionNode> findForwardAssertedEntry(String memberName, String className) {
        for (Map.Entry<Report, PropositionNode> entry : Scheduler.getForwardAssertedNodes().entrySet()) {
            PropositionNode node = entry.getValue();
            String nodeString = String.valueOf(node);
            if (nodeString.contains(memberName) && nodeString.contains(className)) {
                return entry;
            }
        }

        return null;
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

    // ============================================================
    // Helper: member/class proposition
    //
    // Example:
    // member = BuildingA
    // class  = SmokeDetected
    //
    // This represents:
    // SmokeDetected(BuildingA)
    // ============================================================

    private PropositionNode buildMemberClassProposition(
            Relation member,
            Relation clazz,
            Node memberNode,
            Node classNode
    ) throws Exception {

        DownCable memberCable =
                new DownCable(member, new NodeSet(memberNode));

        DownCable classCable =
                new DownCable(clazz, new NodeSet(classNode));

        DownCableSet downCableSet =
                new DownCableSet(memberCable, classCable);

        return (PropositionNode) Network.createNode(
                "propositionnode",
                downCableSet
        );
    }

    // ============================================================
    // Helper: open and-entailment rule
    // ============================================================

    private PropositionNode buildAndEntailmentRule(
            Relation forall,
            Relation ant,
            Relation cq,
            Node variable,
            PropositionNode[] antecedents,
            PropositionNode consequent
    ) throws Exception {

        NodeSet antecedentSet = new NodeSet();

        for (PropositionNode antecedent : antecedents) {
            antecedentSet.add(antecedent);
        }

        DownCable forallCable =
                new DownCable(forall, new NodeSet(variable));

        DownCable antCable =
                new DownCable(ant, antecedentSet);

        DownCable cqCable =
                new DownCable(cq, new NodeSet(consequent));

        DownCableSet downCableSet =
                new DownCableSet(forallCable, antCable, cqCable);

        return (PropositionNode) Network.createNode(
                "andentailment",
                downCableSet
        );
    }
}
