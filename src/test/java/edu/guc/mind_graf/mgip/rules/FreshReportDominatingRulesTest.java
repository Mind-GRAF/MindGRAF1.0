package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.acting.rules.WhenDoNode;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FreshReportDominatingRulesTest {

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
    void normalDominatingRulesShouldReceiveFreshReports() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node danger = Network.createNode("Danger", "propositionnode");
        Node warning = Network.createNode("Warning", "propositionnode");

        PropositionNode source = memberClass(buildingA, smokeDetected);
        PropositionNode dangerA = memberClass(buildingA, danger);
        PropositionNode warningA = memberClass(buildingA, warning);

        RuleNode firstRule = andRule(source, dangerA);
        RuleNode secondRule = andRule(source, warningA);

        source.setHyp(CONTEXT, BELIEF);

        Report baseReport = supportedForwardReport(source, ReportType.AntRule);

        Method method = PropositionNode.class.getDeclaredMethod(
                "sendReportToNodeSet",
                NodeSet.class,
                Report.class
        );
        method.setAccessible(true);

        method.invoke(source, new NodeSet(firstRule, secondRule), baseReport);

        assertEquals(
                2,
                Scheduler.getHighQueue().size(),
                "One report should be queued for each dominating normal rule."
        );

        List<Report> queuedReports = new ArrayList<>(Scheduler.getHighQueue());

        assertNotSame(
                queuedReports.get(0),
                queuedReports.get(1),
                "Each dominating rule should receive a distinct Report object."
        );

        assertQueuedReportsTargetExactly(
                queuedReports,
                source,
                ReportType.AntRule,
                firstRule,
                secondRule
        );
    }

    @Test
    void whenDominatingRulesShouldReceiveFreshReports() throws Exception {
        Node door = Network.createNode("Door", "individualnode");
        Node open = Network.createNode("Open", "propositionnode");
        Node close = Network.createNode("Close", "individualnode");
        Node lock = Network.createNode("Lock", "individualnode");

        PropositionNode source = memberClass(door, open);
        ActNode closeDoor = actionNode(door, close);
        ActNode lockDoor = actionNode(door, lock);

        WhenDoNode firstWhenRule = whenDoRule(source, closeDoor);
        WhenDoNode secondWhenRule = whenDoRule(source, lockDoor);

        source.setHyp(CONTEXT, BELIEF);

        Report baseReport = supportedForwardReport(source, ReportType.WhenRule);

        Method method = PropositionNode.class.getDeclaredMethod(
                "sendReportToWhenNodeSet",
                NodeSet.class,
                Report.class
        );
        method.setAccessible(true);

        method.invoke(source, new NodeSet(firstWhenRule, secondWhenRule), baseReport);

        assertEquals(
                2,
                Scheduler.getHighQueue().size(),
                "One report should be queued for each dominating WhenDo rule."
        );

        List<Report> queuedReports = new ArrayList<>(Scheduler.getHighQueue());

        assertNotSame(
                queuedReports.get(0),
                queuedReports.get(1),
                "Each dominating WhenDo rule should receive a distinct Report object."
        );

        assertQueuedReportsTargetExactly(
                queuedReports,
                source,
                ReportType.WhenRule,
                firstWhenRule,
                secondWhenRule
        );
    }

    private Report supportedForwardReport(PropositionNode source, ReportType reportType) {
        Support reportSupport = new Support(-1);
        reportSupport.addNode(BELIEF, source);

        Report report = new Report(
                new Substitutions(),
                reportSupport,
                BELIEF,
                true,
                InferenceType.FORWARD,
                null,
                source
        );
        report.setContextName(CONTEXT);
        report.setReportType(reportType);
        return report;
    }

    private void assertQueuedReportsTargetExactly(
            List<Report> queuedReports,
            PropositionNode expectedReporter,
            ReportType expectedType,
            PropositionNode... expectedRequesters
    ) {
        HashSet<Integer> actualRequesterIds = new HashSet<>();

        for (Report report : queuedReports) {
            assertEquals(
                    expectedReporter,
                    report.getReporterNode(),
                    "The source proposition should be the reporter for each queued report."
            );

            assertEquals(
                    expectedType,
                    report.getReportType(),
                    "Each queued report should use the outgoing dominating-rule channel type."
            );

            actualRequesterIds.add(report.getRequesterNode().getId());
        }

        for (PropositionNode expectedRequester : expectedRequesters) {
            assertTrue(
                    actualRequesterIds.contains(expectedRequester.getId()),
                    "Expected a queued report for requester " + expectedRequester.getName()
            );
        }
    }

    private RuleNode andRule(PropositionNode antecedent, PropositionNode consequent) throws Exception {
        return (RuleNode) Network.createNode(
                "andentailment",
                new DownCableSet(
                        new DownCable(
                                Network.getRelations().get("ant"),
                                new NodeSet(antecedent)
                        ),
                        new DownCable(
                                Network.getRelations().get("cq"),
                                new NodeSet(consequent)
                        )
                )
        );
    }

    private WhenDoNode whenDoRule(PropositionNode whenNode, ActNode actNode) throws Exception {
        return (WhenDoNode) Network.createNode(
                "whendonode",
                new DownCableSet(
                        new DownCable(
                                getOrCreateRelation("0-when", "propositionnode"),
                                new NodeSet(whenNode)
                        ),
                        new DownCable(
                                getOrCreateRelation("do", "actnode"),
                                new NodeSet(actNode)
                        )
                )
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

    private ActNode actionNode(Node object, Node action) throws Exception {
        return (ActNode) Network.createNode(
                "actnode",
                new DownCableSet(
                        new DownCable(
                                getOrCreateRelation("obj", "individualnode"),
                                new NodeSet(object)
                        ),
                        new DownCable(
                                getOrCreateRelation("action", "individualnode"),
                                new NodeSet(action)
                        )
                )
        );
    }

    private Relation getOrCreateRelation(String name, String type) throws Exception {
        Relation relation = Network.getRelations().get(name);

        if (relation == null) {
            relation = Network.createRelation(
                    name,
                    type,
                    Adjustability.EXPAND,
                    1
            );
        }

        return relation;
    }
}
