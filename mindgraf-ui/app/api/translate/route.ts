import { NextResponse } from "next/server";
import { mindGRAFGraph } from "./lib/graph";

/**
 * POST /api/translate
 *
 * Body: { englishText: string, contextName?: string, attitudeName?: string }
 *
 * Runs the full LangGraph self-healing pipeline:
 *   Router → Structural Specialist → Validator → Executor (with retry loop)
 *
 * Returns:
 * {
 *   command:      string   — the final CLI command that was generated
 *   category:     string   — the router's classification
 *   javaResponse: string   — what the Java server said
 *   retryCount:   number   — how many self-healing retries occurred
 *   success:      boolean  — whether the Java server accepted the command
 *   errors:       string[] — accumulated error history (empty on clean run)
 * }
 */
export async function POST(req: Request) {
  try {
    const body = await req.json();
    const {
      englishText,
      contextName = "default",
      attitudeName = "belief",
    } = body as {
      englishText: string;
      contextName?: string;
      attitudeName?: string;
    };

    if (!englishText?.trim()) {
      return NextResponse.json(
        { error: "englishText is required" },
        { status: 400 }
      );
    }

    // ── Run the LangGraph pipeline ──────────────────────────────────────────
    const result = await mindGRAFGraph.invoke({
      userInput: englishText.trim(),
      contextName,
      attitudeName,
    });

    return NextResponse.json({
      command: result.generatedCommand,
      category: result.category,
      javaResponse: result.javaResponse,
      retryCount: result.retryCount,
      success: result.success,
      errors: result.errorHistory,
    });
  } catch (err) {
    console.error("[MindGRAF Graph] Unhandled error:", err);
    return NextResponse.json(
      { error: err instanceof Error ? err.message : "Translation pipeline failed" },
      { status: 500 }
    );
  }
}