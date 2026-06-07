# MindGRAF + HTN Planning — Complete System Guide

---

## 1. System Overview

This project integrates a **SHOP-style HTN planner** into Ibrahim's MindGRAF acting system. The result is a **Plan-then-Execute** architecture that replaces blind reactive execution with deliberative search.

```
┌──────────────┐      ┌─────────────────────────┐
│              │      │       HTN Planner       │
│  Agent Goal  ├─────►│    (SHOP Algorithm)     │
│              │      │                         │
└──────────────┘      └───────────┬─────────────┘
                                  │
                             [PlanResult]
                                  │
                                  ▼
                      ┌─────────────────────────┐
                      │        HTNBridge        │
                      └───────────┬─────────────┘
                                  │
                             [ActNodes]
                                  │
                                  ▼
                      ┌─────────────────────────┐
                      │        Scheduler        │
                      │       (3 Queues)        │
                      └───────────┬─────────────┘
                                  │
                         [processIntends()]
                                  │
                                  ▼
                      ┌─────────────────────────┐
                      │        Execution        │
                      └─────────────────────────┘
```

---

## 2. Architecture: BEFORE (Ibrahim Only)

### 2.1 How Ibrahim's System Works

Ibrahim's system is **reactive**. Every `ActNode` goes through a state machine called the **agenda**, managed by the `Scheduler`. The act is pushed onto the act stack and re-processed at each stage until `DONE`.

### 2.2 The Agenda Lifecycle

```
       ┌───────┐
       │ START │
       └───┬───┘
           │ (Send backward chaining request)
           ▼
┌────────────────────┐      (No preconditions)       ┌─────────┐
│ FIND_PRECONDITIONS ├──────────────────────────────►│ EXECUTE │
└──────────┬─────────┘                               └───┬─────┘
           │ (Preconditions found)                       │
           ▼                                             │
       ┌───────┐     (All asserted)                      │
       │ TEST  ├─────────────────────────────────────────┤
       └───┬───┘                                         │
           │ (Not asserted, try to achieve)              │
           ▼                                             │
     [Back to START]                                     │
                                                         │
           ┌─────────────────────────────────────────────┘
           │
           │ (Primitive act)                         ┌──────┐
           ├────────────────────────────────────────►│ DONE │
           │                                         └───▲──┘
           │ (Complex act)                               │
           ▼                                             │
    ┌────────────┐     (Schedule DoOneNode)              │
    │ FIND_PLANS ├───────────────────────────────────────┘
    └────────────┘
```

**What happens at each stage:**

| Stage | What Happens |
|-------|-------------|
| **START** | Builds Precondition-Act transformer, sends backward chaining request |
| **FIND_PRECONDITIONS** | Checks if reports came back. If yes → TEST. If null → EXECUTE |
| **TEST** | Checks if preconditions are asserted in belief attitude. If not → achieve them |
| **EXECUTE** | Primitive → run actuator. Complex → send request to find plans |
| **FIND_PLANS** | Gets plans via Plan-Act transformer. Schedules DoOneNode to pick ONE |
| **DONE** | Act is finished |

### 2.3 The Three-Queue Scheduler

The `Scheduler` (`mgip/Scheduler.java`) runs a priority loop over three data structures:

```
┌────────────────────────────────────────────────────────┐
│ Scheduler.schedule() Loop                              │
│                                                        │
│  1. HIGH PRIORITY QUEUE (Queue<Report>)                │
│     - Process reports first                            │
│     - Loop until empty                                 │
│          │                                             │
│          ▼ (Empty?)                                    │
│  2. LOW PRIORITY QUEUE (Queue<Request>)                │
│     - Process requests second                          │
│     - Loop until empty                                 │
│          │                                             │
│          ▼ (Empty?)                                    │
│  3. ACT STACK (Stack<ActNode>)                         │
│     - Process acts last (LIFO)                         │
│     - If new reports/requests generated, jump back to 1│
└────────────────────────────────────────────────────────┘
```

