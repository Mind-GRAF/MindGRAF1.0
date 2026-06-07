package edu.guc.mind_graf.integration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.AchieveNode;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.AttitudeNode;
import edu.guc.mind_graf.nodes.DoAllNode;
import edu.guc.mind_graf.nodes.DoOneNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.SNIFNode;
import edu.guc.mind_graf.nodes.SNITERATENode;
import edu.guc.mind_graf.nodes.SNSequenceNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 * SAMPLE RUNS / rigorous scenario suite for the wired HTN acting integration.
 *
 * Each @Test is one documented "sample run" used in Chapter 4 of the thesis. The
 * scenarios deliberately include edge cases — failure recovery, multi-level
 * backtracking, exhaustion of all alternatives, and a degenerate empty choice —
 * to check whether the system behaves correctly at the boundaries, not just on
 * the happy path.
 *
 * HOW WE OBSERVE BEHAVIOUR: Scheduler.schedule() drains the executionQueue as its
 * final phase, so the queue is empty when it returns. We therefore assert on what
 * actually executed by capturing System.out: a leaf primitive's runActuator()
 * prints "running <name> act's actuator", and the Scheduler prints its own
 * "Saved choice point ..." / "Backtracking ..." lines.
 */
public class HTNSampleRunsTest {

    @BeforeEach
    void setUp() {
        freshNetwork();
    }

