# HTN Planning Package — `src/htn/`

> ⚠️ **STATUS — read this first.** This package is the verified **standalone** SHOP
> planner. It is **not currently wired into the MindGRAF runtime**; the runtime uses
> its own native decomposition (`isControlAct()` routing, a single `executionQueue`,
> and a choice-point stack with a per-node `DoOne` retry agenda). The "integration"
> described below is how the planner *could* be wired in (future work). For the
> changes actually made to the acting system, see
> `docs/MARWA_HTN_INTEGRATION_CHANGES.md`.

## Integration with MindGRAF Acting System

This package extends the MindGRAF acting system with SHOP-style HTN (Hierarchical Task Network) planning. It is **connected** to Marwa's existing system via `HTNBridge`, which pushes planned operators onto the `Scheduler`'s act stack.

### Connection Points

```
HTNPlanner (this package)
     ↓ produces PlanResult
HTNBridge (this package)
     ↓ calls Scheduler.addToActQueue()
Scheduler (mgip package — Marwa's code)
     ↓ pops ActNode from act stack
ActNode.processIntends() (nodes package — Marwa's code)
```

### Files

| File | Purpose |
|------|---------|
| `Task.java` | Unit of work — primitive or compound |
| `Operator.java` | STRIPS primitive action (preconditions + add/delete lists) |
| `Method.java` | Decomposition rule for compound tasks |
| `WorldState.java` | Planner's mental scratchpad (set of propositions) |
| `HTNDomain.java` | Registry of all operators and methods |
| `PlanResult.java` | Plan wrapper (success with operator list, or failure) |
| `HTNPlanner.java` | SHOP algorithm — recursive DFS with backtracking |
| `HTNBridge.java` | **INTEGRATION**: connects planner output to Scheduler |
| `HTNPlannerTest.java` | Standalone planner test (3 scenarios) |
| `HTNIntegrationTest.java` | **Full pipeline test**: Planner → Bridge → Scheduler |

### Build & Run

```bash
# Compile everything (htn + dependencies)
javac -d bin -sourcepath src src/htn/HTNIntegrationTest.java

# Run integration test (full pipeline)
java -cp bin htn.HTNIntegrationTest

# Run standalone planner test
java -cp bin htn.HTNPlannerTest
```

---

# How the System Works: Before & After HTN

---

## PART 1 — Marwa's Acting System (Before HTN)

### The Core Idea

Marwa's system is **reactive**. An act goes through a **state machine** (called the "agenda") before it can execute. The act is repeatedly pushed onto the act stack and processed one stage at a time.

### The Agenda Lifecycle

Every `ActNode` has an agenda that tracks where it is in its lifecycle:

```
START → FIND_PRECONDITIONS → TEST → EXECUTE → FIND_PLANS → DONE
```

Here's what happens at each stage:

**1. START**
- The system asks: "Does this act have any preconditions?"
- It builds a Precondition-Act transformer and sends a **backward chaining request** through the low priority queue
- Agenda → `FIND_PRECONDITIONS`
- Act is pushed back onto the act stack

**2. FIND_PRECONDITIONS**
- The system checks if any reports came back from the request
- If reports = null → the act has **no preconditions** → skip to `EXECUTE`
- If reports exist → the act HAS preconditions → agenda → `TEST`
- Sends requests to each precondition to check if they're asserted
- Act is pushed back onto the act stack

**3. TEST**
- Calls `isPreconditionsAsserted()` — checks if all preconditions are supported in the current context in the belief attitude
- If **all asserted** → agenda → `EXECUTE`
- If **not asserted** → agenda → `START` again, and schedules AchieveNodes to try to assert them
- Act is pushed back onto the act stack

**4. EXECUTE**
- If the act is **primitive** → run the actuator directly, agenda → `DONE`
- If the act is **complex** → it can't run directly, so it asks: "Does this act have plans?"
- Sends a backward chaining request using a Plan-Act transformer
- Agenda → `FIND_PLANS`
- Act is pushed back onto the act stack

**5. FIND_PLANS**
- Checks reports from the Plan-Act request
- If plans = null → "Act can't be performed" (message to user)
- If plans exist → schedules a **DoOneNode** pointing to the found plans
- DoOneNode picks ONE plan and schedules it
- Agenda → `DONE`

### The Three Queues

The Scheduler runs this cycle:

```
┌──────────────────────────────────────────────┐
│  HIGH PRIORITY QUEUE  →  Process Reports     │
│  LOW PRIORITY QUEUE   →  Process Requests    │
│  ACT STACK            →  Process Acts        │
│                                              │
│  Loop until all queues empty                 │
└──────────────────────────────────────────────┘
```

Reports flow first (high), then requests (low), then acts are processed one stage at a time. An act is rescheduled on the stack after each stage, so it goes through multiple cycles.

### The Problem

> Marwa's system has **no search**. It picks ONE plan (via DoOneNode). If that plan fails, the system is stuck.

---

## PART 2 — The Same System With HTN Planner

### What Changes

The HTN planner inserts itself **between "identify goal" and "execute."** Instead of going through the agenda one-act-at-a-time, the planner simulates the ENTIRE decomposition tree up front.

### The New Flow

```
Agent has a goal (e.g., travel(A,B))
         ↓
Convert to HTN Task (compound)
         ↓
HTNPlanner.seekPlan(initialState, [travel], domain, [])
         ↓
Recursive DFS: decompose → check preconditions → simulate → backtrack
         ↓
Returns PlanResult: either [op1, op2, op3, ...] or FAIL
         ↓
If success → HTNBridge pushes operators to Scheduler for execution
If fail    → report failure
```

