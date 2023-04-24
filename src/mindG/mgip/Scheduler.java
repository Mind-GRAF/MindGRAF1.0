package mindG.mgip;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Hashtable;
import java.util.Queue;

import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.Request;
import mindG.network.Node;
import mindG.network.PropositionNode;

public class Scheduler {
    private static Queue<Report> highQueue;
    private static Queue<Request> lowQueue;
    private static Hashtable<Report, PropositionNode> forwardAssertedNodes;
    // mesh fahma leh hettet el forwardAssertedNodes

    public static void Scheduler() {
        highQueue = new ArrayDeque<Report>();
        lowQueue = new ArrayDeque<Request>();
        forwardAssertedNodes = new Hashtable<Report, PropositionNode>();
    }

    // to schedule the requests before being put on the low priority queue
    public static void scheduleRequests(Channel request) {

    }

    // to schedule the reports before being put on the high priority queue

    public static void scheduleReports(Report report) {

    }

    // The main scheduling method of dequeuing of the queue which request/report
    // will be processed next
    public static String schedule() {
        return null;

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
    public static void addNodeAssertionThroughFReport(Report report, PropositionNode node) {
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
        return null; // request not found
    }

    public static Queue<Report> getHighQueue() {
        return highQueue;
    }

    public static void setHighQueue(Queue<Report> highQueue) {
        Scheduler.highQueue = highQueue;
    }

}