**Key details from `Scheduler.java`:**
- `highQueue` = `Queue<Report>` — reports have highest priority
- `lowQueue` = `Queue<Request>` — backward/forward chaining requests
- `actQueue` = `Stack<ActNode>` — acts processed LIFO (last in, first out)
- The `main:` label with `continue main` ensures reports always preempt requests and acts

**Why a Stack for acts?** From Ibrahim's thesis: "Scheduling acts on a stack leading that last act scheduled is the first act to be performed." This means sub-plans (pushed later) execute before their parent.

### 2.4 The Problem

```
travel(A,B) is pushed onto act stack
  → FIND_PLANS finds taxi plan
  → DoOneNode picks taxi (the ONLY option tried)
  → callTaxi ✅, rideTaxi ✅, pay ❌
  → STUCK. No mechanism to go back and try bus.
```

> **Ibrahim's system has NO search and NO backtracking.** It picks ONE plan via DoOneNode. If that plan fails mid-execution, the system is stuck.

### 2.5 Before Flowchart — Transportation Domain

```
 travel(A,B) pushed to act stack
               │
               ▼
   START: find preconditions
               │
               ▼
  EXECUTE: complex act → find plans
               │
               ▼
  FIND_PLANS: Plan-Act transformer
               │
               ▼
      DoOneNode picks taxi
               │
               ▼
Push callTaxi, rideTaxi, pay to stack
               │
               ▼
callTaxi: taxiAvailable? ✅ Execute
               │
               ▼
rideTaxi: taxiCalled? ✅ Execute
               │
               ▼
pay: hasCash sufficient? ❌
               │
               ▼
  Try to achieve precondition
               │
               ▼
 [❌ STUCK — Cannot add money]
               │
               ▼
 [❌ No mechanism to try bus]
```

---

## 3. Architecture: AFTER (With HTN Planner)

### 3.1 What We Added

The HTN planner sits **between goal identification and execution**. Instead of discovering plans one-at-a-time through the channel/report system, the planner **simulates the entire decomposition tree upfront** using DFS with backtracking.

### 3.2 Complete System Flowchart

