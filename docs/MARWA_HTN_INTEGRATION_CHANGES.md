MARWA HTN Integration Changes

Author: Hatem Soliman (marked edits)
Date: 2026-05-19

This document summarizes the concrete changes made to Marwa-side code to implement deterministic planning, sibling-preserving backtracking, and a separate execution queue. For each affected file it shows a concise "Before" (original behavior) and "After" (current behavior) code fragment and a short English explanation and migration TODOs.

---

**File:** src/main/java/edu/guc/mind_graf/mgip/Scheduler.java

Before (original - key behavior):
- No dedicated `executionQueue` existed; primitives executed inline during `ActNode` execution.
- No Marwa-side `planChoicePoints` stack; sibling alternatives could be lost.

After (current - key behavior):
- Added `Queue<ActNode> executionQueue` to hold fully-validated primitive acts for later execution.
- Added `PlanChoicePoint` records and `Stack<PlanChoicePoint> planChoicePoints` to save sibling alternatives and the act-queue depth at choice points.
- Implemented `pushPlanChoicePoint(...)`, `backtrackToNextAlternative()`, and `trimActQueueTo(int)` to restore parent choices and remove tail acts when backtracking.

Why:
- Separating planning from execution avoids actuator side-effects during partial planning and makes it safe to backtrack.
- Choice points preserve siblings so deeper failures do not lose alternative plans.

TODO / Migration:
- Migrate choice-point stack into standalone HTN planner, exposing an API for Marwa-side scheduling.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/ActNode.java

Before (original - key behavior):
- In the `EXECUTE` agenda, primitive acts called `runActuator()` immediately.
- Decomposition and execution were interleaved.

After (current - key behavior):
- In the `EXECUTE` agenda, non-primitives continue to find plans; when an act is a primitive, the node is now added to `Scheduler.executionQueue` instead of executing immediately.
- Primitives are marked `DONE` after being queued for execution.

Why:
- Ensures the plan for the whole act-stack is validated before any actuator side-effects occur, supporting safe backtracking.

TODO / Migration:
- Move this deferral boundary into the standalone HTN package when refactoring to keep runtime planner logic centralized.

---

**File:** src/main/java/edu/guc/mind_graf/set/NodeSet.java

Before (original - key behavior):
- Internal storage used an unordered `HashMap`, making plan selection non-deterministic.

After (current - key behavior):
- Internal storage now uses `LinkedHashMap` to preserve insertion order.
- `getNode(int)` and iteration reflect the insertion order deterministically.

Why:
- Deterministic plan ordering is required for predictable DoOne/DoAll scheduling and repeatable backtracking.

TODO / Migration:
- If planner semantics require a canonical ordering independent of insertion, replace with a sorted collection and document ordering criteria.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/DoOneNode.java

Before (original - key behavior):
- The node collapsed alternatives (possibly randomly) and scheduled one plan, discarding others.

After (current - key behavior):
- The node pushes the remaining alternatives as a `PlanChoicePoint` onto `Scheduler.planChoicePoints` and schedules the first alternative deterministically (in insertion order).

Why:
- Preserves siblings for later backtracking so that a failed deeper branch can return to try the next sibling.

TODO / Migration:
- Replace the local `PlanChoicePoint` handling with calls into the HTN planner's choice-point API when available.

---

**File:** src/main/java/edu/guc/mind_graf/nodes/DoAllNode.java

Before (original - key behavior):
- The `DoAll` expansion might have randomized the execution order of subacts.

After (current - key behavior):
- `DoAll` now iterates deterministically over `NodeSet` in insertion order and schedules each sub-act sequentially.

Why:
- Predictable traces and simpler reasoning for backtracking and test reproducibility.

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

If you want, I can now run the full test-suite and fix any remaining failures.

---

Summary status

- Deterministic sibling-preserving backtracking: implemented (choice-points + ordered NodeSet).
- Execution queue separation: implemented (primitives queued for execution after planning).
- Migration notes / TODOs: added inline and in this doc where appropriate.
- World-state rollback: not needed for the current planning-only flow.

If you want, I will:
- (A) Add more explicit "ADDED BY" header comments to any additional files you point out.
- (B) Run the full test-suite and report failures.

World-state snapshot/restore is intentionally not on the plan because the current design backtracks only over planned structure, not execution-time world mutation.

Which of (A)/(B) should I do next? (I can start with B to re-run tests.)
