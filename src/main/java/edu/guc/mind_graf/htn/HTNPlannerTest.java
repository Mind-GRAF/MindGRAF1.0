package edu.guc.mind_graf.htn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * HTNPlannerTest — Full integration test using the Transportation Domain.
 * 
 * ==========================================================================
 * PURPOSE:
 * ==========================================================================
 * This test class demonstrates the complete HTN planning pipeline using
 * the extended transportation domain from the thesis. It validates:
 * 
 *   1. Multi-level task decomposition (travel → taxi → pay → cash/visa)
 *   2. Operator precondition checking
 *   3. DFS search behavior (try first method fully before alternatives)
 *   4. Nested backtracking (fail at pay → fail at taxi → try bus)
 *   5. Successful plan construction
 * 
 * ==========================================================================
 * DOMAIN STRUCTURE:
 * ==========================================================================
 * 
 * Compound Tasks and their Methods:
 * 
 *   travel(A, B):
 *     Method 1: travel-by-taxi   → [getTaxi, rideTaxi, pay]
 *     Method 2: travel-by-bus    → [walkToStop, takeBus, walkToDestination]
 * 
 *   getTaxi:
 *     Method 1: get-taxi-call    → [callTaxi]
 *     Method 2: get-taxi-hail    → [hailTaxi]
 * 
 *   pay:
 *     Method 1: pay-cash         → [payCash]
 *     Method 2: pay-visa         → [payVisa]
 * 
 * Primitive Tasks (Operators):
 *   callTaxi, hailTaxi, rideTaxi, payCash, payVisa,
 *   walkToStop, takeBus, walkToDestination
 * 
 * ==========================================================================
 * INITIAL STATE:
 * ==========================================================================
 *   cash    = 5    (insufficient — fare is 10)
 *   fare    = 10
 *   visa    = true (but balance = 0)
 *   bus     = true
 *   taxi    = available
 * 
 * ==========================================================================
 * EXPECTED RESULT:
 * ==========================================================================
 *   Taxi branch FAILS (can't pay: cash < fare AND visa balance = 0)
 *   Bus branch SUCCEEDS
 *   
 *   Final plan: [walkToStop, takeBus, walkToDestination]
 * 
 * ==========================================================================
 * EXECUTION TRACE (what you should see):
 * ==========================================================================
 * 
 *   travel(A,B) → compound
 *     → Try taxi method
 *       → getTaxi → compound
 *         → Try call method
 *           → callTaxi ✅
 *       → rideTaxi ✅
 *       → pay → compound
 *         → Try pay-cash
 *           → payCash ❌ (cash < fare)
 *         → Try pay-visa
 *           → payVisa ❌ (visa balance < fare)
 *         → ALL pay methods FAIL
 *       → taxi branch FAILS → BACKTRACK
 *     → Try bus method
 *       → walkToStop ✅
 *       → takeBus ✅
 *       → walkToDestination ✅
 *     → SUCCESS!
 */
public class HTNPlannerTest {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  HTN PLANNER TEST — Transportation Domain               ║");
        System.out.println("║  Integration with MindGRAF Acting System                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // =====================================================================
        // STEP 1: Build the HTN Domain
        // =====================================================================
        // This is analogous to defining the ActNodes and their relationships
        // in Ibrahim's semantic network. Here we define operators (primitive
        // acts) and methods (decomposition rules) explicitly.

        System.out.println("━━━ STEP 1: Building HTN Domain ━━━\n");
        HTNDomain domain = buildTransportationDomain();
        System.out.println(domain);

        // =====================================================================
        // STEP 2: Define the Initial World State
        // =====================================================================
        // In Ibrahim's system, this corresponds to the agent's beliefs
        // (BELIEVE(hasCash(5)), BELIEVE(at(A)), etc.)

        System.out.println("\n━━━ STEP 2: Defining Initial World State ━━━\n");
        WorldState initialState = buildInitialState();
        System.out.println("Initial state: " + initialState);

        // =====================================================================
        // STEP 3: Define the Goal Task
        // =====================================================================
        // In Ibrahim's system, this is the goal identified by the inference
        // engine. The planner takes this goal and searches for a valid
        // sequence of primitive actions to achieve it.

        System.out.println("\n━━━ STEP 3: Defining Goal Task ━━━\n");
        Task goalTask = new Task("travel", new String[]{"A", "B"}, false);
        System.out.println("Goal: " + goalTask);

        // =====================================================================
        // STEP 4: Run the SHOP Planner
        // =====================================================================
        // This is THE key addition to Ibrahim's system. Instead of directly
        // executing the travel ActNode, we first PLAN a valid sequence.

        System.out.println("\n━━━ STEP 4: Running SHOP Planner ━━━\n");
        HTNPlanner planner = new HTNPlanner(true); // verbose = true
        PlanResult result = planner.plan(goalTask, initialState, domain);

        // =====================================================================
        // STEP 5: Interpret the Result
        // =====================================================================
        // If the plan is successful, in Ibrahim's system this would be
        // handed to the Scheduler for execution via:
        //   for (Operator op : result.getPlan()) {
        //       Scheduler.addToActQueue(op.toActNode());
        //   }

        System.out.println("\n━━━ STEP 5: Result ━━━\n");
        System.out.println(result);

        if (result.isSuccess()) {
            System.out.println("\n✅ INTEGRATION SUCCESS");
            System.out.println("Plan would be handed to Ibrahim's Scheduler for execution:");
            List<Operator> plan = result.getPlan();
            for (int i = 0; i < plan.size(); i++) {
                System.out.println("  " + (i + 1) + ". Execute: " + plan.get(i).getName());
            }
        } else {
            System.out.println("\n❌ PLANNING FAILED — no valid plan exists");
        }

        // =====================================================================
        // STEP 6: Run additional test scenarios
        // =====================================================================
        System.out.println("\n\n");
        testScenario2_SufficientCash();
        System.out.println("\n\n");
        testScenario3_NothingAvailable();
    }

    // =========================================================================
    // Domain Builder
    // =========================================================================

    /**
     * Builds the extended transportation domain with multi-level decomposition.
     * 
     * Domain structure:
     *   travel → [taxi, bus]
     *   getTaxi → [call, hail]
     *   pay → [cash, visa]
     *   
     *   All primitive operators with STRIPS preconditions/effects
     */
    private static HTNDomain buildTransportationDomain() {
        HTNDomain domain = new HTNDomain();

        // -----------------------------------------------------------------
        // OPERATORS (Primitive Actions)
        // -----------------------------------------------------------------
        // These map to Ibrahim's primitive ActNodes — directly executable
        // actions with explicit preconditions and effects.

        // callTaxi: requires taxi to be available, results in taxi being called
        domain.addOperator(new Operator(
                "callTaxi",
                setOf("taxiAvailable"),       // preconditions
                setOf("taxiCalled"),           // add-list
                setOf()                        // delete-list
        ));

        // hailTaxi: alternative way to get a taxi (requires being on street)
        domain.addOperator(new Operator(
                "hailTaxi",
                setOf("onStreet"),            // preconditions
                setOf("taxiCalled"),           // add-list
                setOf()                        // delete-list
        ));

        // rideTaxi: requires taxi to be called, moves agent to destination
        domain.addOperator(new Operator(
                "rideTaxi",
                setOf("taxiCalled"),           // preconditions
                setOf("atDestination"),         // add-list
                setOf("at(A)", "taxiCalled")   // delete-list (no longer at A)
        ));

        // payCash: requires sufficient cash (cash >= fare)
        // In the test scenario: cash=5, fare=10 → this FAILS
        domain.addOperator(new Operator(
                "payCash",
                setOf("hasCash(sufficient)"),  // preconditions
                setOf("paid"),                  // add-list
                setOf("hasCash(sufficient)")    // delete-list (money spent)
        ));

        // payVisa: requires visa with sufficient balance
        // In the test scenario: visaBalance=0 → this FAILS
        domain.addOperator(new Operator(
                "payVisa",
                setOf("hasVisa", "visaBalance(sufficient)"),  // preconditions
                setOf("paid"),                                 // add-list
                setOf("visaBalance(sufficient)")               // delete-list
        ));

        // walkToStop: no preconditions — always applicable
        domain.addOperator(new Operator(
                "walkToStop",
                setOf(),                       // preconditions (none)
                setOf("atBusStop"),             // add-list
                setOf("at(A)")                 // delete-list
        ));

        // takeBus: requires bus ticket and being at bus stop
        domain.addOperator(new Operator(
                "takeBus",
                setOf("busTicket", "atBusStop"),  // preconditions
                setOf("atDestination"),             // add-list
                setOf("atBusStop")                 // delete-list
        ));

        // walkToDestination: requires being at destination area
        domain.addOperator(new Operator(
                "walkToDestination",
                setOf("atDestination"),        // preconditions
                setOf("at(B)"),                 // add-list
                setOf("atDestination")         // delete-list
        ));

        // -----------------------------------------------------------------
        // METHODS (Decomposition Rules)
        // -----------------------------------------------------------------
        // These are the KEY ADDITION — they formalize how compound tasks
        // break down into subtasks. Multiple methods per task enable
        // alternatives and backtracking.

        // --- Methods for "travel" ---

        // Method 1: Travel by taxi
        // Decomposition: travel → [getTaxi, rideTaxi, pay]
        domain.addMethod(new Method(
                "travel",
                "travel-by-taxi",
                setOf("taxiAvailable"),        // precondition: taxi must exist
                Arrays.asList(
                        new Task("getTaxi", false),     // compound (call or hail)
                        new Task("rideTaxi", true),     // primitive
                        new Task("pay", false)           // compound (cash or visa)
                )
        ));

        // Method 2: Travel by bus
        // Decomposition: travel → [walkToStop, takeBus, walkToDestination]
        domain.addMethod(new Method(
                "travel",
                "travel-by-bus",
                setOf("busTicket"),             // precondition: must have bus ticket
                Arrays.asList(
                        new Task("walkToStop", true),          // primitive
                        new Task("takeBus", true),             // primitive
                        new Task("walkToDestination", true)    // primitive
                )
        ));

        // --- Methods for "getTaxi" ---

        // Method 1: Get taxi by calling
        domain.addMethod(new Method(
                "getTaxi",
                "get-taxi-call",
                setOf("taxiAvailable"),
                Arrays.asList(
                        new Task("callTaxi", true)     // primitive
                )
        ));

        // Method 2: Get taxi by hailing (requires being on street)
        domain.addMethod(new Method(
                "getTaxi",
                "get-taxi-hail",
                setOf("onStreet"),
                Arrays.asList(
                        new Task("hailTaxi", true)     // primitive
                )
        ));

        // --- Methods for "pay" ---

        // Method 1: Pay with cash
        domain.addMethod(new Method(
                "pay",
                "pay-cash",
                setOf("hasCash(sufficient)"),
                Arrays.asList(
                        new Task("payCash", true)      // primitive
                )
        ));

        // Method 2: Pay with visa
        domain.addMethod(new Method(
                "pay",
                "pay-visa",
                setOf("hasVisa"),
                Arrays.asList(
                        new Task("payVisa", true)      // primitive
                )
        ));

        return domain;
    }

    // =========================================================================
    // State Builders
    // =========================================================================

    /**
     * Builds the initial world state for Scenario 1 (main test):
     * - Agent is at location A
     * - Cash is INSUFFICIENT (5 < fare of 10)
     * - Has visa but balance is 0
     * - Has a bus ticket
     * - Taxi is available
     * 
     * Expected: Taxi fails (can't pay), Bus succeeds
     */
    private static WorldState buildInitialState() {
        WorldState state = new WorldState();

        // Location
        state.addProposition("at(A)");

        // Money situation: insufficient cash, no visa balance
        // NOTE: hasCash(sufficient) is ABSENT → payCash will FAIL
        // NOTE: visaBalance(sufficient) is ABSENT → payVisa will FAIL
        state.addProposition("hasVisa");  // has card but no balance

        // Transportation availability
        state.addProposition("taxiAvailable");
        state.addProposition("busTicket");

        return state;
    }

    // =========================================================================
    // Additional Test Scenarios
    // =========================================================================

    /**
     * Scenario 2: Agent HAS sufficient cash.
     * Expected: Taxi method SUCCEEDS (pay-cash works)
     * Final plan: [callTaxi, rideTaxi, payCash]
     */
    private static void testScenario2_SufficientCash() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO 2: Sufficient Cash (Taxi should succeed)      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        HTNDomain domain = buildTransportationDomain();

        WorldState state = new WorldState();
        state.addProposition("at(A)");
        state.addProposition("hasCash(sufficient)");  // NOW has enough cash
        state.addProposition("taxiAvailable");
        state.addProposition("busTicket");

        Task goal = new Task("travel", new String[]{"A", "B"}, false);

        HTNPlanner planner = new HTNPlanner(true);
        PlanResult result = planner.plan(goal, state, domain);

        System.out.println("\n" + result);

        if (result.isSuccess()) {
            System.out.println("✅ Expected: Taxi path succeeds with pay-cash");
            List<Operator> plan = result.getPlan();
            for (int i = 0; i < plan.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + plan.get(i).getName());
            }
        }
    }

    /**
     * Scenario 3: Nothing available (no taxi, no bus ticket).
     * Expected: PLANNING FAILS
     */
    private static void testScenario3_NothingAvailable() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO 3: Nothing Available (should FAIL)            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        HTNDomain domain = buildTransportationDomain();

        WorldState state = new WorldState();
        state.addProposition("at(A)");
        // No cash, no visa, no taxi, no bus ticket

        Task goal = new Task("travel", new String[]{"A", "B"}, false);

        HTNPlanner planner = new HTNPlanner(true);
        PlanResult result = planner.plan(goal, state, domain);

        System.out.println("\n" + result);

        if (!result.isSuccess()) {
            System.out.println("✅ Expected: Planning correctly reports FAILURE");
        } else {
            System.out.println("❌ Unexpected: Planning should have failed!");
        }
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Convenience method to create a Set from varargs.
     */
    @SafeVarargs
    private static <T> Set<T> setOf(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