```
┌─────────────────────────────────┐
│ Agent has goal: travel(A,B)     │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│ Convert to HTN Task (compound)  │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│      HTNPlanner.seekPlan()      │
└────────────────┬────────────────┘
                 │
=================│=================
 HTN Planning Phase (Simulation Only)
=================│=================
                 │
       ┌─────────▼────────┐
       │ Task list empty? │───(Yes)──► [Return PlanResult.success(plan)]
       └─────────┬────────┘                  │
                 │(No)                       │
                 ▼                           │
      ┌──────────────────────┐               │
      │First task primitive? │               │
      └──────────┬───────────┘               │
       (Yes)     │     (No)                  │
         ┌───────┴───────┐                   │
         ▼               ▼                   │
 ┌──────────────┐  ┌──────────────────┐      │
 │Find Operator │  │Get all Methods   │      │
 │in HTNDomain  │  │for this task     │      │
 └───────┬──────┘  └─────┬────────────┘      │
         ▼               ▼                   │
 ┌──────────────┐  ┌──────────────────┐      │
 │Preconditions │  │Try next method   │◄─┐   │
 │hold in       │  │                  │  │   │
 │WorldState?   │  └─────┬────────────┘  │   │
 └───────┬──────┘        │               │   │
  (Yes)  │   (No)        ▼               │   │
   ┌─────┴────┐    ┌──────────────────┐  │   │
   ▼          ▼    │Method precon-    │  │   │
┌────────┐ ┌──────┐│ditions hold?     │  │   │
│SIMULATE│ │Return│└─────┬────────────┘  │   │
│apply   │ │FAIL  │ (Yes)│        (No)   │   │
│effects │ └──────┘      ▼          ┌────┴─┐ │
│to STATE│        ┌──────────────┐  │Skip  │ │
│COPY    │        │Replace task  │  │method│ │
└────┬───┘        │with method's │  └──────┘ │
     │            │subtasks      │           │
     ▼            └──────┬───────┘           │
┌────────┐               │                   │
│Add op  │               │                   │
│to plan,│               │                   │
│recurse │               │                   │
└────┬───┘               │                   │
     │                   │                   │
     └───────────────────┴─►(Back to Start)  │
                                             │
=================│===========================│
                 │                           │
                 ▼                           ▼
            ┌─────────┐             ┌──────────────────────┐
            │ FAILURE │             │HTNBridge.executePlan │
            └─────────┘             └────────┬─────────────┘
                                             │
                                             ▼
                                    ┌──────────────────────┐
                                    │Wrap each Operator as │
                                    │ActNode (isPrimitive= │
                                    │true, agenda=EXECUTE) │
                                    └────────┬─────────────┘
                                             │
                                             ▼
                                    ┌──────────────────────┐
                                    │Push to Scheduler act │
                                    │queue in reverse order│
                                    │(Stack = LIFO)        │
                                    └────────┬─────────────┘
                                             │
=============================================│
 Ibrahim's Execution Phase (Real)              │
=============================================│
                                             │
                                             ▼
                                    ┌──────────────────────┐
                                    │Scheduler.schedule()  │◄──┐
                                    │loop                  │   │
                                    └────────┬─────────────┘   │
                                             │                 │
                                             ▼                 │
                                    ┌──────────────────────┐   │
                                    │Pop ActNode from      │   │
                                    │act stack             │   │
                                    └────────┬─────────────┘   │
                                             │                 │
                                             ▼                 │
                                    ┌──────────────────────┐   │
                                    │processIntends()      │   │
                                    │→ EXECUTE             │   │
                                    └────────┬─────────────┘   │
                                             │                 │
                                             ▼                 │
                                    ┌──────────────────────┐   │
                                    │Run actuator          ├───┘
                                    └──────────────────────┘
```

### 3.3 The SHOP Algorithm

**Algorithm:** Total-Order Forward Decomposition (TFD)
**Search:** Depth-First Search (DFS) with Backtracking
**Reference:** Nau, Cao, Lotem, Muñoz-Avila (1999)

```
seekPlan(State S, TaskList T, Domain D, Plan p):
  1. If T is empty → return p (SUCCESS)
  2. t ← first(T), R ← rest(T)
  3. If t is PRIMITIVE:
     a. Find operator for t
     b. If operator applicable in S:
        - S' ← apply(operator, S)     ← STATE COPY, not mutation
        - return seekPlan(S', R, D, p ∪ {operator})
     c. Else → return FAIL
  4. If t is COMPOUND:
     a. For each method m for t:
        - If m.preconditions satisfied in S:
          - result ← seekPlan(S, m.subtasks ++ R, D, p)
          - If result ≠ FAIL → return result   ← FIRST SUCCESS WINS
     b. All methods failed → return FAIL        ← BACKTRACK
```

### 3.4 How Backtracking Works (Code-Level Proof)

```
 handleCompoundTask: travel
           │
           ▼
 for loop: try method 1/2 travel-by-taxi
           │
           ▼
 seekPlan() recurses deep into taxi branch
           │
           ├─► payCash ❌ → PlanResult.failure()
           │
           ├─► payVisa ❌ → PlanResult.failure()
           │
           ▼
 [❌ ALL pay methods exhausted → return FAIL]
           │
           ▼
 Failure propagates up to taxi method
           │
           ▼
 result.isSuccess() = false
           │
           ▼
 for loop continues: try method 2/2 travel-by-bus
           │
           ▼
 seekPlan() recurses into bus branch
           │
           ▼
 [✅ walkToStop → takeBus → walkToDestination]
           │
           ▼
 Task list empty → [✅ PlanResult.success()]
           │
           ▼
 result.isSuccess() = true → return result
```

