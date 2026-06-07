package edu.guc.mind_graf.htn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TransportationDomainDemo — the classic SHOP transportation-planning domain.
 *
 * ==========================================================================
 * SOURCE
 * ==========================================================================
 * This domain is the worked example from the SHOP paper:
 *
 *   D. Nau, Y. Cao, A. Lotem, and H. Munoz-Avila,
 *   "SHOP: Simple Hierarchical Ordered Planner",
 *   IJCAI 1999 (Tables 1-3).
 *
 * The agent wants to travel from one location to another in a city. There are
 * three modes of transport:
 *   - taxi : hail a taxi, ride to the destination, pay $1.50 + $1.00 per mile;
 *   - bus  : wait for the bus, pay a flat $1.00, ride to the destination;
 *   - foot : just walk, but only if the destination is within walking distance
 *            (<= 3 miles in good weather, <= 0.5 miles otherwise).
 *
 * ==========================================================================
 * ADAPTATION (important)
 * ==========================================================================
 * The original SHOP domain uses axioms, a Lisp evaluator, and numeric
 * computations (e.g. have-taxi-fare = cash >= 1.5 + distance). Our standalone
 * planner is a pure STRIPS/propositional Total-Order Forward Decomposition
 * engine with no numeric evaluator. We therefore model the numeric facts as
 * PROPOSITIONS that a domain author would pre-compute for a concrete trip:
 *
 *   walkable(dest)        <- distance(dest) is within the weather's walk limit
 *   canAffordTaxi(dest)   <- cash >= taxi fare to dest (1.5 + distance)
 *   canAffordBus          <- cash >= 1.00
 *
 * With those propositions, the decomposition structure, method order, and
 * resulting plans match the SHOP paper exactly for the Table-2 example.
 *
 * ==========================================================================
 * DOMAIN STRUCTURE (matches SHOP Table 1, methods M2/M3, operators O1-O5)
 * ==========================================================================
 *   travel  (compound):
 *     travel-by-foot  -> [walk]                       (M2)
 *     travel-by-taxi  -> [hailTaxi, rideTaxi, payDriver]   (M3, taxi branch)
 *     travel-by-bus   -> [waitForBus, payBus, rideBus]     (M3, bus branch)
 *
 *   Operators: walk (O5), hailTaxi (O1), rideTaxi (O3), payDriver (O4),
 *              waitForBus (O2), payBus (O4), rideBus (O3).
 *
 * ==========================================================================
 * TABLE-2 SCENARIO: go to the suburb, good weather, have $12
 * ==========================================================================
 *   distance(suburb) = 12  -> NOT walkable (12 > 3)        -> foot method skipped
 *   taxi fare        = 13.5 > 12 -> NOT canAffordTaxi      -> taxi branch fails
 *   bus fare         = 1.00 <= 12 -> canAffordBus, busRoute -> BUS SUCCEEDS
 *
 *   Expected plan: [waitForBus, payBus, rideBus]
 *   (matches SHOP Table 3: "(wait-for bus3)(set-cash 12 11.0)(ride bus3 ...)")
 */
public class TransportationDomainDemo {

    public static void main(String[] args) {
        System.out.println("=== SHOP Transportation Domain (Nau et al., IJCAI 1999) ===\n");

        // Scenario A: the SHOP Table-2 example -> bus wins (taxi too dear, too far to walk).
        runScenario("A  go to suburb, good weather, $12  (expect: BUS)",
                stateFor(/*walkable*/false, /*canAffordTaxi*/false, /*canAffordBus*/true));

        // Scenario B: same trip but with plenty of cash -> taxi wins (tried before bus).
        runScenario("B  go to suburb, good weather, $80  (expect: TAXI)",
                stateFor(/*walkable*/false, /*canAffordTaxi*/true, /*canAffordBus*/true));

        // Scenario C: a short hop in good weather -> walking wins (tried first).
        runScenario("C  short hop, good weather       (expect: WALK)",
                stateFor(/*walkable*/true, /*canAffordTaxi*/true, /*canAffordBus*/true));

        // Scenario D: nothing affordable and too far to walk -> no plan.
        runScenario("D  too far, no money              (expect: FAIL)",
                stateFor(/*walkable*/false, /*canAffordTaxi*/false, /*canAffordBus*/false));
    }

