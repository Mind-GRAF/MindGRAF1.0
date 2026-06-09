package edu.guc.mind_graf.mgip.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.requests.AntecedentToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.MatchChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.requests.RuleToConsequentChannel;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;

/**
 * Tests Change 11 directly.
 *
 * The goal is to force a request channel with:
 *   filterSubs = {X = nemo}
 *   switchSubs = {}
 *
 * Then the supported node Fish(nemo) processes the request.
 *
 * Before the change:
 *   processSingleRequests() creates reportSubstitutions = {}
 *   so the SEND REPORT log shows reportSubs: {}
 *
 * After the change:
 *   processSingleRequests() uses currentChannel.getFilterSubstitutions()
 *   so the SEND REPORT log shows reportSubs: {X = nemo}
 */
public class SupportedNodeFilterSubstitutionFishTest {

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
    public void supportedNodeReply_shouldUseFilterSubsInCreatedReport()
            throws Exception {

        Node nemo = Network.createNode("nemo", "propositionnode");
        Node fish = Network.createNode("Fish", "propositionnode");
        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode fishX = memberClass(x, fish);

        fishNemo.setHyp(CONTEXT, BELIEF);

        assertTrue(
                fishNemo.supported(CONTEXT, BELIEF, 0),
                "Fish(nemo) must be supported so it immediately replies to the request."
        );

        /*
         * Build filterSubs manually:
         * X = nemo
         */
        Substitutions filterSubs = new Substitutions();
        filterSubs.add((Node) x, (Node) nemo);

        Substitutions switchSubs = new Substitutions();

        /*
         * Create a normal channel manually.
         * requester = Fish(X)
         * reporter/current node that will process request = Fish(nemo)
         *
         * Important:
         * This test is not testing matching.
         * It is directly testing what a supported node puts in the report
         * when the request channel already has filter substitutions.
         */
        Channel channel = new Channel(
                switchSubs,
                filterSubs,
                CONTEXT,
                BELIEF,
                fishX
        );

        Request request = new Request(channel, fishNemo);

        System.out.println("\n====================================================");
        System.out.println("CHANGE 11 DIRECT FILTER TEST");
        System.out.println("Supported node: Fish(nemo)");
        System.out.println("Requester: Fish(X)");
        System.out.println("Forced channel filterSubs: " + filterSubs);
        System.out.println("Forced channel switchSubs: " + switchSubs);
        System.out.println("Expected after fix: SEND REPORT has reportSubs = filterSubs");
        System.out.println("====================================================\n");

        /*
         * processSingleRequests is protected, so call it with reflection.
         */
        Method method = PropositionNode.class.getDeclaredMethod(
                "processSingleRequests",
                Request.class
        );
        method.setAccessible(true);
        method.invoke(fishNemo, request);

        assertFalse(
                Scheduler.getHighQueue().isEmpty(),
                "The supported reply should be enqueued after passing the channel filter."
        );

        assertEquals(
                1,
                Scheduler.getHighQueue().size(),
                "Exactly one supported reply report should be enqueued."
        );

        Report queuedReport = Scheduler.getHighQueue().peek();

        assertEquals(
                filterSubs,
                queuedReport.getSubstitutions(),
                "The queued report should preserve the channel filter substitutions."
        );

        assertTrue(
                queuedReport.getSubstitutions().contains(x),
                "The queued report substitutions should contain X."
        );

        assertEquals(
                nemo,
                queuedReport.getSubstitutions().get(x),
                "The queued report should bind X to nemo."
        );

        assertEquals(
                fishX,
                queuedReport.getRequesterNode(),
                "The report should be addressed to the requester pattern Fish(X)."
        );

        assertEquals(
                fishNemo,
                queuedReport.getReporterNode(),
                "The report should be sent by the supported node Fish(nemo)."
        );

        assertEquals(
                InferenceType.BACKWARD,
                queuedReport.getInferenceType(),
                "A supported reply to a request should be a backward-inference report."
        );

        assertEquals(
                ReportType.WhenRule,
                queuedReport.getReportType(),
                "The queued report should keep the type assigned from the request channel."
        );

        assertEquals(
                CONTEXT,
                queuedReport.getContextName(),
                "The queued report should keep the request channel context."
        );

        assertEquals(
                BELIEF,
                queuedReport.getAttitude(),
                "The queued report should keep the request channel attitude."
        );

        assertTrue(
                queuedReport.isSign(),
                "The supported reply should be a positive report."
        );

        var supportByLevel = queuedReport.getSupport().getJustificationSupport();
        assertNotNull(
                supportByLevel.get(Network.currentLevel),
                "The report support should include the current level."
        );
        var supportByAttitude = supportByLevel.get(Network.currentLevel);
        assertNotNull(
                supportByAttitude.get(BELIEF),
                "The report support should include the belief attitude."
        );
        assertTrue(
                supportByAttitude.get(BELIEF).stream()
                        .anyMatch(support -> support.getFirst().containsKey(BELIEF)
                                && support.getFirst().get(BELIEF).getFirst().contains(fishNemo)),
                "The report support should identify Fish(nemo) as the supporting origin."
        );

        System.out.println("\n=== CHECK LOG ABOVE ===");
        System.out.println("Before change: reportSubs: {} and filter test may fail.");
        System.out.println("After change: reportSubs: " + filterSubs);
        System.out.println("=======================\n");
    }