### What the Planner Does Differently

| Marwa's System | HTN Planner |
|---|---|
| Discovers preconditions **one at a time** via requests/reports | Checks ALL preconditions **instantly** via WorldState |
| Discovers plans **one at a time** via Plan-Act transformer | Has ALL methods registered in HTNDomain upfront |
| Picks ONE plan (DoOneNode), hopes it works | Tries ALL methods via DFS, backtracks on failure |
| Executes during discovery (interleaved) | Plans FIRST, executes AFTER (separated) |
| If a plan fails → stuck | If a plan fails → backtracks to try alternatives |

---

## PART 3 — Transportation Domain: Side by Side

### Setup

- Agent is at location A, wants to get to location B
- Has 5 cash (fare costs 10)
- Has a visa card (but balance = 0)
- Has a bus ticket
- Taxi is available

### Marwa's System (Before)

```
1. ActNode "travel(A,B)" is pushed onto act stack
   agenda = START

2. START: send request — "does travel have preconditions?"
   → reports come back (e.g., none or already asserted)
   agenda = FIND_PRECONDITIONS → TEST → EXECUTE

3. EXECUTE: travel is COMPLEX (not primitive)
   → send request — "does travel have plans?"
   → Plan-Act transformer finds one plan: taxi
   agenda = FIND_PLANS

4. FIND_PLANS: schedule DoOneNode with plan "taxi"
   → DoOneNode picks taxi sequence
   → Pushes: [callTaxi, rideTaxi, pay] onto act stack
   agenda = DONE for travel

5. callTaxi processes:
   START → FIND_PRECONDITIONS → TEST (taxiAvailable? ✅)
   → EXECUTE (primitive: call the taxi) ✅

6. rideTaxi processes:
   START → FIND_PRECONDITIONS → TEST (taxiCalled? ✅)
   → EXECUTE (primitive: ride) ✅

7. pay processes:
   START → FIND_PRECONDITIONS → TEST
   → "hasCash(sufficient)?" → ❌ NOT ASSERTED
   → Try to achieve it? Can't add money.
   → ❌ STUCK

   The system has NO mechanism to go back and try the bus instead.
   The taxi plan was already committed to.
```

**Result: FAILURE. System is stuck at pay.**

### HTN Planner (After)

```
1. Goal: travel(A,B) → compound Task

2. seekPlan(S={at(A), hasVisa, taxiAvailable, busTicket}, T=[travel], p=[])

3. travel is COMPOUND → try methods in order

4. Method 1: travel-by-taxi
   precondition {taxiAvailable} → ✅ in state
   Decompose: T = [getTaxi, rideTaxi, pay]

   5. getTaxi is COMPOUND → try methods
      Method: get-taxi-call → [callTaxi]
      
      6. callTaxi is PRIMITIVE
         precondition {taxiAvailable} → ✅
         SIMULATE: S' = S + {taxiCalled}
         p = [callTaxi]

      7. rideTaxi is PRIMITIVE
         precondition {taxiCalled} → ✅ (just added)
         SIMULATE: S'' = S' + {atDestination} - {at(A), taxiCalled}
         p = [callTaxi, rideTaxi]

      8. pay is COMPOUND → try methods
         Method 1: pay-cash
           precondition {hasCash(sufficient)} → ❌ NOT IN STATE
           SKIP

         Method 2: pay-visa
           precondition {hasVisa} → ✅
           Decompose: [payVisa]
           
           9. payVisa is PRIMITIVE
              precondition {hasVisa, visaBalance(sufficient)}
              visaBalance(sufficient) → ❌ NOT IN STATE
              return FAIL                          ← failure at operator

         ← pay-visa method failed
         ← ALL pay methods exhausted → return FAIL  ← BACKTRACK POINT 1

      ← taxi branch killed by pay failure
      ← return FAIL propagates up                   ← BACKTRACK POINT 2

   ← travel-by-taxi FAILED

10. Method 2: travel-by-bus                          ← BACKTRACK POINT 3
    precondition {busTicket} → ✅
    Decompose: T = [walkToStop, takeBus, walkToDestination]
    
    STATE IS ORIGINAL: {at(A), hasVisa, taxiAvailable, busTicket}
    (taxi's state changes were discarded — they were copies)

    11. walkToStop: precondition {} → ✅ (always)
        SIMULATE: S' = S + {atBusStop} - {at(A)}
        p = [walkToStop]

    12. takeBus: precondition {busTicket, atBusStop} → ✅
        SIMULATE: S'' = S' + {atDestination} - {atBusStop}
        p = [walkToStop, takeBus]

    13. walkToDestination: precondition {atDestination} → ✅
        SIMULATE: S''' = S'' + {at(B)} - {atDestination}
        p = [walkToStop, takeBus, walkToDestination]

    14. T = [] → BASE CASE → return SUCCESS

FINAL PLAN: [walkToStop, takeBus, walkToDestination]
→ HTNBridge pushes to Scheduler → Execution
```

**Result: SUCCESS. Bus alternative found automatically via backtracking.**

---

## The Key Difference in One Sentence

> **Marwa's system commits to one plan and discovers failure during execution. The HTN planner explores all alternatives in simulation first, and only commits when it finds one that works.**
