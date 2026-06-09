package edu.guc.mind_graf.mgip.matching;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatchRequestSubstitutionPreservationTest {

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
    void matchedRequestShouldPreserveCallerAndMatchSubstitutions() throws Exception {
        Node person = Network.createVariableNode("P", "individualnode");
        Node building = Network.createVariableNode("B", "individualnode");
        Node alice = Network.createNode("Alice", "individualnode");
        Node buildingA = Network.createNode("BuildingA", "individualnode");
        Node smokeDetected = Network.createNode("SmokeDetected", "propositionnode");

        PropositionNode smokeDetectedB = memberClass(building, smokeDetected);
        PropositionNode smokeDetectedBuildingA = memberClass(buildingA, smokeDetected);

        Substitutions callerFilterSubs = new Substitutions();
        callerFilterSubs.add(person, alice);

        Substitutions callerSwitchSubs = new Substitutions();

        Substitutions matchFilterSubs = new Substitutions();
        matchFilterSubs.add(building, buildingA);

        Match match = new Match(
                matchFilterSubs,
                new Substitutions(),
                smokeDetectedBuildingA,
                1,
                new Support(-1)
        );

        Method method = PropositionNode.class.getDeclaredMethod(
                "sendRequestsToMatches",
                List.class,
                Substitutions.class,
                Substitutions.class,
                String.class,
                int.class,
                ChannelType.class,
                Node.class
        );
        method.setAccessible(true);

        method.invoke(
                smokeDetectedB,
                List.of(match),
                callerFilterSubs,
                callerSwitchSubs,
                CONTEXT,
                BELIEF,
                ChannelType.Matched,
                smokeDetectedB
        );

        assertEquals(
                1,
                Scheduler.getLowQueue().size(),
                "Exactly one matched request should be queued."
        );

        Request queuedRequest = Scheduler.getLowQueue().peek();
        Substitutions queuedFilterSubs = queuedRequest.getChannel().getFilterSubstitutions();

        assertTrue(
                queuedFilterSubs.contains(person),
                "The matched channel should preserve the caller constraint P = Alice."
        );

        assertEquals(
                alice,
                queuedFilterSubs.get(person),
                "The caller constraint should still bind P to Alice."
        );

        assertTrue(
                queuedFilterSubs.contains(building),
                "The matched channel should preserve the match binding B = BuildingA."
        );

        assertEquals(
                buildingA,
                queuedFilterSubs.get(building),
                "The match binding should still bind B to BuildingA."
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
}