The backtracking mechanism in `HTNPlanner.java` lines 332-374:
1. The `for` loop iterates methods: `for (int i = 0; i < methods.size(); i++)`
2. Each method triggers `seekPlan()` recursively (line 352)
3. If `result.isSuccess()` is false → loop continues to `i++` (next method)
4. If ALL methods fail → returns `PlanResult.failure()` (line 374), propagating up

**State safety:** `WorldState.applyOperator()` creates a **copy** — original state survives for backtracking. No undo needed.

---

## 4. Data Structures

### 4.1 Scheduler Queues (Ibrahim's System)

| Queue | Type | Priority | Purpose |
|-------|------|----------|---------|
| `highQueue` | `Queue<Report>` | 1st (highest) | Forward/backward inference results |
| `lowQueue` | `Queue<Request>` | 2nd | Backward chaining requests |
| `actQueue` | `Stack<ActNode>` | 3rd (lowest) | Act execution (LIFO order) |

### 4.2 Why NOT a 4th Queue?

The supervisor suggested adding a 4th queue for HTN-planned actions. We chose **not to** because:

1. **The existing act stack already works.** `HTNBridge` pushes planned `ActNode`s directly onto `Scheduler.actQueue`. The Scheduler pops them and calls `processIntends()` — same mechanism, no new queue needed.

2. **Adding a 4th queue would break Scheduler invariants.** The `schedule()` method has a precise priority loop (`main: while` with `continue main`). Adding a new queue means modifying this loop, which risks breaking the existing report/request priority logic.

3. **Planned acts ARE regular acts.** From the Scheduler's perspective, an HTN-planned `ActNode` is identical to any other `ActNode`. It has a name, an agenda, and `processIntends()`. The Scheduler doesn't need to know it came from a planner.

4. **Separation of concerns.** The planner's job is to PRODUCE a plan. The Scheduler's job is to EXECUTE acts. The bridge translates between the two. No new queue needed.

### 4.3 HTN Data Structures

| Class | Purpose | Maps To (Ibrahim) |
|-------|---------|-----------------|
| `Task` | Unit of work (primitive/compound) | ActNode concept |
| `Operator` | STRIPS action (preconditions + add/delete lists) | Primitive ActNode |
| `Method` | Decomposition rule (compound → subtasks) | Plan-Act transformer |
| `WorldState` | Set of ground propositions | Beliefs in semantic network |
| `HTNDomain` | Registry of operators + methods | Network knowledge base |
| `PlanResult` | Plan wrapper (success/failure) | No equivalent (NEW) |

---

## 5. Why the Modular Approach?

### 5.1 Why Not Modify ActNode to Handle Operators?

We could have added STRIPS preconditions, add-lists, and delete-lists directly to `ActNode`. We deliberately did NOT because:

| Approach | Pros | Cons |
|----------|------|------|
| **Modify ActNode** | Single class, less code | Pollutes semantic network node with planning logic. Breaks existing tests. Mixes planning-time and execution-time concerns. |
| **Separate Operator class** ✅ | Clean separation. Existing code untouched. Clear "before vs after" for thesis. | Slight duplication of concept. |

**The key insight:** `ActNode` is a node in the **semantic network** — it has cables, channels, inference, and report-processing. `Operator` is a **planning-time abstraction** — it exists only during the DFS search as a lightweight precondition/effect container. Merging them would force the semantic network to carry STRIPS logic it doesn't need.

### 5.2 Why Not Modify Scheduler?

Same principle. The Scheduler's 3-queue loop is Ibrahim's core contribution. Modifying it risks breaking the inference engine. Instead, `HTNBridge` feeds INTO the existing Scheduler.

### 5.3 Why a Separate `htn` Package?

