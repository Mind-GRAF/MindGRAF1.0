package edu.guc.mind_graf.mgip;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Stack;

import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;

public class Scheduler {
    /**
     * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
     * - Added `executionQueue` to separate finalized primitive execution from planning.
     * - Added `planChoicePoints` to record sibling alternatives at choice points
     *   so Marwa-style act decomposition can backtrack without losing alternatives.
     *
     * [2026-05-28] (Author: Hatem Soliman) — Renamed PlanChoicePoint fields:
     *   actStackSize         → actQueueDepthAtSnapshot
     *   executionQueueSize   → executionQueueDepthAtSnapshot
     * These names better describe what they are: snapshots of queue depths at
     * the moment a choice point is created. See PlanChoicePoint Javadoc for
     * a detailed example of how backtracking uses these snapshots.
     *
     * NOTES/TODO:
     * - This is an intentionally lightweight Marwa-side backtracking helper.
     * - Future work: extract choice-point/backtracking into the standalone
     *   HTN package and replace these helpers with calls into `HTNPlanner`.
     */
    private static Queue<Report> highQueue;
    private static Queue<Request> lowQueue;
    private static Stack<ActNode> actQueue;
    private static Stack<ActNode> highActQueue;
    // NEW: final execution stage for primitive acts that already passed planning.
    // TODO(HTN migration): move this boundary into the standalone HTN integration layer
    // once Marwa's work is re-packaged around the dedicated HTN package.
    private static Deque<ActNode> executionQueue;
    // NEW: remember choice points so a failed branch can resume with the next sibling.
    // TODO(HTN migration): replace this with the standalone HTN planner's recursive
    // backtracking once Marwa-side acts are fully migrated.
    private static Stack<PlanChoicePoint> planChoicePoints;
    private static PropositionNode originOfBackInf;
    private static Hashtable<Report, PropositionNode> forwardAssertedNodes;
    private static Hashtable<Report, PropositionNode> backwardAssertedReplyNodes;

    /**
     * PlanChoicePoint — a snapshot of the scheduler state at the moment a
     * DoOneNode chooses one alternative from a set of sibling plans.
     *
     * PURPOSE:
     * When DoOneNode picks one alternative and schedules it, we save a
     * choice point with the remaining siblings. If the chosen branch fails
     * (throws NoPlansExistForTheActException), the Scheduler can backtrack
     * by restoring the queue depths from this snapshot and trying the next
     * sibling.
     *
     * FIELD EXPLANATIONS:
     *
     * - actQueueDepthAtSnapshot (formerly: actStackSize)
     *     The number of ActNodes on the actQueue at the moment this choice
     *     point was created. On backtracking, actQueue is trimmed to this
     *     depth, removing all acts that were added during the failed branch
     *     (sub-decompositions, precondition checks, etc.).
     *
     * - executionQueueDepthAtSnapshot (formerly: executionQueueSize)
     *     The number of ActNodes on the executionQueue at the moment this
     *     choice point was created. On backtracking, executionQueue is
     *     trimmed to this depth, removing any leaf primitives that were
     *     queued for real-world execution during the failed branch. This
     *     prevents executing actions from an abandoned plan.
     *
     * WORKED EXAMPLE:
     *
     *   Suppose we have: actQueue = [A, B], executionQueue = [X].
     *   DoOneNode faces alternatives {Plan1, Plan2, Plan3}.
     *
     *   1. Choice point created:
     *        actQueueDepthAtSnapshot = 2  (A, B are on actQueue)
     *        executionQueueDepthAtSnapshot = 1  (X is on executionQueue)
     *        remainingAlternatives = [Plan2, Plan3]
     *
     *   2. Plan1 is scheduled → actQueue becomes [A, B, Plan1].
     *      Plan1 decomposes → actQueue becomes [A, B, Plan1, Sub1, Sub2].
     *      Sub1 is primitive → executionQueue becomes [X, Sub1].
     *
     *   3. Sub2 fails → NoPlansExistForTheActException thrown.
     *
     *   4. backtrackToNextAlternative():
     *        trimActQueueTo(2)   → removes Sub2, Sub1, Plan1 → actQueue = [A, B]
     *        trimExecutionQueueTo(1) → removes Sub1 → executionQueue = [X]
     *        Schedules Plan2 → actQueue = [A, B, Plan2]
     *
     *   Everything added during the failed Plan1 branch is undone.
     */
    private static final class PlanChoicePoint {
        /** See class Javadoc — snapshot of actQueue.size() at creation. */
        private final int actQueueDepthAtSnapshot;
        /** See class Javadoc — snapshot of executionQueue.size() at creation. */
        private final int executionQueueDepthAtSnapshot;
        private final ArrayList<ActNode> remainingAlternatives;
        private final String sourceActName;
        private int nextAlternativeIndex;