    @Test
    public void supportedNodeReply_shouldPreserveFilterSubsForInferenceChannelTypes()
            throws Exception {

        Node nemo = Network.createNode("nemo", "propositionnode");
        Node fish = Network.createNode("Fish", "propositionnode");
        Node x = Network.createVariableNode("X", "propositionnode");

        PropositionNode fishNemo = memberClass(nemo, fish);
        PropositionNode fishX = memberClass(x, fish);

        fishNemo.setHyp(CONTEXT, BELIEF);

        Substitutions filterSubs = new Substitutions();
        filterSubs.add((Node) x, (Node) nemo);

        Substitutions switchSubs = new Substitutions();

        List<ChannelCase> channelCases = List.of(
                new ChannelCase(
                        new AntecedentToRuleChannel(switchSubs, filterSubs, CONTEXT, BELIEF, fishX),
                        ReportType.AntRule
                ),
                new ChannelCase(
                        new RuleToConsequentChannel(switchSubs, filterSubs, CONTEXT, BELIEF, fishX),
                        ReportType.RuleCons
                ),
                new ChannelCase(
                        new MatchChannel(switchSubs, filterSubs, CONTEXT, BELIEF, 0, fishX, new Support(-2)),
                        ReportType.Matched
                )
        );

        Method method = PropositionNode.class.getDeclaredMethod(
                "processSingleRequests",
                Request.class
        );
        method.setAccessible(true);

        for (ChannelCase channelCase : channelCases) {
            Scheduler.initiate();

            method.invoke(fishNemo, new Request(channelCase.channel(), fishNemo));

            assertEquals(
                    1,
                    Scheduler.getHighQueue().size(),
                    "A supported reply should be enqueued for " + channelCase.expectedReportType() + "."
            );

            Report queuedReport = Scheduler.getHighQueue().peek();

            assertEquals(
                    filterSubs,
                    queuedReport.getSubstitutions(),
                    "The queued " + channelCase.expectedReportType()
                            + " report should preserve the channel filter substitutions."
            );

            assertEquals(
                    channelCase.expectedReportType(),
                    queuedReport.getReportType(),
                    "The supported reply should keep the report type derived from its channel."
            );

            assertEquals(
                    InferenceType.BACKWARD,
                    queuedReport.getInferenceType(),
                    "A supported reply to an inference request should remain a backward report."
            );

            assertEquals(
                    fishNemo,
                    queuedReport.getReporterNode(),
                    "The supported node should remain the report origin."
            );
        }
    }

    private record ChannelCase(Channel channel, ReportType expectedReportType) {
    }

    private PropositionNode memberClass(Node member, Node clazz) throws NoSuchTypeException {
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
}
