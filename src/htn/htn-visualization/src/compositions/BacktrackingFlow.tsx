import React, { useMemo } from "react";
import { useCurrentFrame, interpolate, Easing, AbsoluteFill } from "remotion";
import styles from "./BacktrackingFlow.module.css";

interface ActNode {
  name: string;
  x: number;
  y: number;
  type: "control" | "primitive" | "failed";
}

interface ChoicePoint {
  parentName: string;
  siblings: string[];
  actStackDepth: number;
  executionQueueDepth: number;
  timestamp: number;
}

/**
 * Visualizes the whiteboard scenario:
 * - Act 'a' with plans [p1, p2, p3]
 * - p1 expands to [p4, p5, p6]
 * - p4 expands to [a2, a3, a4]
 * - a3 expands to [p7, p8]
 * - When a deeper branch fails, backtracking removes tail acts and resumes with next sibling
 */
export const BacktrackingFlow: React.FC = () => {
  const frame = useCurrentFrame();

  // Timeline phases (in frames, ~30fps):
  // 0-150: Setup and initial plan decomposition
  // 150-300: Expand p1 choice point
  // 300-450: Expand p4 and schedule a2, a3, a4
  // 450-600: Expand a3, which leads to p7, p8
  // 600-750: Failure at deeper level; show backtracking
  // 750-900: Trim execution queue, restore to choice point
  // 900-1050: Resume with next sibling p5
  // 1050-1200: Continue planning
  // 1200-1800: Summary

  const actQueueDepth = useMemo(
    () => interpolate(frame, [0, 600], [0, 8], { easing: Easing.linear }),
    [frame]
  );

  const executionQueueDepth = useMemo(
    () => interpolate(frame, [150, 450], [0, 5], { easing: Easing.linear }),
    [frame]
  );

  const backtrackProgress = useMemo(
    () => interpolate(frame, [600, 750], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }),
    [frame]
  );

  const resumeProgress = useMemo(
    () => interpolate(frame, [750, 900], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }),
    [frame]
  );

  const choicePointsShown = useMemo(() => {
    if (frame < 150) return [];
    if (frame < 300) return [{ name: "p1", depth: 1 }];
    if (frame < 450) return [{ name: "p1", depth: 1 }, { name: "p4", depth: 2 }];
    if (frame < 600) return [{ name: "p1", depth: 1 }, { name: "p4", depth: 2 }, { name: "a3", depth: 3 }];
    return [{ name: "p1", depth: 1 }, { name: "p4", depth: 2 }, { name: "a3", depth: 3 }];
  }, [frame]);

  const failurePhaseStarted = frame >= 600;
  const backtrackingActive = frame >= 600 && frame < 750;
  const resumingFromSibling = frame >= 750;

  return (
    <AbsoluteFill style={{ backgroundColor: "#0f0f1e", color: "#e0e0ff", fontFamily: "Courier New, monospace" }}>
      <div style={{ padding: "40px", fontSize: "18px" }}>
        {/* Header */}
        <div style={{ marginBottom: "40px", fontSize: "32px", fontWeight: "bold", color: "#ff6b9d" }}>
          HTN Deterministic Planning with Sibling-Preserving Backtracking
        </div>

        {/* Main content grid */}
        <div style={{ display: "flex", gap: "60px" }}>
          {/* Left: Act Queue Stack visualization */}
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#4ecdc4" }}>
              Act Queue (Pending Plans)
            </div>
            <div
              style={{
                border: "2px solid #4ecdc4",
                padding: "20px",
                borderRadius: "8px",
                minHeight: "300px",
                backgroundColor: "rgba(78, 205, 196, 0.05)",
              }}
            >
              {renderActStack(Math.round(actQueueDepth), failurePhaseStarted, backtrackingActive, resumeProgress)}
            </div>

            <div style={{ marginTop: "30px", fontSize: "16px", color: "#95e1d3" }}>
              Depth: {Math.round(actQueueDepth)} | Status:{" "}
              {backtrackingActive ? "TRIMMING" : resumingFromSibling ? "RESUMED" : "PLANNING"}
            </div>
          </div>

          {/* Middle: Execution Queue visualization */}
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#f38181" }}>
              Execution Queue (Queued Primitives)
            </div>
            <div
              style={{
                border: "2px solid #f38181",
                padding: "20px",
                borderRadius: "8px",
                minHeight: "300px",
                backgroundColor: "rgba(243, 129, 129, 0.05)",
              }}
            >
              {renderExecutionStack(Math.round(executionQueueDepth), backtrackingActive, backtrackProgress)}
            </div>

            <div style={{ marginTop: "30px", fontSize: "16px", color: "#ffb3ba" }}>
              Primitives Queued: {Math.round(executionQueueDepth)} | Will Execute After Planning
            </div>
          </div>

          {/* Right: Choice Points Stack */}
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#ffe66d" }}>
              Choice Point Stack
            </div>
            <div
              style={{
                border: "2px solid #ffe66d",
                padding: "20px",
                borderRadius: "8px",
                minHeight: "300px",
                backgroundColor: "rgba(255, 230, 109, 0.05)",
              }}
            >
              {renderChoicePointStack(choicePointsShown, backtrackProgress, resumeProgress)}
            </div>

            <div style={{ marginTop: "30px", fontSize: "16px", color: "#ffd97d" }}>
              Saved Siblings: {choicePointsShown.length} | Status:{" "}
              {backtrackingActive ? "POPPING" : resumingFromSibling ? "NEXT SIBLING" : "ACCUMULATING"}
            </div>
          </div>
        </div>

        {/* Event log at bottom */}
        <div style={{ marginTop: "40px", fontSize: "16px", color: "#95e1d3" }}>
          <div style={{ fontWeight: "bold", marginBottom: "10px" }}>Event Log:</div>
          {renderEventLog(frame, backtrackingActive, resumingFromSibling)}
        </div>
      </div>
    </AbsoluteFill>
  );
};