        private PlanChoicePoint(int actQueueDepthAtSnapshot, int executionQueueDepthAtSnapshot,
                ArrayList<ActNode> remainingAlternatives, String sourceActName) {
            this.actQueueDepthAtSnapshot = actQueueDepthAtSnapshot;
            this.executionQueueDepthAtSnapshot = executionQueueDepthAtSnapshot;
            this.remainingAlternatives = remainingAlternatives;
            this.sourceActName = sourceActName;
            this.nextAlternativeIndex = 0;
        }

        private boolean hasNextAlternative() {
            return nextAlternativeIndex < remainingAlternatives.size();
        }

        private ActNode nextAlternative() {
            return remainingAlternatives.get(nextAlternativeIndex++);
        }
    }

    public static void initiate() {
        highQueue = new ArrayDeque<Report>();
        lowQueue = new ArrayDeque<Request>();
        actQueue = new Stack<ActNode>();
        highActQueue = new Stack<ActNode>();
        executionQueue = new ArrayDeque<ActNode>();
        planChoicePoints = new Stack<PlanChoicePoint>();
        
        forwardAssertedNodes = new Hashtable<Report, PropositionNode>();
        backwardAssertedReplyNodes = new Hashtable<Report, PropositionNode>();

    }

    public static void printLowQueue() {
        System.out.println("Low Priority Queue:");
        for (Request request : lowQueue) {
            System.out.println("Request of channel Id " + request.getChannel().getIdCount());
        }
    }

    public static void printHighQueue() {
        System.out.println("High Priority Queue:");
        for (Report report : highQueue) {
            System.out.println("Report of substitutions " + report.stringifyReport());
        }
    }