1. **Zero risk to existing code** — the `htn` package can be deleted and the system reverts to Ibrahim's original
2. **Clear thesis narrative** — "Before" (Ibrahim) vs "After" (Ibrahim + HTN) with concrete code snapshots
3. **Testable in isolation** — `HTNPlannerTest` validates the planner without starting the full MindGRAF system
4. **Single integration point** — only `HTNBridge` imports from `nodes` and `mgip`

---

## 6. File Map

### 6.1 Connection Diagram

```
┌───────────────────────────────────────────────┐
│ HTN Package (src/htn/) — NEW                  │
│                                               │
│    ┌──────────────┐     ┌───────────────┐     │
│    │  Task.java   │     │ HTNBridge.java│     │
│    └──────▲───────┘     └───────┬───────┘     │
│           │                     │             │
│    ┌──────┴───────┐     ┌───────▼───────┐     │
│    │HTNPlanner.java│◄───┤PlanResult.java│     │
│    └──────┬───────┘     └───────────────┘     │
│           │                                   │
│    ┌──────┴───────┐     ┌───────────────┐     │
│    │WorldState.java│    │ HTNDomain.java│     │
│    └──────────────┘     └───────────────┘     │
│                                               │
│    ┌──────────────┐     ┌───────────────┐     │
│    │Operator.java │     │  Method.java  │     │
│    └──────────────┘     └───────────────┘     │
└─────────────────────────┬─────────────────────┘
                          │ (imports)
                          ▼
┌───────────────────────────────────────────────┐
│ Ibrahim's System — EXISTING                     │
│                                               │
│    ┌──────────────┐     ┌───────────────┐     │
│    │ ActNode.java │◄────┤Scheduler.java │     │
│    └──────┬───────┘     └───────────────┘     │
│           │                                   │
│    ┌──────▼───────┐     ┌───────────────┐     │
│    │  Node.java   │     │PropositionNode│     │
│    └──────────────┘     └───────────────┘     │
└───────────────────────────────────────────────┘
```

**`HTNBridge.java` is the ONLY file that crosses the boundary.** It imports from both `htn` and `nodes`/`mgip`.

### 6.2 All Files

| File | Package | Role | Imports From |
|------|---------|------|-------------|
| `Task.java` | htn | Primitive/compound task | — |
| `Operator.java` | htn | STRIPS action | htn (WorldState) |
| `Method.java` | htn | Decomposition rule | htn (Task, WorldState) |
| `WorldState.java` | htn | Proposition set | htn (Operator) |
| `HTNDomain.java` | htn | Knowledge base | htn (Operator, Method) |
| `PlanResult.java` | htn | Plan wrapper | htn (Operator) |
| `HTNPlanner.java` | htn | SHOP algorithm | htn (all above) |
| **`HTNBridge.java`** | **htn** | **Integration point** | **htn + nodes + mgip** |
| `HTNPlannerTest.java` | htn | Standalone test | htn only |
| `HTNIntegrationTest.java` | htn | Full pipeline test | htn + mgip |
| `ActNode.java` | nodes | Action node | nodes, mgip, cables |
| `Scheduler.java` | mgip | 3-queue scheduler | mgip, nodes |

---

## 7. Transportation Domain — Full Trace

### 7.1 Setup

- Location: A, Destination: B
- Cash: 5 (fare costs 10) — **insufficient**
- Visa card: yes, balance: 0 — **insufficient**
- Bus ticket: yes
- Taxi: available

### 7.2 Domain Definition

**Compound tasks and methods:**
```
travel(A,B):
  Method 1: travel-by-taxi  pre:{taxiAvailable}  → [getTaxi, rideTaxi, pay]
  Method 2: travel-by-bus   pre:{busTicket}       → [walkToStop, takeBus, walkToDestination]

getTaxi:
  Method 1: get-taxi-call   pre:{taxiAvailable}   → [callTaxi]
  Method 2: get-taxi-hail   pre:{onStreet}         → [hailTaxi]

pay:
  Method 1: pay-cash        pre:{hasCash(sufficient)} → [payCash]
  Method 2: pay-visa        pre:{hasVisa}              → [payVisa]
```

