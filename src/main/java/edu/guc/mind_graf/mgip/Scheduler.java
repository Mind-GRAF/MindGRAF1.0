package edu.guc.mind_graf.mgip;

import java.util.ArrayDeque;
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
    private static Queue<Report> highQueue;
    private static Queue<Request> lowQueue;
    private static Stack<ActNode> actQueue;
    private static Stack<ActNode> highActQueue;
    private static PropositionNode originOfBackInf;
    private static Hashtable<Report, PropositionNode> forwardAssertedNodes;
    private static Hashtable<Report, PropositionNode> backwardAssertedReplyNodes;

    public static void initiate() {
        highQueue = new ArrayDeque<Report>();
        lowQueue = new ArrayDeque<Request>();
        actQueue = new Stack<ActNode>();
        highActQueue = new Stack<ActNode>();
        
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
                toRunNext.processIntends(true);
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
                toRunNext.processIntends(false);
                sequence += "A ";
                if (!highQueue.isEmpty() || !lowQueue.isEmpty()|| !highActQueue.isEmpty()) {
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

    public static void setActQueue(Stack<ActNode> actQueue) {
        Scheduler.actQueue = actQueue;
    }

    public static PropositionNode getOriginOfBackInf() {
        return originOfBackInf;
    }

    public static void setOriginOfBackInf(PropositionNode originOfBackInf) {
        Scheduler.originOfBackInf = originOfBackInf;
    }

}
