# Marwa-side HTN Integration: Changes

**Author:** Hatem Soliman
**Dates:** 2026-05-19 (initial), 2026-05-28 (Dr.'s feedback revisions), 2026-06-06 (this refresh)

This document is the dev-facing summary of every concrete change made to the Marwa-side acting code to add **plan-first execution**, **sibling-preserving backtracking**, and a **dedicated execution stage** to Mind GRAF. For each affected file it shows the relevant "Before" and "After" code shape, why the change exists, and what the next migration step would be.

The big picture: **plan in simulation first, defer real actions until the whole plan is built, and keep failed branches from ever touching the world.** The single switch that makes this work is the new `ActNode.isControlAct()` method (point 1 below); the dedicated `executionQueue` and `planChoicePoints` stack in `Scheduler` are the storage for it (points 2 and 3); `NodeSet` was briefly changed and then reverted (point 4); the five control-act nodes and the integration-test fixes round it out (points 5–7).

---

## 1. `ActNode.java` — the `isControlAct()` switch and a 3-way EXECUTE stage

**This is the central new mechanism.** Every act that reaches the `EXECUTE` stage is now routed by a 3-way decision based on two flags (`isPrimitive`, `isControlAct()`):

| Condition | Meaning | What happens at EXECUTE |
|---|---|---|
| `!isPrimitive` | compound act | transition to `FIND_PLANS`, look up plans, wrap in a `DoOne` |
| `isPrimitive && isControlAct()` | a "thinking" act (DoAll, DoOne, Sequence, Achieve, Attitude) | call `runActuator()` **immediately** — decompose into the planning queue, no real action |
| `isPrimitive && !isControlAct()` | a leaf primitive, or a conditional whose branch depends on the live world (`SNIF`/`SNITERATE`) | **defer** to `Scheduler.executionQueue` — runs only after planning is complete |

### Before
```java
case EXECUTE:
    if (isPrimitive()) {
        runActuator();   // IMMEDIATE — unsafe if a later branch fails
    } else {
        processIntends();
    }
    break;
```

### After
```java
// NEW: returns false by default; the five control-act subclasses override to true.
public boolean isControlAct() {
    return false;
}

case EXECUTE:
    if (!isPrimitive) {
        this.agenda = ActAgenda.FIND_PLANS;
        Scheduler.addToActQueue(this);
        sendRequest(false);
    } else {
        if (this.isControlAct()) {
            // Control acts decompose during planning -- never touch the world.
            this.agenda = ActAgenda.DONE;
            this.runActuator();
        } else {
            // Leaf primitives and SNIF/SNITERATE wait for the execution stage.
            this.agenda = ActAgenda.DONE;
            Scheduler.addToExecutionQueue(this);
        }
    }
    break;
```

### Why
Without this 3-way split, control nodes like `DoAll` would have been queued for "execution" alongside genuine actions, so trimming a failed branch could not distinguish "thinking work that's safe to redo" from "real-world work that's already happened." The flag makes that distinction crisp: only red-coloured (non-control) acts ever sit in the execution queue, and the queue is drained last.

### Migration TODO
Move the deferral decision into the standalone HTN package once it is wired in; the `isControlAct()` flag is then equivalent to "this task corresponds to a method, not an operator."

---

## 2. `Scheduler.java` — the `executionQueue` and `planChoicePoints` stack

### Before
```java
private static Queue<Report>   highQueue;
private static Queue<Request>  lowQueue;
private static Stack<ActNode>  actQueue;
private static Stack<ActNode>  highActQueue;
// No separation between planning and execution; no choice points.
```

### After
```java
private static Queue<Report>            highQueue;
private static Queue<Request>           lowQueue;
private static Stack<ActNode>           actQueue;
private static Stack<ActNode>           highActQueue;
private static Deque<ActNode>           executionQueue;   // NEW: drained LAST
private static Stack<PlanChoicePoint>   planChoicePoints; // NEW: sibling alternatives

private static final class PlanChoicePoint {
    // RENAMED 2026-05-28: clarifies these are queue-depth SNAPSHOTS used for trimming.
    private final int actQueueDepthAtSnapshot;        // was: actStackSize
    private final int executionQueueDepthAtSnapshot;  // was: executionQueueSize
    private final ArrayList<ActNode> remainingAlternatives;
    private final String sourceActName;
    private int nextAlternativeIndex;
}

// Backtracking trims BOTH queues to the saved depths.
private static boolean backtrackToNextAlternative() {
    while (!planChoicePoints.isEmpty()) {
        PlanChoicePoint cp = planChoicePoints.peek();
        if (cp.hasNextAlternative()) {
            trimActQueueTo(cp.actQueueDepthAtSnapshot);
            trimExecutionQueueTo(cp.executionQueueDepthAtSnapshot);
            ActNode next = cp.nextAlternative();
            next.restartAgenda();
            actQueue.push(next);
            return true;
        }
        planChoicePoints.pop();
    }
    return false;
}
```

### Why
- The execution queue is the boundary that makes plan-first execution real.
- Choice points preserve siblings so a failed deep branch doesn't lose its alternatives.
- Trimming the execution queue (not just the act queue) is the safety property: a leaf primitive queued by the failed branch is removed **before** it can run.

### Migration TODO
Extract the choice-point stack and `backtrackToNextAlternative()` into the standalone `htn` package behind a small API; the `Scheduler` then keeps only the act stacks and the execution-queue drain.

---

## 3. `DoOneNode.java` — push a choice point, then pick the first alternative

### Before
```java
@Override
public void runActuator() {
    NodeSet possibleActs = getDownCableSet().get("obj").getNodeSet();
    if (possibleActs.isEmpty()) return;
    ActNode act = (ActNode) possibleActs.getNode(0);  // siblings discarded
    act.restartAgenda();
    Scheduler.addToActQueue(act);
}
```

### After
```java
@Override
public boolean isControlAct() { return true; }   // NEW: decompose during planning

@Override
public void runActuator() {
    NodeSet possibleActs = getDownCableSet().get("obj").getNodeSet();
    if (possibleActs.isEmpty()) return;
    // Save the un-picked siblings for backtracking.
    Scheduler.pushPlanChoicePoint(this, possibleActs, Scheduler.getActQueue().size());
    ActNode act = (ActNode) possibleActs.getNode(0);
    act.restartAgenda();
    Scheduler.addToActQueue(act);
}
```

### Why
- Preserves siblings so a deeper failure can return and try the next plan.
- "First" is whatever the `HashMap`-backed `NodeSet` iterates first — not specified, but always exactly one is picked, and the others are saved. (See point 4.)

### Design note (Dr.'s suggestion, future work)
Instead of the global scheduler handling retries, `DoOneNode` could run its own agenda cycle (`TRYING → RETRYING → DONE`) and pick the next sibling itself. That style gives cleaner per-node semantics but needs more agenda states. `AchieveNode` already follows this pattern.

---

## 4. `NodeSet.java` — reverted to `HashMap`

### Before / After
```java
public class NodeSet implements Iterable<Node> {
    private HashMap<String, Node> nodes;   // both before and after
    public NodeSet() { nodes = new HashMap<>(); }
}
```

Briefly changed to `LinkedHashMap` for "deterministic ordering" on 2026-05-19, then **reverted** on 2026-05-28 after the Dr.'s review.

### Why reverted
- Backtracking does **not** need NodeSet to be ordered. The un-picked plan alternatives are kept independently in the `PlanChoicePoint`'s `ArrayList`, and `SNSequenceNode` encodes its order through numbered relation cables (`obj1`, `obj2`, ...), not through `NodeSet` iteration.
- Reverting avoids any risk of perturbing other team members' code that may rely on standard `HashMap` semantics.

### Migration TODO
If reproducible traces are ever needed at the `NodeSet` level (independent of choice points), re-introduce `LinkedHashMap` defensively, making sure every constructor consistently wraps with it.

---

## 5. The control-act node types — `isControlAct() = true`

The five "thinking" act node types each got a one-line override so they decompose during planning rather than getting deferred:

| File | Behaviour of `runActuator()` (unchanged in spirit) |
|---|---|
| `DoAllNode.java`      | iterate the `obj` NodeSet and push every child onto `actQueue` |
| `DoOneNode.java`      | save a choice point, then push the first sibling onto `actQueue` (see point 3) |
| `SNSequenceNode.java` | read `obj1, obj2, ...` cables in order, push children through a local stack so the first ends up on top of the LIFO `actQueue` |
| `AchieveNode.java`    | runs its own internal sub-agenda (`START → FIND_PLANS → DONE`): if the goal is already a belief it short-circuits ("`tmam ya brens`"), otherwise it requests plans and decomposes them via `sendDoOneToActQueue()` |
| `AttitudeNode.java`   | adds the proposition to the current context under the named attitude (e.g. `beliefs`) |

`SNIFNode` and `SNITERATENode` deliberately **do not** override `isControlAct()`: they remain `false` so they are deferred to the execution queue, where they evaluate their guards against the live world.

---

## 6. NodeSet revert and the subtle "first-alternative" point

Because `NodeSet` is now `HashMap`-backed, `possibleActs.getNode(0)` may return either alternative when a `DoOne` has more than one. This is **by design**: the system guarantees exactly one is picked and the others are saved, but does not commit to which. The integration test reflects this honestly with an XOR assertion (`plateRan ^ garnishRan`) rather than fixing the choice. The thesis discusses the rationale in the Implementation chapter's "Deterministic versus Unordered Node Storage" section.

---

## 7. Test infrastructure — integration test and sample-run suite

### Fixed `HTNIntegrationTest.java`
The original integration tests were committed broken (relation-name typos; asserting on `executionQueue` after `schedule()` returns, which always drains it; molecular-name collisions from raw-`new`'d nodes sharing the default name `M<count>`). Fixes:
- Use the relation names the node code actually reads (`obj1` for sequence, `obj` for DoAll/DoOne).
- Capture `System.out` and assert on actuator log lines (the post-drain `executionQueue` is always empty).
- Force distinct names on raw-`new`'d molecular nodes via reflection.

### New `HTNSampleRunsTest.java`
- `universalSampleRun` — a single comprehensive run covering all eight act-node categories in four phases (nested decomposition with `Achieve`+`Attitude`; backtracking + trim; SNIF/SNITERATE deferral; exhaustion + empty edge cases).
- Six focused scenario tests (ordered sequence, nested decomposition, single- and multi-level backtracking, exhaustion, empty choice).
- All seven pass.

### Suite tally
```
$ mvn test
Tests run: 100, Failures: 1, Errors: 0, Skipped: 0
```
The single failure is the pre-existing, unrelated `BridgeRuleTest.applyRuleHandler` (bridge inference); it predates this work and is untouched.

### Caveat (test-isolation only)
`SNIF`/`SNITERATE` reliably fire their guarded branch in **isolated** runs, but in a shared JVM with many `schedule()` cycles the guard-true report does not always arrive — Mind GRAF inference accumulates global static state that `new Network()` does not fully clear. The sample-run suite therefore asserts the deterministic part for these nodes (deferral + reaching guard evaluation) and demonstrates the full firing only in isolated traces. This is a test-environment quirk, not a production defect.

---

## Verification

```bash
# Targeted: just the per-node and sample-run suites
mvn -Dtest='DoOneNodeTest,DoAllNodeTest,SNSequenceNodeTest,AchieveNodeTest,AttitudeNodeTest,HTNIntegrationTest,HTNSampleRunsTest' test

# Full suite (expect: 100 tests, 1 pre-existing BridgeRule failure)
mvn test
```

---

## Migration summary (where to take this next)

1. **Integrate the standalone SHOP planner** — wire `edu.guc.mind_graf.htn` in as a deliberation service: build a `WorldState` from the live network, call `seekPlan`, hand the resulting plan back to the execution queue.
2. **Modularize the runtime planning into that package** — move `planChoicePoints` and `backtrackToNextAlternative()` out of `Scheduler` and behind a clean interface.
3. **Replanning during execution** — when a deferred primitive's runtime preconditions no longer hold, re-enter planning for the remainder of the agenda instead of failing.
4. **Per-node retry agendas** — promote the Dr.'s suggestion so each control act handles its own retries (the way `AchieveNode` already does).