**Operators (STRIPS):**

| Operator | Preconditions | Add List | Delete List |
|----------|--------------|----------|-------------|
| callTaxi | {taxiAvailable} | {taxiCalled} | {} |
| hailTaxi | {onStreet} | {taxiCalled} | {} |
| rideTaxi | {taxiCalled} | {atDestination} | {at(A), taxiCalled} |
| payCash | {hasCash(sufficient)} | {paid} | {hasCash(sufficient)} |
| payVisa | {hasVisa, visaBalance(sufficient)} | {paid} | {visaBalance(sufficient)} |
| walkToStop | {} | {atBusStop} | {at(A)} |
| takeBus | {busTicket, atBusStop} | {atDestination} | {atBusStop} |
| walkToDestination | {atDestination} | {at(B)} | {atDestination} |

### 7.3 Step-by-Step Execution Trace

```
INITIAL STATE: {at(A), hasVisa, taxiAvailable, busTicket}

seekPlan(S, [travel], [])
│
│ travel is COMPOUND → 2 methods
│
├─ TRY method 1/2: travel-by-taxi
│  pre {taxiAvailable} → ✅
│  Expand: [getTaxi, rideTaxi, pay]
│
│  seekPlan(S, [getTaxi, rideTaxi, pay], [])
│  ├─ getTaxi is COMPOUND → 2 methods
│  ├─ TRY: get-taxi-call, pre {taxiAvailable} → ✅
│  │  Expand: [callTaxi, rideTaxi, pay]
│  │
│  │  callTaxi PRIMITIVE: pre {taxiAvailable} → ✅
│  │  SIMULATE: S' = S + {taxiCalled}
│  │  plan = [callTaxi]
│  │
│  │  rideTaxi PRIMITIVE: pre {taxiCalled} → ✅
│  │  SIMULATE: S'' = S' + {atDestination} - {at(A), taxiCalled}
│  │  plan = [callTaxi, rideTaxi]
│  │
│  │  pay is COMPOUND → 2 methods
│  │  ├─ TRY: pay-cash, pre {hasCash(sufficient)} → ❌ SKIP
│  │  ├─ TRY: pay-visa, pre {hasVisa} → ✅
│  │  │  Expand: [payVisa]
│  │  │  payVisa PRIMITIVE: pre {hasVisa, visaBalance(sufficient)}
│  │  │  visaBalance(sufficient) ❌ → return FAIL
│  │  ├─ ALL pay methods exhausted → FAIL          ← BACKTRACK 1
│  │
│  │  ← failure propagates up through rideTaxi, callTaxi
│  ├─ get-taxi-call FAILED
│  ├─ TRY: get-taxi-hail, pre {onStreet} → ❌ SKIP
│  ├─ ALL getTaxi methods exhausted → FAIL          ← BACKTRACK 2
│
├─ travel-by-taxi FAILED
│
├─ TRY method 2/2: travel-by-bus                    ← BACKTRACK 3
│  pre {busTicket} → ✅
│  Expand: [walkToStop, takeBus, walkToDestination]
│
│  STATE IS ORIGINAL: {at(A), hasVisa, taxiAvailable, busTicket}
│  (taxi state changes were COPIES — discarded on failure)
│
│  walkToStop: pre {} → ✅ (always applicable)
│  SIMULATE: S' = S + {atBusStop} - {at(A)}
│  plan = [walkToStop]
│
│  takeBus: pre {busTicket, atBusStop} → ✅
│  SIMULATE: S'' = S' + {atDestination} - {atBusStop}
│  plan = [walkToStop, takeBus]
│
│  walkToDestination: pre {atDestination} → ✅
│  SIMULATE: S''' = S'' + {at(B)} - {atDestination}
│  plan = [walkToStop, takeBus, walkToDestination]
│
│  Task list EMPTY → return SUCCESS
│
└─ FINAL PLAN: [walkToStop, takeBus, walkToDestination]

→ HTNBridge wraps as ActNodes → Scheduler executes: A A A
```

