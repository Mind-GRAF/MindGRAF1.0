/**
 * graph.ts — Phase 1–5: Full StateGraph wiring.
 *
 * Topology:
 *
 *   __start__
 *       │
 *       ▼
 *    routerNode ─────────────(conditional: routeFromRouter)─────────────┐
 *       │                                                                 │
 *       │ "structural"  "knowledge"  "query"  "inference"  "system"     │ 
 *       ▼                ▼            ▼         ▼            ▼          ▼
 *  structural    knowledge    querySpecialist   inference    system   (...)
 *       │            │            │
 *       └────────────┼────────────┘
 *                    ▼
 *              validatorNode ────(conditional: routeAfterValidation)─► retryHub
 *                    │ "execute"                                           │
 *                    ▼                                                     │ (conditional: routeRetry)
 *              executorNode ────(conditional: routeAfterExecution)─► retryHub
 *                    │ "done"                                              │
 *                    ▼                                            ◄────────┘ → structural | knowledge | query
 *                 __end__
 *
 * Notes:
 *  • retryHub is a lightweight pass-through node that increments retryCount
 *    when coming from validation failure and routes back to the right specialist.
 *  • The graph is compiled once (module-level singleton) so it is reused
 *    across all Next.js route invocations in the same server lifetime.
 */
import { StateGraph, END } from "@langchain/langgraph";
import { GraphState } from "./graph-state";
import {
  routerNode,
  structuralSpecialistNode,
  knowledgeSpecialistNode,
  querySpecialistNode,
  inferenceSpecialistNode,
  systemSpecialistNode,
  validatorNode,
  executorNode,
  routeFromRouter,
  routeAfterValidation,
  routeAfterExecution,
  routeRetry,
} from "./nodes";
import type { GraphStateType } from "./graph-state";

// ─────────────────────────────────────────────────────────────────────────────
// retryHub
// A tiny inline node — its only jobs are:
//   1. Increment retryCount if required (validation failures haven't yet bumped it).
//   2. Act as the single re-entry point before addConditionalEdges routes
//      back to the correct specialist.
// ─────────────────────────────────────────────────────────────────────────────
async function retryHub(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  // If the last error came from the validator (not the executor),
  // the retryCount hasn't been bumped yet — bump it now.
  const lastError = state.errorHistory[state.errorHistory.length - 1] ?? "";
  if (lastError.startsWith("Validator:")) {
    return { retryCount: state.retryCount + 1 };
  }
  // Executor already bumped retryCount — nothing to do here.
  return {};
}

// ─────────────────────────────────────────────────────────────────────────────
// Build & compile the graph  (singleton)
// ─────────────────────────────────────────────────────────────────────────────
function buildGraph() {
  return (
    new StateGraph(GraphState)
      // ── Register nodes ────────────────────────────────────────────────────
      .addNode("router", routerNode)
      .addNode("structural", structuralSpecialistNode)
      .addNode("knowledge", knowledgeSpecialistNode)
      .addNode("query", querySpecialistNode)
      .addNode("inference", inferenceSpecialistNode)
      .addNode("system", systemSpecialistNode)
      .addNode("validator", validatorNode)
      .addNode("executor", executorNode)
      .addNode("retryHub", retryHub)

      // ── Entry ─────────────────────────────────────────────────────────────
      .addEdge("__start__", "router")

      // ── Router → Specialist ───────────────────────────────────────────────
      .addConditionalEdges("router", routeFromRouter, {
        structuralSpecialist: "structural",
        knowledgeSpecialist: "knowledge",
        querySpecialist: "query",
        inferenceSpecialist: "inference",
        systemSpecialist: "system",
      })

      // ── Specialist → Validator ────────────────────────────────────────────
      .addEdge("structural", "validator")
      .addEdge("knowledge", "validator")
      .addEdge("query", "validator")
      .addEdge("inference", "validator")
      .addEdge("system", "validator")

      // ── Validator → Execute OR retry ──────────────────────────────────────
      .addConditionalEdges("validator", routeAfterValidation, {
        execute: "executor",
        retry: "retryHub",
      })

      // ── Executor → End OR retry ───────────────────────────────────────────
      .addConditionalEdges("executor", routeAfterExecution, {
        done: END,
        retry: "retryHub",
      })

      // ── RetryHub → back to specialist ─────────────────────────────────────
      .addConditionalEdges("retryHub", routeRetry, {
        structural: "structural",
        knowledge: "knowledge",
        query: "query",
        inference: "inference",
        system: "system",
      })

      .compile()
  );
}

export const mindGRAFGraph = buildGraph();
