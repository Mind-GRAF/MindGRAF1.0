# Deep HTN-MindGRAF Integration Architecture (PROPOSED — NOT ADOPTED)

> ⚠️ **STATUS — read this first.** This document describes a *proposed* integration
> design — dual-primitiveness (`htnPrimitive`/`mindgrafPrimitive`), three HTN queues
> (`htnPlanningQueue`/`htnBacktrackingQueue`/`htnExecutionQueue`), and the methods
> `performHTNPlanning()`/`executeHTNOperator()`/`performHTNBacktracking()`. **This
> design was NOT adopted; none of it is in the code.** The runtime instead uses a
> native mechanism: `isControlAct()` routing at the `EXECUTE` stage, a single
> `executionQueue` (drained last), and a choice-point stack with a per-node `DoOne`
> retry agenda. The `edu.guc.mind_graf.htn` package is a verified **standalone** SHOP
> planner that is **not wired into the runtime** (wiring it in is future work). For
> the changes actually made to the acting system, see
> `docs/MARWA_HTN_INTEGRATION_CHANGES.md`. The text below is kept only as a record of
> the originally-proposed approach.

---

## Problem Statement

The original HTN integration treated the planner as an **external module** (via HTNBridge), leading to three critical issues:

1. **Primitiveness Definition Mismatch**: 
   - HTN domain defines primitiveness statically (operator = primitive task)
   - MindGRAF defines primitiveness dynamically (task with actuator = primitive)
   - A task might be HTN-primitive but MindGRAF can't execute it → system fails

2. **Backtracking Context Loss**:
   - HTN planner supports backtracking via DFS
   - When backtracking occurs, system loses MindGRAF context (acting state machine)
   - Scheduler has no recovery mechanism for failed plans

3. **Temporal Coupling**:
   - Old: Plan → Execute (sequential, separate modules)
   - New: Plan & Execute must be interleaved within acting system lifecycle
   - World state changes during acting must influence planning decisions

---

## Solution: Embedded HTN Planning with Dual-Primitiveness

### 1. Queue Hierarchy (Scheduler.java)

```
Priority 1 (Highest)  → HIGH REPORT QUEUE    (incoming sensor data)
Priority 2            → LOW REQUEST QUEUE    (queries, inferences)
Priority 3 (NEW)      → HTN PLANNING QUEUE   (task decomposition)
Priority 4 (NEW)      → HTN BACKTRACK QUEUE  (failed plan recovery)
Priority 5 (NEW)      → HTN EXECUTION QUEUE  (operator execution)
Priority 6            → HIGH ACT QUEUE       (explicit high-priority acts)
Priority 7 (Lowest)   → REGULAR ACT QUEUE    (background acts)
```

**Rationale**: 
- Reports & requests have highest priority (external stimuli)
- HTN planning phases are interleaved with reporting/requesting (enables dynamic replanning)
- Backtracking has higher priority than regular execution (error recovery)

### 2. Dual-Primitiveness in ActNode

#### Field Definitions

```java
private boolean htnPrimitive;           // Set at domain/task creation time
private boolean mindgrafPrimitive;      // Determined at EXECUTE stage (actuator check)
```

#### Decision Logic in EXECUTE Stage

```
IF mindgrafPrimitive == TRUE
  └─ Execute directly (call actuator)
     agenda → DONE

ELSE IF htnPrimitive == FALSE
  └─ Task is compound (needs decomposition)
     Send to HTN_PLANNING_QUEUE
     HTN planner decomposes into operators
     
ELSE (htnPrimitive == TRUE AND mindgrafPrimitive == FALSE)
  └─ Mismatch: HTN says primitive but no MindGRAF actuator
     Record failure reason
     Send to HTN_BACKTRACKING_QUEUE
     HTN planner tries alternative method
```

#### Example: Transportation Domain

```
Task: travel(A, B)
  htnPrimitive = false          ← HTN domain says: compound (has methods)
  mindgrafPrimitive = false     ← MindGRAF says: no direct actuator
  → Send to HTN_PLANNING_QUEUE
  → Decompose into: [getTaxi, rideTaxi, payDriver]

Task: payDriver
  htnPrimitive = false          ← Has methods: payVisa, payCash
  mindgrafPrimitive = true      ← MindGRAF has actuator
  → If method succeeds: Execute directly
  → If method fails: Backtrack to try alternative method
```

### 3. Backtracking State Management

#### Per-ActNode Backtracking Context

