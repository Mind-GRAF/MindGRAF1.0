MARWA HTN Integration Changes

Author: Hatem Soliman (marked edits)
Date: 2026-05-19 (initial), 2026-05-28 (Dr.'s feedback revisions)

This document summarizes the concrete changes made to Marwa-side code to implement deterministic planning, sibling-preserving backtracking, and a separate execution queue. For each affected file it shows a concise "Before" (original behavior) and "After" (current behavior) code fragment and a short English explanation and migration TODOs.

---

**File:** src/main/java/edu/guc/mind_graf/mgip/Scheduler.java

**Before (original - key behavior):**
```java
// BEFORE: No executionQueue, no choice-point stack
private static Queue<Report> highQueue;
private static Queue<Request> lowQueue;
private static Stack<ActNode> actQueue;
private static Stack<ActNode> highActQueue;
// Planning and execution interleaved; alternatives could be lost when plan branches fail
```

**After (current - key behavior):**
```java
// AFTER: Added execution queue and choice-point tracking
private static Queue<Report> highQueue;
private static Queue<Request> lowQueue;
private static Stack<ActNode> actQueue;
private static Stack<ActNode> highActQueue;
private static Deque<ActNode> executionQueue;  // NEW
private static Stack<PlanChoicePoint> planChoicePoints;  // NEW

private static final class PlanChoicePoint {
    // RENAMED on 2026-05-28 (formerly: actStackSize, executionQueueSize)
    private final int actQueueDepthAtSnapshot;
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
}

// Backtracking now trims both act and execution queue tails:
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
```

**Why:**
- Separating planning from execution avoids actuator side-effects during partial planning and makes it safe to backtrack.
- Choice points preserve siblings so deeper failures do not lose alternative plans.
- Execution queue depth tracking ensures primitives queued from failing branches are also removed.

**TODO / Migration:**
- Migrate choice-point stack into standalone HTN planner, exposing an API for Marwa-side scheduling.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/ActNode.java

**Before (original - key behavior):**
```java
case EXECUTE:
    System.out.println("In execute case");
    if (isPrimitive()) {
        runActuator();  // IMMEDIATE execution - unsafe if later decomposition fails
    } else {
        processIntends();
    }
    break;
```

**After (current - key behavior):**
```java
case EXECUTE:
    System.out.println("In execute case");
    if (!isPrimitive) {
        // NEW: keep decomposition and final execution separate.
        // TODO(HTN migration): this is the boundary that can later move into the
        // standalone HTN package without changing the marwa-side act model.
        this.agenda = ActAgenda.FIND_PLANS;
        Scheduler.addToActQueue(this);
        sendRequest(false);
    } else {
        // NEW: fully validated primitive acts do not execute immediately.
        // They are deferred to the dedicated execution queue so that planning
        // stays complete before any actuator side effects happen.
        this.agenda = ActAgenda.DONE;
        Scheduler.addToExecutionQueue(this);  // DEFERRED to execution stage
    }
    break;
```

**Why:**
- Ensures the plan for the whole act-stack is validated before any actuator side-effects occur, supporting safe backtracking.
- If a deeper branch fails, primitives from the failing branch are already queued but not yet executed, so they can be removed.

**TODO / Migration:**
- Move this deferral boundary into the standalone HTN package when refactoring to keep runtime planner logic centralized.

---

**File:** src/main/java/edu/guc/mind_graf/set/NodeSet.java

**Before (original - key behavior):**
```java
public class NodeSet implements Iterable<Node> {
    private HashMap<String, Node> nodes;  // UNORDERED - non-deterministic iteration

    public NodeSet() {
        nodes = new HashMap<String, Node>();  // BEFORE: HashMap
    }
```

**After (2026-05-19 → REVERTED on 2026-05-28):**
```java
/**
 * [2026-05-19] Changed HashMap → LinkedHashMap for deterministic ordering.
 * [2026-05-28] REVERTED back to HashMap per Dr.'s feedback:
 *   - Ordering not needed: PlanChoicePoint stores alternatives independently.
 *   - Avoids breaking other team members' code.
 *   - TODO (future): re-introduce LinkedHashMap defensively if needed.
 */
public class NodeSet implements Iterable<Node> {
    private HashMap<String, Node> nodes;  // Back to HashMap

    public NodeSet() {
        nodes = new HashMap<String, Node>();  // REVERTED to HashMap
    }

    public Node getNode(int index) {
        int i = 0;
        for (Node node : this.nodes.values()) {
            if (i == index) {
                return node;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + i);
    }
```

**Why reverted:**
- The Dr. confirmed that ordering in NodeSet is not required for correct backtracking. Plan alternatives are independently stored in `Scheduler.PlanChoicePoint` (as `ArrayList<ActNode>`), which preserves its own insertion order. The already-chosen plan is popped, and any remaining plan from the choice point can be tried regardless of NodeSet iteration order.
- Keeping HashMap avoids risking breakage in other team members' code.

**TODO / Migration:**
- Future work: if deterministic ordering is ever needed at the NodeSet level (e.g., for reproducible traces independent of choice points), consider re-introducing LinkedHashMap defensively, ensuring all constructors consistently wrap in LinkedHashMap.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/DoOneNode.java

**Before (original - key behavior):**
```java
@Override
public void runActuator() {
    NodeSet possibleActs = this.getDownCableSet().get("obj").getNodeSet();
    if (possibleActs.isEmpty()) {
        return;
    }
    // BEFORE: No preservation of siblings; they are lost
    ActNode act = (ActNode) possibleActs.getNode(0);  // Randomly picks or takes first?
    System.out.println(act.getName());
    act.restartAgenda();
    Scheduler.addToActQueue(act);
    // Other alternatives (1, 2, ...) are discarded
}
```

**After (current - key behavior):**
```java
/*
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Deterministic sibling-preserving DoOne: the scheduler saves the
 *   remaining alternatives as a choice point and we schedule the first
 *   alternative deterministically.
 */
@Override
public void runActuator() {
    NodeSet possibleActs = this.getDownCableSet().get("obj").getNodeSet();
    if (possibleActs.isEmpty()) {
        return;
    }

    // NEW: keep every sibling alternative available for later backtracking.
    // The scheduler stores the remaining siblings as a choice point and we take
    // the first branch in deterministic order.
    Scheduler.pushPlanChoicePoint(this, possibleActs, Scheduler.getActQueue().size());

    ActNode act = (ActNode) possibleActs.getNode(0);  // Deterministic: always first
    System.out.println(act.getName());
    act.restartAgenda();
    Scheduler.addToActQueue(act);
    // Siblings (1, 2, ...) are saved in the choice-point stack for later backtracking
}
```

**Why:**
- Preserves siblings for later backtracking so that a failed deeper branch can return to try the next sibling.
- Deterministic selection ensures repeatable execution traces and test results.

**TODO / Migration:**
- Replace the local `PlanChoicePoint` handling with calls into the HTN planner's choice-point API when available.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/DoAllNode.java

**Before (original - key behavior):**
```java
/*
 * BEFORE: No author comment; behavior was implicit and possibly randomized
 */
@Override
public void runActuator() {
    NodeSet acts = this.getDownCableSet().get("obj").getNodeSet();
    for (Node node : acts) {  // Iteration order was non-deterministic (HashMap)
        ActNode nextAct = (ActNode) node;
        System.out.println(nextAct.getName());
        nextAct.restartAgenda();
        Scheduler.addToActQueue(nextAct);
    }
}
```

**After (current - key behavior):**
```java
/*
 * MODIFIED for thesis integration (Author: Hatem Soliman, 2026-05-19)
 * - Deterministic DoAll scheduling: iterate in insertion order and schedule
 *   acts sequentially. This avoids randomization so plan traces are stable.
 */
@Override
public void runActuator() {
    NodeSet acts = this.getDownCableSet().get("obj").getNodeSet();
    for (Node node : acts) {  // Now iterates in insertion order (LinkedHashMap)
        ActNode nextAct = (ActNode) node;
        System.out.println(nextAct.getName());
        nextAct.restartAgenda();
        Scheduler.addToActQueue(nextAct);
    }
}
```

**Why:**
- Predictable traces and simpler reasoning for backtracking and test reproducibility.
- All sub-acts are scheduled in the same order every time, supporting deterministic planning and testing.

**TODO / Migration:**
- Ensure the standalone HTN planner maintains the same deterministic scheduling when migrating this code.

---

Addressing the whiteboard/backtracking behavior

What we implemented:
- Choice points: whenever a node with multiple plan alternatives is expanded (e.g., `DoOne`), the scheduler records a `PlanChoicePoint` which contains:
  - reference to the parent act (control node),
  - the remaining sibling alternatives (a `NodeSet`), and
  - the act-queue depth to which the scheduler must trim when backtracking.
- On failure (for example when `NoPlansExistForTheActException` is thrown deeper in the stack), the scheduler calls `backtrackToNextAlternative()` to:
  - pop the latest `PlanChoicePoint`,
  - trim the pending act queue to the recorded depth (removing tail acts that belong to the failing branch),
  - trim the pending execution queue to the recorded depth (removing queued primitives that belong to the failing branch),
  - schedule the next sibling alternative.

Remaining / optional work:
- World-state snapshot/restore is not required for the current design because planning is kept separate from execution. The implemented backtracking only trims scheduled acts and resumes with siblings; it does not need to undo execution-time side effects during planning.
- Full test-suite run and fixups: run `mvn test` to surface any unrelated failures (there was a prior unrelated test failure in `BridgeRuleTest.applyRuleHandler`); address those separately.

Verification checklist (what to run locally):

- Run targeted tests for modified nodes:

```bash
mvn -Dtest=DoOneNodeTest,DoAllNodeTest test
```

- Run full test-suite and inspect failures:

```bash
mvn test
```

---

## Changes on 2026-05-28 (Dr.'s feedback session)

The following additional changes were made based on Dr.'s review feedback:

### 1. NodeSet reverted from LinkedHashMap → HashMap
- **Reason**: Ordering not needed; PlanChoicePoint stores alternatives independently.
- **Files**: `NodeSet.java`

### 2. PlanChoicePoint fields renamed for clarity
- `actStackSize` → `actQueueDepthAtSnapshot`
- `executionQueueSize` → `executionQueueDepthAtSnapshot`
- **Reason**: Original names were ambiguous. New names clarify they are queue-depth snapshots taken at choice-point creation time, used to trim queues during backtracking.
- **Files**: `Scheduler.java`

### 3. Execution semantics documented for control nodes
- Added extensive comments to `DoAllNode.java`, `DoOneNode.java`, `SNSequenceNode.java` explaining:
  - Control nodes add to `actQueue` (planning), NOT `executionQueue` (real-world)
  - Only leaf primitives reach the executionQueue
  - Why: control nodes' outcomes are known during planning; leaf primitives need real-world execution
- **Files**: `DoAllNode.java`, `DoOneNode.java`, `SNSequenceNode.java`

### 4. Per-node-type semantics documented as design alternative
- Added DESIGN NOTE to `DoOneNode.java` documenting the Dr.'s suggestion that each node type could manage its own agenda cycle (TRYING → RETRYING → DONE) instead of relying on the Scheduler's global backtrackToNextAlternative().
- Left as future work.
- **Files**: `DoOneNode.java`