    private static void runScenario(String title, WorldState state) {
        System.out.println("----------------------------------------------------------");
        System.out.println("Scenario " + title);
        System.out.println("Initial state: " + state);
        HTNDomain domain = buildDomain();
        Task goal = new Task("travel", new String[]{"downtown", "suburb"}, false);
        HTNPlanner planner = new HTNPlanner(true); // verbose trace
        PlanResult result = planner.plan(goal, state, domain);
        if (result.isSuccess()) {
            System.out.print("PLAN:");
            for (Operator op : result.getPlan()) {
                System.out.print(" " + op.getName());
            }
            System.out.println();
        } else {
            System.out.println("PLAN: (none -- planning failed)");
        }
        System.out.println();
    }

    /**
     * Builds the SHOP transportation domain (propositional adaptation).
     * Methods are added foot -> taxi -> bus, matching SHOP's try order.
     */
    private static HTNDomain buildDomain() {
        HTNDomain domain = new HTNDomain();

        // ---- Operators (SHOP O1-O5) ----
        // O5 !walk: del at(start), add at(dest)
        domain.addOperator(new Operator("walk",
                setOf("at(downtown)", "walkable(suburb)"),
                setOf("at(suburb)"), setOf("at(downtown)")));
        // O1 !hail: add at(taxi,here) == taxiHere
        domain.addOperator(new Operator("hailTaxi",
                setOf("taxiStand(downtown)"), setOf("taxiHere"), setOf()));
        // O3 !ride (taxi): del at(start)+vehicle-here, add at(dest)
        domain.addOperator(new Operator("rideTaxi",
                setOf("at(downtown)", "taxiHere"),
                setOf("at(suburb)"), setOf("at(downtown)", "taxiHere")));
        // O4 set-cash (pay the driver the taxi fare)
        domain.addOperator(new Operator("payDriver",
                setOf("canAffordTaxi(suburb)"), setOf("taxiPaid"), setOf()));
        // O2 !wait-for: add at(bus,here) == busHere
        domain.addOperator(new Operator("waitForBus",
                setOf("busRoute(suburb)"), setOf("busHere"), setOf()));
        // O4 set-cash (pay the flat bus fare)
        domain.addOperator(new Operator("payBus",
                setOf("canAffordBus"), setOf("busPaid"), setOf()));
        // O3 !ride (bus)
        domain.addOperator(new Operator("rideBus",
                setOf("at(downtown)", "busHere"),
                setOf("at(suburb)"), setOf("at(downtown)", "busHere")));

        // ---- Methods for travel (SHOP M2 foot, M3 taxi/bus) ----
        domain.addMethod(new Method("travel", "travel-by-foot",
                setOf("walkable(suburb)"),
                Arrays.asList(new Task("walk", true))));
        domain.addMethod(new Method("travel", "travel-by-taxi",
                setOf("taxiStand(downtown)"),
                Arrays.asList(new Task("hailTaxi", true),
                              new Task("rideTaxi", true),
                              new Task("payDriver", true))));
        domain.addMethod(new Method("travel", "travel-by-bus",
                setOf("busRoute(suburb)"),
                Arrays.asList(new Task("waitForBus", true),
                              new Task("payBus", true),
                              new Task("rideBus", true))));
        return domain;
    }

    /**
     * The fixed facts of the Table-2 city (taxi stand and bus route downtown,
     * agent at downtown, good weather), plus the three pre-computed numeric
     * facts that the caller toggles to select the scenario.
     */
    private static WorldState stateFor(boolean walkable, boolean canAffordTaxi, boolean canAffordBus) {
        WorldState s = new WorldState();
        s.addProposition("at(downtown)");
        s.addProposition("weatherGood");
        s.addProposition("taxiStand(downtown)");
        s.addProposition("busRoute(suburb)");
        if (walkable)      s.addProposition("walkable(suburb)");
        if (canAffordTaxi) s.addProposition("canAffordTaxi(suburb)");
        if (canAffordBus)  s.addProposition("canAffordBus");
        return s;
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