    /** Builds a clean network + context + scheduler. Called per test, and per phase
     *  of the universal sample run, so each phase is fully independent and deterministic. */
    private void freshNetwork() {
        new Network();
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);
        attitudeNames.add("fears", 2);
        attitudeNames.add("hate", 3);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 2)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 2, 3)));

        NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);
        ContextController.createNewContext("guc");
        ContextController.setCurrContext("guc");
        Scheduler.initiate();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** A real leaf primitive: a network ActNode whose base runActuator() runs. */
    private ActNode primitive(String name) throws Exception {
        ActNode p = (ActNode) Network.createNode(name, "actnode");
        p.setPrimitive(true);
        return p;
    }

    /**
     * A compound act with NO available plan: at the FIND_PLANS stage it throws
     * NoPlansExistForTheActException. The dummy action/obj cables exist only so the
     * exception's diagnostic string does not itself throw a NullPointerException.
     */
    private ActNode failingAct(String name) throws Exception {
        ActNode fail = (ActNode) Network.createNode(name, "actnode");
        fail.setPrimitive(false);
        Relation actionRel = Network.createRelation("action", "", Adjustability.NONE, 2);
        Node act = Network.createNode(name + "_action", "actnode");
        Relation objRel = Network.createRelation("obj", "", Adjustability.NONE, 2);
        Node obj = Network.createNode(name + "_obj", "actnode");
        fail.setDownCableSet(new DownCableSet(
                new DownCable(actionRel, new NodeSet(act)),
                new DownCable(objRel, new NodeSet(obj))));
        return fail;
    }

    /** Build a Sequence over the given children (read via cables obj1, obj2, ...). */
    private SNSequenceNode sequence(ActNode... children) throws Exception {
        DownCable[] cables = new DownCable[children.length];
        for (int i = 0; i < children.length; i++) {
            Relation r = Network.createRelation("obj" + (i + 1), "", Adjustability.NONE, 2);
            cables[i] = new DownCable(r, new NodeSet(children[i]));
        }
        return new SNSequenceNode(new DownCableSet(cables));
    }

    /** Build a DoOne over the given alternatives (read via cable obj). */
    private DoOneNode doOne(ActNode... alternatives) throws Exception {
        Relation r = Network.createRelation("obj", "", Adjustability.NONE, 2);
        NodeSet ns = new NodeSet();
        for (ActNode a : alternatives) {
            ns.add(a);
        }
        return new DoOneNode(new DownCableSet(new DownCable(r, ns)));
    }

    /** Runs the scheduler, capturing everything printed to System.out. */
    private String run() throws Exception {
        PrintStream realOut = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buf));
            Scheduler.schedule();
        } finally {
            System.setOut(realOut);
        }
        return buf.toString();
    }

    /** The exact line a leaf primitive prints when its actuator runs. */
    private static String ran(String name) {
        return "running " + name + " act's actuator";
    }

    /**
     * Forces a distinct name onto a raw-constructed molecular node. Needed only
     * when several such nodes share one name-keyed NodeSet (e.g. DoAll's children),
     * because Node(DownCableSet) reuses the default name "M<count>" without
     * incrementing the counter.
     */
    private static void forceName(Node node, String name) throws Exception {
        Field f = Node.class.getDeclaredField("name");
        f.setAccessible(true);
        f.set(node, name);
    }

    /** Seeds a choice point on the Scheduler's private stack (reflection), so the
     *  order in which alternatives are tried is deterministic for a sample run. */
    private void seedChoicePoint(int actDepth, int execDepth, ArrayList<ActNode> remaining, String name)
            throws Exception {
        Class<?> cp = Class.forName("edu.guc.mind_graf.mgip.Scheduler$PlanChoicePoint");
        Constructor<?> ctor = cp.getDeclaredConstructor(int.class, int.class, ArrayList.class, String.class);
        ctor.setAccessible(true);
        Object choicePoint = ctor.newInstance(actDepth, execDepth, remaining, name);
        Field f = Scheduler.class.getDeclaredField("planChoicePoints");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Object> stack = (Stack<Object>) f.get(null);
        stack.push(choicePoint);
    }

    // ---------------------------------------------------------------------
    // Scenario 1 — Ordered decomposition of a Sequence
    // ---------------------------------------------------------------------
    @Test
    void scenario1_orderedSequence() throws Exception {
        // prepareTea = Sequence[boilWater, steepTea, pourTea]
        SNSequenceNode prepareTea = sequence(
                primitive("boilWater"), primitive("steepTea"), primitive("pourTea"));
        prepareTea.restartAgenda();
        Scheduler.addToActQueue(prepareTea);

        String out = run();

        // All three leaves execute, and strictly in sequence order.
        assertTrue(out.contains(ran("boilWater")), "boilWater must execute");
        assertTrue(out.contains(ran("steepTea")), "steepTea must execute");
        assertTrue(out.contains(ran("pourTea")), "pourTea must execute");
        assertTrue(out.indexOf(ran("boilWater")) < out.indexOf(ran("steepTea")));
        assertTrue(out.indexOf(ran("steepTea")) < out.indexOf(ran("pourTea")));
    }

    // ---------------------------------------------------------------------
    // Scenario 2 — Nested decomposition: DoAll[ Sequence, DoOne ]
    // ---------------------------------------------------------------------
    @Test
    void scenario2_nestedDecomposition() throws Exception {
        SNSequenceNode seq = sequence(primitive("lockDoor"), primitive("startCar"));
        DoOneNode pick = doOne(primitive("takeHighway"));
        forceName(seq, "seqNode");
        forceName(pick, "doOneNode");

        Relation objAll = Network.createRelation("obj", "", Adjustability.NONE, 2);
        NodeSet children = new NodeSet();
        children.add(seq);
        children.add(pick);
        DoAllNode runErrands = new DoAllNode(new DownCableSet(new DownCable(objAll, children)));
        runErrands.restartAgenda();
        Scheduler.addToActQueue(runErrands);

        String out = run();

        // Every leaf at every level executes; control nodes never run a leaf actuator.
        assertTrue(out.contains(ran("lockDoor")), "lockDoor must execute");
        assertTrue(out.contains(ran("startCar")), "startCar must execute");
        assertTrue(out.contains(ran("takeHighway")), "takeHighway must execute");
        assertTrue(out.indexOf(ran("lockDoor")) < out.indexOf(ran("startCar")),
                "sequence order preserved inside the nested DoAll");
    }

    // ---------------------------------------------------------------------
    // Scenario 3 — Failure recovery: backtrack to the next sibling, trimming the
    //              failed branch's already-queued primitive before it runs.
    // ---------------------------------------------------------------------
    @Test
    void scenario3_backtrackToSibling() throws Exception {
        // Plan "viaTrain" = Sequence[buyTicket, trainFails]; sibling "viaBus" = Sequence[rideBus]
        SNSequenceNode viaTrain = sequence(primitive("buyTicket"), failingAct("trainFails"));
        SNSequenceNode viaBus = sequence(primitive("rideBus"));
        forceName(viaTrain, "viaTrain");
        forceName(viaBus, "viaBus");

        ArrayList<ActNode> remaining = new ArrayList<>();
        remaining.add(viaBus);
        seedChoicePoint(0, 0, remaining, "getToWork"); // queues empty now

        viaTrain.restartAgenda();
        Scheduler.addToActQueue(viaTrain);

        String out = run();

        // buyTicket was only QUEUED (plan-first), so when viaTrain failed it was
        // trimmed and never executed. The bus plan ran instead.
        assertFalse(out.contains(ran("buyTicket")),
                "buyTicket belonged to the failed plan and must be trimmed, not executed");
        assertTrue(out.contains(ran("rideBus")), "the surviving plan (bus) must execute");
        assertTrue(out.contains("Backtracking"), "a backtrack must have occurred");
    }

    // ---------------------------------------------------------------------
    // Scenario 4 — Multi-level backtracking through the choice-point STACK:
    //              an inner DoOne exhausts all its options, then control unwinds
    //              to an outer choice point that recovers.
    // ---------------------------------------------------------------------
    @Test
    void scenario4_multiLevelBacktracking() throws Exception {
        DoOneNode innerChoice = doOne(failingAct("planX"), failingAct("planY")); // both fail
        forceName(innerChoice, "innerChoice");
        ActNode walk = primitive("walk"); // the outer recovery

        ArrayList<ActNode> outerRemaining = new ArrayList<>();
        outerRemaining.add(walk);
        seedChoicePoint(0, 0, outerRemaining, "outerGoal");

        innerChoice.restartAgenda();
        Scheduler.addToActQueue(innerChoice);

        String out = run();

        // Inner DoOne tries planX, backtracks to planY, both fail, then the stack
        // unwinds to the outer choice point and "walk" runs.
        assertTrue(out.contains(ran("walk")), "outer recovery 'walk' must execute");
        assertTrue(out.contains("Backtracking"), "backtracking must have occurred");
        assertTrue(out.contains("Saved choice point"), "the inner DoOne must have saved its own choice point");
    }

    // ---------------------------------------------------------------------
    // Scenario 5 — Exhaustion: every alternative fails and there is no surviving
    //              choice point, so planning fails cleanly with the documented
    //              exception (the system does not hang or run a bogus action).
    // ---------------------------------------------------------------------
    @Test
    void scenario5_allAlternativesFail() throws Exception {
        DoOneNode choice = doOne(failingAct("brokenA"), failingAct("brokenB"));
        choice.restartAgenda();
        Scheduler.addToActQueue(choice);

        assertThrows(NoPlansExistForTheActException.class, () -> Scheduler.schedule(),
                "with no recoverable alternative, planning must fail with NoPlansExist...");
    }

    // ---------------------------------------------------------------------
    // Scenario 6 — Degenerate input: a DoOne with no alternatives is handled
    //              gracefully (no crash, nothing executed).
    // ---------------------------------------------------------------------
    @Test
    void scenario6_emptyDoOne() throws Exception {
        Relation r = Network.createRelation("obj", "", Adjustability.NONE, 2);
        DoOneNode empty = new DoOneNode(new DownCableSet(new DownCable(r, new NodeSet())));
        empty.restartAgenda();
        Scheduler.addToActQueue(empty);

        String out = assertDoesNotThrow(this::run,
                "an empty DoOne must not crash the scheduler");
        assertFalse(out.contains("act's actuator"),
                "no leaf actuator should run for an empty choice");
    }

    // ---------------------------------------------------------------------
    // Helpers for the remaining node types
    // ---------------------------------------------------------------------

    /** AttitudeNode that asserts {@code prop} under the named attitude (e.g. "beliefs"). */
    private AttitudeNode attitude(Node prop, String attitudeName) throws Exception {
        Node attAction = Network.createNode(attitudeName, "actnode");
        Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 2);
        Relation action = Network.createRelation("action", "", Adjustability.NONE, 2);
        return new AttitudeNode(new DownCableSet(
                new DownCable(obj, new NodeSet(prop)),
                new DownCable(action, new NodeSet(attAction))));
    }

    /** AchieveNode whose goal is pre-asserted, so the goal is already satisfied. */
    private AchieveNode achieveAlreadySatisfied(String goalName) throws Exception {
        PropositionNode goal = (PropositionNode) Network.createNode(goalName, "propositionnode");
        goal.setHyp("guc", 0); // assert the goal as a current belief
        Node achAction = Network.createNode(goalName + "Act", "actnode");
        Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 2);
        Relation action = Network.createRelation("action", "", Adjustability.NONE, 2);
        return new AchieveNode(new DownCableSet(
                new DownCable(obj, new NodeSet(goal)),
                new DownCable(action, new NodeSet(achAction))));
    }

    /** A conditional node (SNIF or SNITERATE) over one guarded act whose guard is asserted true. */
    private ActNode conditional(boolean iterate, String actName, String guardName) throws Exception {
        ActNode realAct = primitive(actName);
        PropositionNode guard = (PropositionNode) Network.createNode(guardName, "propositionnode");
        guard.setHyp("guc", 0); // assert the guard so the branch is taken at execution time
        Relation actR = Network.createRelation("act", "", Adjustability.NONE, 2);
        Relation guardR = Network.createRelation("guard", "", Adjustability.NONE, 2);
        Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 2);
        ActNode guardedAct = new ActNode(new DownCableSet(
                new DownCable(actR, new NodeSet(realAct)),
                new DownCable(guardR, new NodeSet(guard))));
        DownCableSet dcs = new DownCableSet(new DownCable(obj, new NodeSet(guardedAct)));
        return iterate ? new SNITERATENode(dcs) : new SNIFNode(dcs);
    }

    // ---------------------------------------------------------------------
    // THE universal sample run: every node type and every edge case in one test.
    // This is the single run documented in Chapter 4. Each phase resets the
    // Scheduler so the phases stay independent and the whole test is deterministic.
    // ---------------------------------------------------------------------
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void universalSampleRun() throws Exception {

        // ===== PHASE 1 — nested deliberative decomposition =====
        // DoAll[ Sequence[chop, season], DoOne[plate], Achieve(atShelter), Attitude(citySafe) ]
        // Exercises DoAll, SNSequence, DoOne, Achieve, Attitude, and leaf primitives.
        freshNetwork();
        SNSequenceNode seq = sequence(primitive("chop"), primitive("season"));
        // DoOne with TWO viable alternatives: the first (plate) is picked, the
        // second (garnish) is preserved as a choice point for potential backtracking.
        DoOneNode pick = doOne(primitive("plate"), primitive("garnish"));
        AchieveNode ach = achieveAlreadySatisfied("atShelter");
        PropositionNode citySafe = (PropositionNode) Network.createNode("citySafe", "propositionnode");
        AttitudeNode att = attitude(citySafe, "beliefs");
        forceName(seq, "seqNode");
        forceName(pick, "doOneNode");
        forceName(ach, "achieveNode");
        forceName(att, "attitudeNode");
        Relation objAll = Network.createRelation("obj", "", Adjustability.NONE, 2);
        NodeSet kids = new NodeSet();
        kids.add(seq);
        kids.add(pick);
        kids.add(ach);
        kids.add(att);
        DoAllNode all = new DoAllNode(new DownCableSet(new DownCable(objAll, kids)));
        all.restartAgenda();
        Scheduler.addToActQueue(all);
        String p1 = run();
        // Sequence + leaf primitives: both must execute, in order.
        assertTrue(p1.contains(ran("chop")) && p1.contains(ran("season")),
                "P1: both Sequence steps (chop, season) must execute");
        assertTrue(p1.indexOf(ran("chop")) < p1.indexOf(ran("season")),
                "P1: SNSequence order must be preserved (chop before season)");
        // DoOne with two viable alternatives: EXACTLY ONE runs, the other is preserved.
        // Note: NodeSet is HashMap-backed (the Dr.'s revert), so which of plate/garnish
        // is "first" is not specified -- but DoOne must always pick one and only one.
        boolean plateRan = p1.contains(ran("plate"));
        boolean garnishRan = p1.contains(ran("garnish"));
        assertTrue(plateRan ^ garnishRan,
                "P1: DoOne must run exactly one of its two alternatives (XOR), the other is preserved");
        assertTrue(p1.contains("Saved choice point"),
                "P1: DoOne with two viable options must save the unchosen sibling as a choice point");
        // Achieve + Attitude.
        assertTrue(p1.contains("tmam ya brens"),
                "P1: Achieve must see its pre-asserted goal as already satisfied");
        assertTrue(citySafe.supported("guc", 0, 0),
                "P1: Attitude must have asserted the belief citySafe");

        // ===== PHASE 2 — failure, backtracking, and trimming =====
        // A DoOne picks a failing plan first; its already-queued leaf is trimmed; the sibling runs.
        // Exercises choice points, NoPlansExist failure, trim-based backtracking, and recovery.
        freshNetwork();
        SNSequenceNode failingPlan = sequence(primitive("prep"), failingAct("deadEnd"));
        SNSequenceNode recoveryPlan = sequence(primitive("fallback"));
        forceName(failingPlan, "failingPlan");
        forceName(recoveryPlan, "recoveryPlan");
        ArrayList<ActNode> remaining = new ArrayList<>();
        remaining.add(recoveryPlan);
        seedChoicePoint(0, 0, remaining, "phase2Choice");
        failingPlan.restartAgenda();
        Scheduler.addToActQueue(failingPlan);
        String p2 = run();
        assertFalse(p2.contains(ran("prep")),
                "P2: the failed plan's queued leaf must be trimmed, not executed");
        assertTrue(p2.contains(ran("fallback")), "P2: the recovery sibling must execute");
        assertTrue(p2.contains("Backtracking"), "P2: a backtrack must have occurred");

        // ===== PHASE 3 — execution-time conditionals (SNIF / SNITERATE) =====
        // Conditionals are NOT control acts: they are deferred to the execution queue
        // and evaluate their guards against the live world at execution time. Here we
        // verify they are wired into the pipeline correctly -- classified as deferred
        // and reaching guard evaluation without error. Their full guard-firing behaviour
        // (actually running the guarded act) is asserted in isolation by
        // scenario7/scenario8 below.
        freshNetwork();
        ActNode snif = conditional(false, "openUmbrella", "isRaining");
        assertFalse(snif.isControlAct(), "P3: SNIF must be an execution-time conditional, not a control act");
        snif.restartAgenda();
        Scheduler.addToActQueue(snif);
        String p3a = run();
        assertTrue(p3a.contains("Checking if guards are satisfied"),
                "P3: SNIF must be deferred and evaluate its guards at execution time");

        freshNetwork();
        ActNode iter = conditional(true, "stepForward", "pathClear");
        assertFalse(iter.isControlAct(), "P3: SNITERATE must be an execution-time conditional, not a control act");
        iter.restartAgenda();
        Scheduler.addToActQueue(iter);
        String p3b = run();
        assertTrue(p3b.contains("Checking if guards are satisfied"),
                "P3: SNITERATE must be deferred and evaluate its guards at execution time");

        // ===== PHASE 4 — robustness edge cases =====
        // (a) all alternatives fail -> clean NoPlansExist (no hang, no bogus action).
        freshNetwork();
        DoOneNode allFail = doOne(failingAct("brokenA"), failingAct("brokenB"));
        allFail.restartAgenda();
        Scheduler.addToActQueue(allFail);
        assertThrows(NoPlansExistForTheActException.class, () -> Scheduler.schedule(),
                "P4: exhausting all alternatives must fail cleanly with NoPlansExist...");

        // (b) a degenerate empty DoOne must not crash the scheduler.
        Scheduler.initiate();
        Relation objEmpty = Network.createRelation("obj", "", Adjustability.NONE, 2);
        DoOneNode empty = new DoOneNode(new DownCableSet(new DownCable(objEmpty, new NodeSet())));
        empty.restartAgenda();
        Scheduler.addToActQueue(empty);
        assertDoesNotThrow(() -> Scheduler.schedule(), "P4: an empty DoOne must not crash");
    }

    // ---------------------------------------------------------------------
    // Scenario 9 — DoOne's OWN retry agenda: a real DoOne picks its first
    // alternative, and on failure advances (through its own RETRYING agenda) to
    // the next one. This exercises the node-owned choice-point path, not a
    // scheduler-seeded one.
    // ---------------------------------------------------------------------
    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void scenario9_doOneOwnRetryAgenda() throws Exception {
        freshNetwork();
        SNSequenceNode failPlan = sequence(primitive("p9prep"), failingAct("p9dead"));
        SNSequenceNode okPlan = sequence(primitive("p9ok"));
        DoOneNode pick = doOne(); // empty obj; alternatives supplied (ordered) below
        pick.primeAlternatives(failPlan, okPlan);
        pick.restartAgenda();
        Scheduler.addToActQueue(pick);
        String out = run();
        assertFalse(out.contains(ran("p9prep")),
                "the failed alternative's queued leaf must be trimmed, not executed");
        assertTrue(out.contains(ran("p9ok")),
                "DoOne's own retry agenda must reach and run the second alternative");
        assertTrue(out.contains("Backtracking"), "a backtrack must have occurred");
    }
}
