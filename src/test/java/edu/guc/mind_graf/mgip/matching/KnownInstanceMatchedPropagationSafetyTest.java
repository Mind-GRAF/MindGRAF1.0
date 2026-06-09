package edu.guc.mind_graf.mgip.matching;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnownInstanceMatchedPropagationSafetyTest {

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
    void forwardMatchedReportToOpenNodeStoresKnownInstanceWithoutSupportingPattern() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node x = Network.createVariableNode("X", "individualnode");

        PropositionNode smokeDetectedX = memberClass(x, smokeDetected);
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);
        smokeDetectedBuildingA.setHyp(CONTEXT, BELIEF);

        Substitutions substitutions = new Substitutions();
        substitutions.add(x, buildingA);

        Report matchedReport = matchedReport(
                smokeDetectedX,
                smokeDetectedBuildingA,
                substitutions,
                InferenceType.FORWARD
        );

        invokeProcessSingleReports(smokeDetectedX, matchedReport);

        Collection<KnownInstance> knownInstances =
                smokeDetectedX.getKnownInstances().getPositiveCollectionbyAttribute(BELIEF);

        assertNotNull(
                knownInstances,
                "The open matched node should store a positive known instance."
        );

        KnownInstance storedInstance = knownInstances.iterator().next();

        assertEquals(
                buildingA,
                storedInstance.getSubstitutions().get(x),
                "The known instance should preserve X = BuildingA."
        );

        assertFalse(
                smokeDetectedX.supported(CONTEXT, BELIEF, 0),
                "The open pattern SmokeDetected(X) must not become directly supported."
        );

        assertFalse(
                Scheduler.getForwardAssertedNodes().containsValue(smokeDetectedX),
                "The scheduler must not record the open pattern as a forward asserted node."
        );

        System.out.println(
                "[TEST LOG] open matched known instance stored; open pattern supported="
                        + smokeDetectedX.supported(CONTEXT, BELIEF, 0)
        );
    }

    @Test
    void forwardMatchedReportToClosedNodeAddsSupportAndForwardAssertion() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");
        Node supportingEvidence = Network.createNode("SupportingEvidence", "propositionnode");
        Node x = Network.createVariableNode("X", "individualnode");

        PropositionNode closedReply = memberClass(buildingA, smokeDetected);
        PropositionNode matchedReporter = memberClass(x, smokeDetected);
        PropositionNode supportOrigin = memberClass(buildingA, supportingEvidence);
        supportOrigin.setHyp(CONTEXT, BELIEF);

        Report matchedReport = matchedReport(
                closedReply,
                matchedReporter,
                supportOrigin,
                new Substitutions(),
                InferenceType.FORWARD
        );

        invokeProcessSingleReports(closedReply, matchedReport);

        Map.Entry<Report, PropositionNode> assertion =
                Scheduler.getForwardAssertedNodes().entrySet().iterator().next();

        assertTrue(
                closedReply.supported(CONTEXT, BELIEF, 0),
                "The closed matched node should receive the matched report support."
        );

        assertSame(
                closedReply,
                assertion.getValue(),
                "The scheduler should track the closed matched node as the forward assertion."
        );

        assertEquals(
                ReportType.Matched,
                assertion.getKey().getReportType(),
                "The forward assertion should be carried by a Matched report."
        );

        assertEquals(
                InferenceType.FORWARD,
                assertion.getKey().getInferenceType(),
                "The matched report should preserve forward inference."
        );

        assertSame(
                matchedReporter,
                assertion.getKey().getReporterNode(),
                "The closed matched assertion should keep the matched reporter node."
        );

        System.out.println(
                "[TEST LOG] closed forward matched support recorded; forward assertions="
                        + Scheduler.getForwardAssertedNodes().size()
        );
    }

    @Test
    void backwardMatchedReportToClosedOriginAddsSupportAndBackwardReply() throws Exception {
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node danger = Network.createNode("DangerInBuilding", "propositionnode");
        Node supportingEvidence = Network.createNode("RuleEvidence", "propositionnode");
        Node x = Network.createVariableNode("X", "individualnode");

        PropositionNode closedQuery = memberClass(buildingA, danger);
        PropositionNode matchedReporter = memberClass(x, danger);
        PropositionNode supportOrigin = memberClass(buildingA, supportingEvidence);
        supportOrigin.setHyp(CONTEXT, BELIEF);

        Scheduler.setOriginOfBackInf(closedQuery);

        Report matchedReport = matchedReport(
                closedQuery,
                matchedReporter,
                supportOrigin,
                new Substitutions(),
                InferenceType.BACKWARD
        );

        invokeProcessSingleReports(closedQuery, matchedReport);

        Map.Entry<Report, PropositionNode> reply =
                Scheduler.getBackwardAssertedReplyNodes().entrySet().iterator().next();

        assertTrue(
                closedQuery.supported(CONTEXT, BELIEF, 0),
                "The closed query should receive support from the backward matched answer."
        );

        assertSame(
                closedQuery,
                reply.getValue(),
                "The origin query should be recorded as the backward asserted reply node."
        );

        assertEquals(
                ReportType.Matched,
                reply.getKey().getReportType(),
                "The backward reply should be carried by a Matched report."
        );

        assertEquals(
                InferenceType.BACKWARD,
                reply.getKey().getInferenceType(),
                "The matched report should preserve backward inference."
        );

        assertTrue(
                Scheduler.getHighQueue().isEmpty(),
                "The handled backward matched answer should not be rebroadcast."
        );

        System.out.println(
                "[TEST LOG] closed backward matched answer recorded; backward replies="
                        + Scheduler.getBackwardAssertedReplyNodes().size()
        );
    }

    private PropositionNode memberClass(Node member, Node classNode) throws Exception {
        return (PropositionNode) Network.createNode(
                "propositionnode",
                new DownCableSet(
                        new DownCable(Network.getRelations().get("member"), new NodeSet(member)),
                        new DownCable(Network.getRelations().get("class"), new NodeSet(classNode))
                )
        );
    }

    private Report matchedReport(
            PropositionNode requester,
            PropositionNode reporter,
            PropositionNode supportOrigin,
            Substitutions substitutions,
            InferenceType inferenceType
    ) {
        Report report = new Report(
                substitutions,
                supportOrigin.getSupport(),
                BELIEF,
                true,
                inferenceType,
                requester,
                reporter
        );
        report.setContextName(CONTEXT);
        report.setReportType(ReportType.Matched);
        return report;
    }

    private Report matchedReport(
            PropositionNode requester,
            PropositionNode reporter,
            Substitutions substitutions,
            InferenceType inferenceType
    ) {
        return matchedReport(requester, reporter, reporter, substitutions, inferenceType);
    }

    private void invokeProcessSingleReports(PropositionNode target, Report report) throws Exception {
        Method method = PropositionNode.class.getDeclaredMethod("processSingleReports", Report.class);
        method.setAccessible(true);
        method.invoke(target, report);
    }
}
