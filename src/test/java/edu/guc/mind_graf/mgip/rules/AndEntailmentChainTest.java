package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AndEntailmentChainTest {

    private static final String CONTEXT_NAME = "ChainContext";
    private static final int ATTITUDE_ID = 0;

    @BeforeEach
    void setUp() {
        Scheduler.initiate();

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext(CONTEXT_NAME);
        ContextController.setCurrContext(CONTEXT_NAME);
    }

    @Test
    void chainedRules_AAndBImpliesC_AndCAndDImpliesE() throws NoSuchTypeException {
    

        Node X = Network.createVariableNode("X", "propositionnode");

        DownCable aMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node alpha = Network.createNode("alpha", "propositionnode");
        DownCable aClass = new DownCable(Network.getRelations().get("class"), new NodeSet(alpha));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(aMem, aClass));

        DownCable bMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node beta = Network.createNode("beta", "propositionnode");
        DownCable bClass = new DownCable(Network.getRelations().get("class"), new NodeSet(beta));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(bMem, bClass));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node gamma = Network.createNode("gamma", "propositionnode");
        DownCable cClass = new DownCable(Network.getRelations().get("class"), new NodeSet(gamma));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(cMem, cClass));

        DownCable dMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node delta = Network.createNode("delta", "propositionnode");
        DownCable dClass = new DownCable(Network.getRelations().get("class"), new NodeSet(delta));
        Node M3 = Network.createNode("propositionnode", new DownCableSet(dMem, dClass));

        DownCable eMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node epsilon = Network.createNode("epsilon", "propositionnode");
        DownCable eClass = new DownCable(Network.getRelations().get("class"), new NodeSet(epsilon));
        Node M4 = Network.createNode("propositionnode", new DownCableSet(eMem, eClass));

        Node rule1 = Network.createNode("andentailment", new DownCableSet(
            new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1)),
            new DownCable(Network.getRelations().get("cq"), new NodeSet(M2))));

        Node rule2 = Network.createNode("andentailment", new DownCableSet(
            new DownCable(Network.getRelations().get("ant"), new NodeSet(M2, M3)),
            new DownCable(Network.getRelations().get("cq"), new NodeSet(M4))));

        ((RuleNode) rule1).applyRuleHandler(createAntRuleReport(rule1, M0));
        ((RuleNode) rule1).applyRuleHandler(createAntRuleReport(rule1, M1));

        assertEquals(1, Scheduler.getHighQueue().size());
        Report cInference = Scheduler.getHighQueue().poll();
        assertEquals(rule1, cInference.getReporterNode());
        assertEquals(M2, cInference.getRequesterNode());

        Report cToRule2 = new Report(
                cInference.getSubstitutions(),
                cInference.getSupport(),
                ATTITUDE_ID,
                true,
                InferenceType.BACKWARD,
                rule2,
                M2);
        cToRule2.setReportType(ChannelType.AntRule);
        cToRule2.setContextName(CONTEXT_NAME);

        ((RuleNode) rule2).applyRuleHandler(cToRule2);
        ((RuleNode) rule2).applyRuleHandler(createAntRuleReport(rule2, M3));

        assertEquals(1, Scheduler.getHighQueue().size());
        Report eInference = Scheduler.getHighQueue().poll();
        assertEquals(rule2, eInference.getReporterNode());
        assertEquals(M4, eInference.getRequesterNode());
    }

    private static Report createAntRuleReport(Node requesterRule, Node reporterAntecedent) {
        Report report = new Report(
                new Substitutions(),
                new Support(-1),
                ATTITUDE_ID,
                true,
                InferenceType.BACKWARD,
                requesterRule,
                reporterAntecedent);
        report.setReportType(ChannelType.AntRule);
        report.setContextName(CONTEXT_NAME);
        return report;
    }
}
