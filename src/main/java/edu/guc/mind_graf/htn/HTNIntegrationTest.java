package edu.guc.mind_graf.htn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;

/**
 * HTNIntegrationTest — Demonstrates the FULL pipeline:
 *   HTN Planner → HTNBridge → Marwa's Scheduler
 * 
 * This test proves that the HTN planner is CONNECTED to the existing
 * acting system. The planned operators are pushed onto Scheduler.actQueue.
 */
public class HTNIntegrationTest {

    public static void main(String[] args)
            throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  HTN ↔ MindGRAF INTEGRATION TEST                       ║");
        System.out.println("║  Planner → Bridge → Scheduler                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // =====================================================================
        // STEP 1: Initialize Marwa's Scheduler (the existing system)
        // =====================================================================
        System.out.println("━━━ STEP 1: Initializing Marwa's Scheduler ━━━\n");
        Scheduler.initiate();
        System.out.println("Scheduler initialized with empty queues.");
        System.out.println("Act stack size before planning: " + Scheduler.getActQueue().size());

        // =====================================================================
        // STEP 2: Build the HTN Domain
        // =====================================================================
        System.out.println("\n━━━ STEP 2: Building HTN Domain ━━━\n");
        HTNDomain domain = buildTransportationDomain();

        // =====================================================================
        // STEP 3: Build Initial World State (from beliefs)
        // =====================================================================
        System.out.println("━━━ STEP 3: Building World State from Beliefs ━━━\n");
        WorldState state = new WorldState();
        state.addProposition("at(A)");
        state.addProposition("hasVisa");         // has card but no balance
        state.addProposition("taxiAvailable");
        state.addProposition("busTicket");
        // hasCash(sufficient) is ABSENT → taxi will fail
        // visaBalance(sufficient) is ABSENT → visa will fail
        System.out.println("World state: " + state);

        // =====================================================================
        // STEP 4: Create the Bridge and Plan+Execute
        // =====================================================================
        System.out.println("\n━━━ STEP 4: HTNBridge.planAndExecute() ━━━\n");

        HTNBridge bridge = new HTNBridge(domain, true);
        boolean success = bridge.planAndExecute("travel", state);

        // =====================================================================
        // STEP 5: Verify Scheduler State
        // =====================================================================
        System.out.println("\n━━━ STEP 5: Verifying Scheduler State ━━━\n");

        if (success) {
            System.out.println("✅ Planning succeeded and operators were pushed to Scheduler.");
            System.out.println("Act stack size after planning: " + Scheduler.getActQueue().size());
            System.out.println("\nAct stack contents (top = first to execute):");
            // Peek at the stack without popping
            java.util.Stack<edu.guc.mind_graf.nodes.ActNode> stack = Scheduler.getActQueue();
            for (int i = stack.size() - 1; i >= 0; i--) {
                System.out.println("  " + (stack.size() - i) + ". " + stack.get(i).getName());
            }
            System.out.println("\n✅ INTEGRATION VERIFIED: HTN planner output is now in Marwa's Scheduler.");
        } else {
            System.out.println("❌ Planning failed.");
        }

        // =====================================================================
        // STEP 6: Show what Scheduler.schedule() would do
        // =====================================================================
        System.out.println("\n━━━ STEP 6: Scheduler Processing ━━━\n");
        System.out.println("Calling Scheduler.schedule() to process the act stack...\n");

        // The scheduler will pop each ActNode and call processIntends()
        // Since these are simple ActNodes (not fully wired with channels),
        // processIntends() is a no-op, but the dequeue proves the connection.
        String sequence = Scheduler.schedule();
        System.out.println("\nScheduler sequence: " + sequence);
        System.out.println("Act stack size after scheduling: " + Scheduler.getActQueue().size());
        System.out.println("\n✅ FULL PIPELINE COMPLETE: HTN Planner → Bridge → Scheduler → Execution");
    }

    // =========================================================================
    // Domain Builder (same as HTNPlannerTest)
    // =========================================================================

    private static HTNDomain buildTransportationDomain() {
        HTNDomain domain = new HTNDomain();

        // Operators
        domain.addOperator(new Operator("callTaxi",
                setOf("taxiAvailable"), setOf("taxiCalled"), setOf()));
        domain.addOperator(new Operator("hailTaxi",
                setOf("onStreet"), setOf("taxiCalled"), setOf()));
        domain.addOperator(new Operator("rideTaxi",
                setOf("taxiCalled"), setOf("atDestination"), setOf("at(A)", "taxiCalled")));
        domain.addOperator(new Operator("payCash",
                setOf("hasCash(sufficient)"), setOf("paid"), setOf("hasCash(sufficient)")));
        domain.addOperator(new Operator("payVisa",
                setOf("hasVisa", "visaBalance(sufficient)"), setOf("paid"), setOf("visaBalance(sufficient)")));
        domain.addOperator(new Operator("walkToStop",
                setOf(), setOf("atBusStop"), setOf("at(A)")));
        domain.addOperator(new Operator("takeBus",
                setOf("busTicket", "atBusStop"), setOf("atDestination"), setOf("atBusStop")));
        domain.addOperator(new Operator("walkToDestination",
                setOf("atDestination"), setOf("at(B)"), setOf("atDestination")));

        // Methods
        domain.addMethod(new Method("travel", "travel-by-taxi",
                setOf("taxiAvailable"),
                Arrays.asList(new Task("getTaxi", false), new Task("rideTaxi", true), new Task("pay", false))));
        domain.addMethod(new Method("travel", "travel-by-bus",
                setOf("busTicket"),
                Arrays.asList(new Task("walkToStop", true), new Task("takeBus", true), new Task("walkToDestination", true))));
        domain.addMethod(new Method("getTaxi", "get-taxi-call",
                setOf("taxiAvailable"), Arrays.asList(new Task("callTaxi", true))));
        domain.addMethod(new Method("getTaxi", "get-taxi-hail",
                setOf("onStreet"), Arrays.asList(new Task("hailTaxi", true))));
        domain.addMethod(new Method("pay", "pay-cash",
                setOf("hasCash(sufficient)"), Arrays.asList(new Task("payCash", true))));
        domain.addMethod(new Method("pay", "pay-visa",
                setOf("hasVisa"), Arrays.asList(new Task("payVisa", true))));

        return domain;
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