```java
private int planAttemptNumber;              // Which alternative are we on?
private ArrayList<Object> failureHistory;   // Why each attempt failed
private Object worldStateSnapshot;          // Frozen state for restoration
```

#### Backtracking Flow

```
1. HTN planner attempts Method #1 (payVisa)
   - Creates state snapshot (immutable copy)
   - Attempts decomposition

2. Method #1 fails (visa balance insufficient)
   - recordFailure("visa balance = 0")
   - Restore snapshot (world state reset)
   - Send to HTN_BACKTRACKING_QUEUE

3. Scheduler processes backtracking
   - incrementPlanAttempt() → now on Method #2
   - HTN planner retries with alternative (payCash)
   - Uses captured snapshot for clean retry

4. Method #2 succeeds
   - Operators queued to HTN_EXECUTION_QUEUE
   - Plan commits to world state
```

### 4. Queue Processing Sequences

#### Sequence 1: Normal Execution Path

```
schedule() {
  process highQueue              // Reports arrive
  process lowQueue               // Requests answered
  process htnPlanningQueue       // Compound tasks decomposed → operators queued
  process htnExecutionQueue      // Operators executed one-by-one
  process highActQueue           // High-priority acts processed
  process actQueue               // Background acts processed
}
```

#### Sequence 2: Failure & Backtracking Path

```
schedule() {
  process highQueue              
  process lowQueue               
  process htnPlanningQueue       // Method #1 fails
    → recordFailure()
    → add to htnBacktrackingQueue
    
  process htnBacktrackingQueue   // Retry with Method #2
    → restoreSnapshot()
    → call htnPlanner.seekPlan(currentMethod=2)
    → If success: add operators to htnExecutionQueue
    → If failure: recurse to htnBacktrackingQueue again
    
  process htnExecutionQueue      
  process highActQueue           
  process actQueue               
}
```

#### Sequence 3: Dynamic Replanning (Mid-Execution)

```
schedule() {
  process highQueue
    → Incoming sensor report: "taxi unavailable"
    → Updates world state
    → Scheduler continues: continue main loop
    
  process lowQueue               
  process htnPlanningQueue       
    → HTN planner sees new state
    → payVisa method now fails (wasn't even tried before)
    → Triggers backtrack → payCash method succeeds
    
  process htnExecutionQueue      
  process highActQueue           
  process actQueue               
}
```

---

## Implementation: Three New Methods in ActNode

### 1. performHTNPlanning()
**Called from**: HTN_PLANNING_QUEUE  
**When**: Compound task reaches EXECUTE stage with htnPrimitive=false

```java
public void performHTNPlanning() {
  // TODO: Embedded implementation (NOT external bridge)
  
  1. Create HTN Task from this ActNode's properties
  2. Retrieve HTNDomain (operators, methods)
  3. Capture current WorldState as initial state
  4. Call HTNPlanner.seekPlan(state, [task], domain, [])
  
  5. CASE: Plan successful
     - for each operator in plan:
       - Create ActNode wrapper for operator
       - Add to htnExecutionQueue
     - Set agenda → DONE
     
  6. CASE: Plan fails
     - captureWorldStateSnapshot()
     - recordFailure("No methods succeeded")
     - Send to htnBacktrackingQueue
     - Keep attempt #0 context for backtracking
}
```

### 2. performHTNBacktracking()
**Called from**: HTN_BACKTRACKING_QUEUE  
**When**: Previous plan failed, need to try alternative

```java
public void performHTNBacktracking() {
  // TODO: Implements exponential search space exploration
  
  1. incrementPlanAttempt()
  2. Restore world state from snapshot
  
  3. Call HTNPlanner.seekPlan(
       state=snapshot,
       [task],
       domain,
       excludedMethods=failureHistory  ← Skip previously-failed methods
     )
  
  4. CASE: Alternative plan found
     - Add operators to htnExecutionQueue
     - Clear failureHistory (new attempt chain starts)
     - Set agenda → DONE
     
  5. CASE: All methods exhausted
     - throw NoPlansExistForTheActException
     
  6. CASE: Alternative also fails
     - recordFailure(reason)
     - Keep in htnBacktrackingQueue
     - Scheduler retries in next cycle
}
```

### 3. executeHTNOperator()
**Called from**: HTN_EXECUTION_QUEUE  
**When**: Single operator from successful plan is ready to execute