### 7.4 Decomposition Tree

```
               travel(A,B) [COMPOUND]
               /                   \
              /                     \
      travel-by-taxi ❌       travel-by-bus ✅
      /     |      \          /     |       \
getTaxi  rideTaxi  pay   walkToStop takeBus walkToDestination
           (✅)   (❌)     (✅)      (✅)         (✅)
  /   \           /   \
call  hail    cash   visa
(✅)  (skip) (skip)  (fail)
```

---

## 8. Integration Pipeline

### 8.1 How HTNBridge Connects the Systems

```
 ┌────┐           ┌─────────┐         ┌──────────┐        ┌─────────┐
 │User│           │HTNBridge│         │HTNPlanner│        │Scheduler│
 └─┬──┘           └────┬────┘         └────┬─────┘        └────┬────┘
   │                   │                   │                   │
   │ planAndExecute()  │                   │                   │
   ├──────────────────►│ plan()            │                   │
   │                   ├──────────────────►│                   │
   │                   │                   │                   │
   │                   │                   │ [DFS + Backtracking]
   │                   │                   │ [taxi fails → bus succeeds]
   │                   │                   │                   │
   │                   │   PlanResult      │                   │
   │                   │◄──────────────────┤                   │
   │                   │                   │                   │
   │                   │                   │                   │
   │                   │ (For each operator, reverse order)    │
   │                   │ new ActNode(primitive=true)           │
   │                   ├───────────────────────────────────────►
   │                   │ addToActQueue()                       │
   │                   ├───────────────────────────────────────►
   │                   │                   │                   │
   │                   │                   │                   │
   │                   │                   │ schedule() loop starts
   │                   │                   │                   │
   │                   │                   │ pop() → processIntends()
   │                   │                   │ → agenda=EXECUTE
   │                   │                   │ → run actuator
 ┌─┴──┐           ┌────┴────┐         ┌────┴─────┐        ┌────┴────┐
 │User│           │HTNBridge│         │HTNPlanner│        │Scheduler│
 └────┘           └─────────┘         └──────────┘        └─────────┘
```

### 8.2 Why Reverse Order?

The Scheduler's act queue is a **Stack** (LIFO). If we push `[walkToStop, takeBus, walkToDest]` in order, `walkToDest` would be on top and execute first — wrong!

So `HTNBridge` pushes in **reverse**: `walkToDest` first (bottom), then `takeBus`, then `walkToStop` (top). The Stack pops `walkToStop` first — correct order.

---

## 9. Build & Run

```bash
# From MindGRAF1.0 directory:

# Compile everything
javac -d bin -sourcepath src src/htn/HTNIntegrationTest.java

# Run full pipeline test (Planner → Bridge → Scheduler)
java -cp bin htn.HTNIntegrationTest

# Run standalone planner test (3 scenarios)
java -cp bin htn.HTNPlannerTest
```

---

## 10. Summary Table

| Aspect | Before (Ibrahim) | After (Ibrahim + HTN) |
|--------|---------------|-------------------|
| **Planning** | None — pick first plan found | DFS search over all decompositions |
| **Failure handling** | Stuck | Backtracking to alternatives |
| **Precondition check** | One at a time via reports | All at once via WorldState |
| **Plan discovery** | Plan-Act transformer + DoOneNode | HTNDomain with all methods upfront |
| **Execution** | Interleaved with discovery | Separated — plan FIRST, execute AFTER |
| **State during planning** | Real semantic network | Simulated WorldState copies |
| **Algorithm** | None | SHOP (Total-Order Forward Decomposition) |
| **Search** | None | DFS with backtracking |
| **Data structures** | 3 queues (high, low, act stack) | Same 3 queues + WorldState for simulation |
| **New queue needed?** | — | No — planned acts use existing act stack |
