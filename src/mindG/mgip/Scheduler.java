package mindG.mgip;

import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Stack;

import mindG.mgip.requests.Channel;
import mindG.mgip.requests.Request;
import mindG.network.Node;
import mindG.network.PropositionNode;

public class Scheduler {
    private static Queue<Report> highQueue;
    private static Queue<Request> lowQueue;
    private static Stack<Node> actQueue;
    private static Hashtable<Report, PropositionNode> forwardAssertedNodes;

    public static void initiate() {
        highQueue = new ArrayDeque<Report>();
        lowQueue = new ArrayDeque<Request>();
        actQueue = new Stack<Node>();
        forwardAssertedNodes = new Hashtable<Report, PropositionNode>();
    }

    /***
     * to Schedule requests before being put on the low priority queue
     * 
     * @param request
     */
    public static void scheduleRequests(Request request) {
        addToLowQueue(request);
    }

    /***
     * to Schedule reports before being put on the high priority queue
     * 
     * @param report
     */
    public static void scheduleReports(Report report) {
        addToHighQueue(report);
    }

    // The main scheduling method of dequeuing of the queue which request/report
    // will be processed next
    public static String schedule() {
        String sequence = "";
        main: while (!highQueue.isEmpty() || !lowQueue.isEmpty() || !actQueue.isEmpty()) {
            while (!highQueue.isEmpty()) {
                System.out.println("\n\u2202 Runner: In HighQueue");
                Report toRunNext = highQueue.peek();
                // System.out.println("Processing " + toRunNext.getIdentifier() + " reports.");
                Node requesterNode = toRunNext.getRequesterNode();
                requesterNode.processReports();
                sequence += 'H';
            }
            while (!lowQueue.isEmpty()) {
                System.out.println("\n\u2202 Runner: In LowQueue");
                Request toRunNext = lowQueue.peek();
                // System.out.println("Processing " + toRunNext.getIdentifier() + " requests.");
                Node reporterNode = toRunNext.getReporterNode();
                reporterNode.processRequests();
                // sequence += 'L';
                if (!highQueue.isEmpty())
                    continue main;
            }
            // while (!actQueue.isEmpty()) {
            // System.out.println("AT ACT QUEUE");
            // ActNode toRunNext = actQueue.removeLast();
            // System.out.println(toRunNext + " agenda: " + toRunNext.getAgenda());
            // System.out.println("\n\n");
            // toRunNext.processIntends();
            // sequence += 'A';
            // if (!highQueue.isEmpty() || !lowQueue.isEmpty()) {
            // continue main;
            // }
            // }
        }
        return sequence;
    }

    // to add the reports on the high queue
    public static void addToHighQueue(Report report) {
        highQueue.add(report);
    }

    // to add the requests on the high queue
    public static void addToLowQueue(Request newRequest) {
        lowQueue.add(newRequest);
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
     * Method checks if the given node was asserted (added in the
     * forwardAssertedNodes instance) with a forward report
     * 
     * @param node
     * @return
     */
    public static boolean isNodeAssertedThroughForwardInf(PropositionNode node) {
        return forwardAssertedNodes.containsValue(node);
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

    public static Request getRequestByChannel(Channel newChannel, Node requesterNode, Node reporterNode) {

        String channelId = newChannel.stringifyChannelID();
        for (Request request : getLowQueue()) {
            if (request.getChannel().stringifyChannelID().equals(channelId)) {
                return request;
            }
        }
        return null;
    }

    public static Queue<Report> getHighQueue() {
        return highQueue;
    }

    public static void setHighQueue(Queue<Report> highQueue) {
        Scheduler.highQueue = highQueue;
    }

    public static Stack<Node> getActQueue() {
        return actQueue;
    }

    public static void setActQueue(Stack<Node> actQueue) {
        Scheduler.actQueue = actQueue;
    }

}