    // The main scheduling method of dequeuing of the queue which request/report
    // will be processed next
    public static String schedule() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        String sequence = "The sequence of the scheduler is ";
        main: while (!highQueue.isEmpty() || !lowQueue.isEmpty() || !actQueue.isEmpty() || !highActQueue.isEmpty()) {
            while (!highQueue.isEmpty()) {
                System.out.println(
                        "------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println("\n\u2202 Runner: In HighQueue");
                Report toRunNext = highQueue.peek();
                System.out.println("Processing report with " + toRunNext.stringifyReport() + ".");
                if (toRunNext.getRequesterNode() instanceof ActNode) {
                    ((ActNode) toRunNext.getRequesterNode()).addReport(highQueue.poll());
                    System.out.println("Report added successfully to act node"+toRunNext.getRequesterNode().getName()+"'s set of reports");
                } else {
                    Node requesterNode = toRunNext.getRequesterNode();
                    requesterNode.processReports();
                }
                sequence += "H ";
            }
            while (!lowQueue.isEmpty()) {
                System.out.println(
                        "------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println("\n\u2202 Runner: In LowQueue");
                Request toRunNext = lowQueue.peek();
                System.out.println("Processing request with " + toRunNext.stringifyRequest() + ".");
                Node reporterNode = toRunNext.getReporterNode();
                reporterNode.processRequests();
                sequence += "L ";
                if (!highQueue.isEmpty())
                    continue main;
            }
            while (!highActQueue.isEmpty()) {
                System.out.println(
                        "------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println("AT High ACT QUEUE");
                ActNode toRunNext = highActQueue.pop();
                // System.out.println(toRunNext + " agenda: " + toRunNext.getAgenda());
                System.out.println("\n\n");
                try {
                    toRunNext.processIntends(true);
                } catch (NoPlansExistForTheActException e) {
                    if (!backtrackToNextAlternative()) {
                        throw e;
                    }
                    sequence += "BT ";
                    continue main;
                }
                sequence += "HA ";
                if (!highQueue.isEmpty() || !lowQueue.isEmpty()) {
                    continue main;
                }
            }
            while (!actQueue.isEmpty()) {
                System.out.println(
                        "------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println("AT ACT QUEUE");
                ActNode toRunNext = actQueue.pop();
                // System.out.println(toRunNext + " agenda: " + toRunNext.getAgenda());
                System.out.println("\n\n");
                try {
                    toRunNext.processIntends(false);
                } catch (NoPlansExistForTheActException e) {
                    if (!backtrackToNextAlternative()) {
                        throw e;
                    }
                    sequence += "BT ";
                    continue main;
                }
                sequence += "A ";
                if (!highQueue.isEmpty() || !lowQueue.isEmpty()|| !highActQueue.isEmpty()) {
                    continue main;
                }
            }
            while (!executionQueue.isEmpty()) {
                System.out.println(
                        "------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println("AT EXECUTION QUEUE");
                ActNode toRunNext = executionQueue.poll();
                System.out.println("\n\n");
                toRunNext.runActuator();
                sequence += "EXE ";
                if (!highQueue.isEmpty() || !lowQueue.isEmpty() || !highActQueue.isEmpty() || !actQueue.isEmpty()) {
                    continue main;
                }
            }
        }
        System.out.println(
                "------------------------------------------------------------------------------------------------------------------------------------");

        return sequence;
    }

    /***
     * Method to add a report to high queue
     * 
     * @param report
     */
    public static void addToHighQueue(Report report) {
        highQueue.add(report);
    }

    /***
     * Method to add a request to low queue
     * 
     * @param newRequest // changed from request cz it was causing error (not sure this is right)
     */
    public static void addToLowQueue(Request newRequest) {
        lowQueue.add(newRequest);
    }

    /***
     * Method to add an act node to high act queue
     * 
     * @param actNode
     */
    public static void addToHighActQueue(ActNode actNode) {
        highActQueue.add(actNode);
    }

    /***
     * Method to add an act node to act queue
     * 
     * @param actNode
     */
    public static void addToActQueue(ActNode actNode) {
        actQueue.add(actNode);
    }

    /**
     * Adds a primitive act that already completed planning to the final execution stage.
     */
    public static void addToExecutionQueue(ActNode actNode) {
        executionQueue.add(actNode);
    }

    /**
     * Saves a choice point for a DoOneNode that is picking one alternative
     * from a set of sibling plans.
     *
     * Records the current depths of actQueue and executionQueue so that
     * backtracking can trim both queues to these depths, undoing everything
     * that was added during the failed branch.
     *
     * @param sourceAct           the DoOneNode creating this choice point
     * @param alternatives        the full set of sibling plans (first is already being tried)
     * @param currentActStackSize the current actQueue.size() (renamed param from
     *                            the call site; stored as actQueueDepthAtSnapshot)
     */
    public static void pushPlanChoicePoint(ActNode sourceAct, edu.guc.mind_graf.set.NodeSet alternatives,
            int currentActStackSize) {
        int currentExecutionQueueDepth = executionQueue.size();
        ArrayList<ActNode> remainingAlternatives = new ArrayList<ActNode>();
        boolean first = true;
        for (edu.guc.mind_graf.nodes.Node node : alternatives) {
            ActNode actNode = (ActNode) node;
            if (first) {
                first = false;
                continue;
            }
            remainingAlternatives.add(actNode);
        }
        if (!remainingAlternatives.isEmpty()) {
            planChoicePoints.push(new PlanChoicePoint(currentActStackSize, currentExecutionQueueDepth,
                    remainingAlternatives, sourceAct.getName()));
            System.out.println("Saved choice point for " + sourceAct.getName() + " with "
                    + remainingAlternatives.size() + " remaining sibling(s)");
        }
    }

    private static void trimActQueueTo(int targetSize) {
        while (actQueue.size() > targetSize) {
            ActNode removed = actQueue.pop();
            System.out.println("Backtracking removes pending act " + removed.getName());
        }
    }

    private static void trimExecutionQueueTo(int targetSize) {
        while (executionQueue.size() > targetSize) {
            ActNode removed = executionQueue.removeLast();
            System.out.println("Backtracking removes pending primitive " + removed.getName());
        }
    }

    private static boolean backtrackToNextAlternative() {
        while (!planChoicePoints.isEmpty()) {
            PlanChoicePoint choicePoint = planChoicePoints.peek();
            if (choicePoint.hasNextAlternative()) {
                trimActQueueTo(choicePoint.actQueueDepthAtSnapshot);
                trimExecutionQueueTo(choicePoint.executionQueueDepthAtSnapshot);
                ActNode nextAlternative = choicePoint.nextAlternative();
                nextAlternative.restartAgenda();
                actQueue.push(nextAlternative);
                System.out.println("Backtracking from " + choicePoint.sourceActName
                        + " to sibling " + nextAlternative.getName());
                return true;
            }
            planChoicePoints.pop();
        }
        return false;
    }

    /***
     * Method used to keep track of asserted nodes with a specific certain report as
     * its hash in the hashtable forwardAssertedNodes instance
     * 
     * @param report
     * @param node
     */
    public static void addNodeAssertionThroughFReport(Report report,
            PropositionNode node) {

        forwardAssertedNodes.put(report, node);
    }

    /***
     * Method used to keep track of asserted nodes with a specific certain report as
     * its hash in the hashtable backwardAssertedReplyNodes instance
     * 
     * @param report
     * @param node
     */
    public static void addNodeAssertionThroughBReport(Report report,
            PropositionNode node) {
        backwardAssertedReplyNodes.put(report, node);
    }

    public static Queue<Request> getLowQueue() {
        return lowQueue;
    }

    public static void setLowQueue(Queue<Request> lowQueue) {
        Scheduler.lowQueue = lowQueue;
    }

    public static Hashtable<Report, PropositionNode> getForwardAssertedNodes() {
        return forwardAssertedNodes;
    }

    public static void setForwardAssertedNodes(Hashtable<Report, PropositionNode> forwardAssertedNodes) {
        Scheduler.forwardAssertedNodes = forwardAssertedNodes;
    }

    public static Queue<Report> getHighQueue() {
        return highQueue;
    }

    public static void setHighQueue(Queue<Report> highQueue) {
        Scheduler.highQueue = highQueue;
    }

    public static Hashtable<Report, PropositionNode> getBackwardAssertedReplyNodes() {
        return backwardAssertedReplyNodes;
    }

    public static void setBackwardAssertedReplyNodes(Hashtable<Report, PropositionNode> backwardAssertedReplyNodes) {
        Scheduler.backwardAssertedReplyNodes = backwardAssertedReplyNodes;
    }

    public static Stack<ActNode> getActQueue() {
        return actQueue;
    }
    public static Stack<ActNode> getHighActQueue() {
        return highActQueue;
    }

    public static Deque<ActNode> getExecutionQueue() {
        return executionQueue;
    }

    public static void setActQueue(Stack<ActNode> actQueue) {
        Scheduler.actQueue = actQueue;
    }

    public static void setExecutionQueue(Deque<ActNode> executionQueue) {
        Scheduler.executionQueue = executionQueue;
    }

    public static PropositionNode getOriginOfBackInf() {
        return originOfBackInf;
    }

    public static void setOriginOfBackInf(PropositionNode originOfBackInf) {
        Scheduler.originOfBackInf = originOfBackInf;
    }

}