```java
public void executeHTNOperator() {
  // TODO: Executes STRIPS operator with MindGRAF context
  
  1. Get operator from this ActNode (wrapped operator)
  2. Verify preconditions against current world state
  
  3. CASE: Preconditions not met
     - Log failure
     - Trigger backtracking (entire plan may be wrong)
     - Send parent act node to htnBacktrackingQueue
     
  4. CASE: Preconditions satisfied
     - Execute operator action
     - Apply effects to world state (add/delete)
     - Report success via Scheduler
     
  5. CASE: Execution error
     - Catch exception
     - Record specific failure reason
     - Trigger backtracking
     
  6. Set agenda → DONE
}
```

---

## Integration Points with Existing Systems

### 1. HTNBridge (External Module) → Embedded Planning

**Old Architecture**:
```
ActNode (EXECUTE)
  → HTNBridge.executePlan()
  → HTNPlanner.seekPlan()
  → Back to Scheduler.addToActQueue()  [Lost context!]
```

**New Architecture**:
```
ActNode (EXECUTE stage)
  → agenda = FIND_PLANS
  → Scheduler.addToHTNPlanningQueue(this)
  → Later: Scheduler invokes performHTNPlanning()
  → HTN planner integrated INSIDE ActNode lifecycle
```

### 2. Scheduler Exception Handling

**Old**: Exception bubbles to perform() caller  
**New**: Exceptions trigger HTN_BACKTRACKING_QUEUE automatically

```java
try {
  toRunNext.performHTNPlanning();
} catch (NoPlansExistForTheActException e) {
  // Instead of throwing: attempt backtracking
  if (toRunNext.getPlanAttemptNumber() < MAX_ATTEMPTS) {
    Scheduler.addToHTNBacktrackingQueue(toRunNext);
  } else {
    throw e;  // Only give up after exhausting backtracking
  }
}
```

### 3. World State Integration

**Scheduler's originOfBackInf** (currently unused) becomes HTN world state source:

```java
// In performHTNPlanning()
WorldState initialState = buildWorldStateFromNetwork(
  Scheduler.getOriginOfBackInf(),
  Network.getPropositionNodes()
);
```

---

## Testing Strategy

### Test 1: Simple Primitive Task
```
Task: walk(location)
  htnPrimitive = true
  mindgrafPrimitive = true
  → Direct execution (no planning needed)
```

### Test 2: Compound Task with Single Method
```
Task: travel(A, B)
  htnPrimitive = false
  Methods: [taxi, bus]
  → Send to HTN_PLANNING_QUEUE
  → taxi method succeeds
  → Operators queued: [hail, ride, pay]
```

### Test 3: Backtracking on Method Failure
```
Task: travel(A, B)
  worldState: cash=5, taxi_fare=10
  
  Try taxi: payDriver fails (insufficient cash)
    → backtrack
  Try bus: payDriver succeeds
    → operators queued, execute
```

### Test 4: Dynamic Replanning Mid-Execution
```
Initially: payDriver → taxi method queued
  During execution: "taxi unavailable" report
  → triggers replan in next cycle
  → bus method now selected automatically
```

---

## Implementation Checklist

- [x] Add HTN-specific queues to Scheduler
- [x] Implement dual-primitiveness flags in ActNode
- [x] Modify EXECUTE stage to check both primitiveness types
- [x] Add backtracking state management (attempt #, failure history, snapshots)
- [ ] Implement performHTNPlanning() (calls embedded HTNPlanner.seekPlan())
- [ ] Implement performHTNBacktracking() (restores snapshot, retries with excludedMethods)
- [ ] Implement executeHTNOperator() (precondition check, effects application)
- [ ] Update HTNPlanner to accept excludedMethods parameter
- [ ] Create WorldState builder from current network state
- [ ] Add exception handlers for backtracking triggers
- [ ] Test suite for all 4 scenarios above

---

## Future Enhancements

1. **Lookahead Planning**: Before executing operator, verify next operator's preconditions against predicted effects
2. **Constraint-Based Backtracking**: Prune search space based on accumulated constraints (e.g., "never use taxi again in this context")
3. **Anytime Planning**: Return partial plans while continuing to search for better alternatives in background
4. **Plan Caching**: Remember successful decompositions to skip replanning for similar contexts

---

## References

- **SHOP Paper** (`src/main/java/edu/guc/mind_graf/htn/SHOP.pdf`): Total-order forward search with HTN
- **Ibrahim's Acting System** (`README.md`): Agenda-driven execution with perception-action cycle
- **Dual-Mode Task Semantics**: Primitiveness is contextual (domain vs. capability)
