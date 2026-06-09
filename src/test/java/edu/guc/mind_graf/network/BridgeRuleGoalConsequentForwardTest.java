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

public class BridgeRuleGoalConsequentForwardTest {

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
    void bridgeRule_canInferGoalFromBeliefAndIntention() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node x = Network.createVariableNode("X", "individualnode");

        Node emergencyDeclared = Network.createNode("EmergencyDeclared", "propositionnode");
        Node startEvacuation = Network.createNode("StartEvacuation", "propositionnode");
        Node keepShelterReady = Network.createNode("KeepShelterReady", "propositionnode");

        PropositionNode emergencyDeclaredX = memberClass(x, emergencyDeclared);
        PropositionNode startEvacuationX = memberClass(x, startEvacuation);
        PropositionNode keepShelterReadyX = memberClass(x, keepShelterReady);

        PropositionNode emergencyDeclaredBuildingA = memberClass(buildingA, emergencyDeclared);
        PropositionNode startEvacuationBuildingA = memberClass(buildingA, startEvacuation);

        RuleNode bridgeRule = (RuleNode) Network.createNode(
                "bridgerule",
                new DownCableSet(
                        new DownCable(getOrCreateRelation("0-ant"), new NodeSet(emergencyDeclaredX)),
                        new DownCable(getOrCreateRelation("2-ant"), new NodeSet(startEvacuationX)),
                        new DownCable(getOrCreateRelation("1-cq"), new NodeSet(keepShelterReadyX))
                )
        );

        bridgeRule.setHyp(CONTEXT, BELIEF);

        emergencyDeclaredBuildingA.add(CONTEXT, BELIEF);

        assertNull(
                findBridgeInference(bridgeRule, "KeepShelterReady", "BuildingA"),
                "The bridge rule should not fire after only the belief antecedent."
        );

        startEvacuationBuildingA.add(CONTEXT, INTENTION);

        Map.Entry<Report, PropositionNode> inference =
                findBridgeInference(bridgeRule, "KeepShelterReady", "BuildingA");

        assertNotNull(
                inference,
                "The bridge rule should infer KeepShelterReady(BuildingA) as a goal."
        );
        assertEquals(ReportType.RuleCons, inference.getKey().getReportType());
        assertEquals(InferenceType.FORWARD, inference.getKey().getInferenceType());
        assertEquals(GOAL, inference.getKey().getAttitude());
        assertEquals(buildingA, inference.getKey().getSubstitutions().get(x));
        assertEquals(bridgeRule, inference.getKey().getReporterNode());

        System.out.println("[BRIDGE GOAL CONSEQUENT] Belief + intention inferred KeepShelterReady(BuildingA) as a goal.");
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

    private Map.Entry<Report, PropositionNode> findBridgeInference(
            RuleNode bridgeRule,
            String... requiredText
    ) {
        return Scheduler.getForwardAssertedNodes().entrySet().stream()
                .filter(entry -> entry.getKey().getReporterNode() == bridgeRule)
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