function renderActStack(
  depth: number,
  failureStarted: boolean,
  backtracking: boolean,
  resumeProgress: number
): React.ReactNode {
  const acts = ["a (root)", "p1", "p4", "a2", "a3", "p7", "p8 (FAIL)"];
  const items = [];

  for (let i = 0; i < Math.min(depth, acts.length); i++) {
    const isFailing = i === acts.length - 1 && failureStarted;
    const isRemoving = backtracking && i >= depth * (1 - resumeProgress);

    items.push(
      <div
        key={i}
        style={{
          padding: "12px",
          marginBottom: "8px",
          backgroundColor: isFailing ? "rgba(255, 107, 107, 0.3)" : isRemoving ? "rgba(255, 180, 0, 0.3)" : "rgba(78, 205, 196, 0.2)",
          border: `2px solid ${isFailing ? "#ff6b6b" : isRemoving ? "#ffa500" : "#4ecdc4"}`,
          borderRadius: "4px",
          textAlign: "center",
          fontWeight: "bold",
          opacity: isRemoving ? 1 - resumeProgress : 1,
          transform: `translateX(${isRemoving ? resumeProgress * 20 : 0}px)`,
          transition: "all 0.05s ease-out",
        }}
      >
        {acts[i]}
      </div>
    );
  }

  return <div>{items.length > 0 ? items : <div style={{ color: "#666", fontStyle: "italic" }}>Empty</div>}</div>;
}

function renderExecutionStack(depth: number, backtracking: boolean, backtrackProgress: number): React.ReactNode {
  const primitives = ["DoOne(p1, p2, p3)", "DoAll(a2, a3, a4)", "primitive_p7", "primitive_p8"];
  const items = [];

  for (let i = 0; i < Math.min(depth, primitives.length); i++) {
    const isRemoving = backtracking;

    items.push(
      <div
        key={i}
        style={{
          padding: "12px",
          marginBottom: "8px",
          backgroundColor: isRemoving && i > depth * (1 - backtrackProgress) ? "rgba(255, 107, 107, 0.3)" : "rgba(243, 129, 129, 0.2)",
          border: `2px solid ${isRemoving && i > depth * (1 - backtrackProgress) ? "#ff6b6b" : "#f38181"}`,
          borderRadius: "4px",
          textAlign: "center",
          fontWeight: "bold",
          opacity: isRemoving && i > depth * (1 - backtrackProgress) ? 1 - backtrackProgress : 1,
          transform: `translateX(${isRemoving && i > depth * (1 - backtrackProgress) ? backtrackProgress * 20 : 0}px)`,
        }}
      >
        {primitives[i]}
      </div>
    );
  }

  return <div>{items.length > 0 ? items : <div style={{ color: "#666", fontStyle: "italic" }}>Empty</div>}</div>;
}

function renderChoicePointStack(
  choicePoints: Array<{ name: string; depth: number }>,
  backtrackProgress: number,
  resumeProgress: number
): React.ReactNode {
  return (
    <div>
      {choicePoints.map((cp, idx) => (
        <div
          key={idx}
          style={{
            padding: "12px",
            marginBottom: "8px",
            backgroundColor: "rgba(255, 230, 109, 0.2)",
            border: `2px solid #ffe66d`,
            borderRadius: "4px",
            fontSize: "14px",
          }}
        >
          <div style={{ fontWeight: "bold", marginBottom: "4px" }}>CP {idx + 1}: {cp.name}</div>
          <div style={{ fontSize: "12px", color: "#ffd97d" }}>
            Siblings: [p{cp.depth + 1}, p{cp.depth + 2}, ...] | Depth: {cp.depth}
          </div>
        </div>
      ))}
      {choicePoints.length === 0 && <div style={{ color: "#666", fontStyle: "italic" }}>No choice points yet</div>}
    </div>
  );
}

function renderEventLog(frame: number, backtracking: boolean, resuming: boolean): React.ReactNode {
  const events = [];

  if (frame >= 0) events.push("✓ Initialize scheduler with empty queues");
  if (frame >= 150) events.push("✓ DoOne: Schedule p1, save siblings [p2, p3] as choice point");
  if (frame >= 300) events.push("✓ p1 expands to [p4, p5, p6], schedule p4");
  if (frame >= 450) events.push("✓ p4 expands to [a2, a3, a4] via DoAll");
  if (frame >= 600) events.push("✓ a3 expands to [p7, p8], primitives queued to execution");
  if (frame >= 600 && frame < 750) {
    events.push("⚠ FAILURE: a deeper branch cannot be satisfied");
    events.push("→ BACKTRACKING: Pop choice point for a3");
  }
  if (frame >= 750) {
    events.push("→ Trim act queue to saved depth (remove p7, p8)");
    events.push("→ Trim execution queue to saved depth");
    events.push("✓ Resume with next sibling from p4 choice point");
  }
  if (frame >= 900) events.push("✓ Continue planning with remaining alternatives");
  if (frame >= 1200) events.push("✓ Planning complete, execute primitives from execution queue");

  return (
    <div>
      {events.slice(-3).map((event, idx) => (
        <div key={idx} style={{ marginBottom: "6px", color: event.startsWith("⚠") ? "#ff6b6b" : "#95e1d3" }}>
          {event}
        </div>
      ))}
    </div>
  );
}
