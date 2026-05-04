"use client";
import { useState } from "react";

// ─── Types ──────────────────────────────────────────────────────────────────
interface TranslateResponse {
  command?: string;
  category?: string;
  javaResponse?: string;
  retryCount?: number;
  success?: boolean;
  errors?: string[];
  error?: string;
}

// ─── Status badge colours ────────────────────────────────────────────────────
function categoryColor(cat: string) {
  const map: Record<string, string> = {
    structural: "bg-violet-100 text-violet-700 dark:bg-violet-900/40 dark:text-violet-300",
    knowledge:  "bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300",
    query:      "bg-cyan-100 text-cyan-700 dark:bg-cyan-900/40 dark:text-cyan-300",
    inference:  "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300",
    system:     "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300",
    other:      "bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400",
  };
  return map[cat] ?? map.other;
}

export default function Home() {
  const [input, setInput]       = useState("");
  const [context, setContext]   = useState("hogwarts");
  const [attitude, setAttitude] = useState("belief");
  const [loading, setLoading]   = useState(false);
  const [lastResult, setLastResult] = useState<TranslateResponse | null>(null);
  const [statusMsg, setStatusMsg]   = useState("Idle. Awaiting commands.");
  const [booted, setBooted]         = useState(false);

  // ── Boot sequence  ─────────────────────────────────────────────────────────
  const initializeEnvironment = async () => {
    setStatusMsg("Booting MindGRAF engine…");
    setLoading(true);
    try {
      const bootRes = await fetch("http://localhost:8080/execute", {
        method: "POST",
        body: `boot-wizard ${attitude}`,
      });
      if (!bootRes.ok) throw new Error("Java rejected the Boot Wizard.");

      const ctxRes = await fetch("http://localhost:8080/execute", {
        method: "POST",
        body: `define-context ${context}`,
      });
      // "Already exists" is fine — idempotent
      if (!ctxRes.ok) {
        const t = await ctxRes.text();
        if (!t.toLowerCase().includes("already exist"))
          throw new Error("Java rejected Context definition.");
      }

      setBooted(true);
      setStatusMsg(
        `✅ Ready — context: "${context}", attitude: "${attitude}"`
      );
    } catch (err) {
      setStatusMsg(`❌ Boot Error: ${err instanceof Error ? err.message : err}`);
    } finally {
      setLoading(false);
    }
  };

  // ── Main pipeline ──────────────────────────────────────────────────────────
  const processNaturalLanguage = async () => {
    if (!input.trim()) return;
    setLoading(true);
    setStatusMsg("Running LangGraph pipeline…");
    setLastResult(null);

    try {
      const res = await fetch("/api/translate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          englishText: input,
          contextName: context,
          attitudeName: attitude,
        }),
      });

      const data: TranslateResponse = await res.json();
      setLastResult(data);

      if (!res.ok || data.error) {
        setStatusMsg(`❌ Pipeline error: ${data.error ?? "unknown"}`);
      } else if (data.success) {
        setStatusMsg(`✅ Success${data.retryCount ? ` (${data.retryCount} retr${data.retryCount === 1 ? "y" : "ies"})` : ""}`);
        setInput("");
      } else {
        setStatusMsg(
          `⚠️ Max retries reached — command may not have been accepted.`
        );
      }
    } catch (err) {
      setStatusMsg(`❌ Network error: ${err instanceof Error ? err.message : err}`);
    } finally {
      setLoading(false);
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-zinc-50 dark:bg-black p-8 font-sans">
      <main className="w-full max-w-2xl flex flex-col gap-6 bg-white dark:bg-zinc-900 p-8 rounded-xl shadow-lg border border-zinc-200 dark:border-zinc-800">

        {/* Header */}
        <div className="flex justify-between items-center border-b border-zinc-200 dark:border-zinc-800 pb-4">
          <div>
            <h1 className="text-3xl font-semibold tracking-tight text-black dark:text-zinc-50">
              MindGRAF NLI
            </h1>
            <p className="text-xs text-zinc-400 mt-0.5">
              LangGraph · Self-Healing Pipeline · Phase 1
            </p>
          </div>
          <button
            id="btn-init"
            onClick={initializeEnvironment}
            disabled={loading}
            className={`px-4 py-2 rounded font-bold transition-colors text-sm ${
              booted
                ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300 cursor-default"
                : "bg-emerald-600 hover:bg-emerald-700 text-white"
            }`}
          >
            {booted ? "✓ Initialized" : "Initialize Engine"}
          </button>
        </div>

        {/* Context / Attitude */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="text-xs font-bold text-zinc-500 mb-1 block uppercase tracking-wider">
              Context
            </label>
            <input
              id="input-context"
              className="w-full p-3 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none focus:ring-2 focus:ring-indigo-400"
              value={context}
              onChange={(e) => setContext(e.target.value)}
            />
          </div>
          <div>
            <label className="text-xs font-bold text-zinc-500 mb-1 block uppercase tracking-wider">
              Attitude
            </label>
            <input
              id="input-attitude"
              className="w-full p-3 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none focus:ring-2 focus:ring-indigo-400"
              value={attitude}
              onChange={(e) => setAttitude(e.target.value)}
            />
          </div>
        </div>

        {/* Input */}
        <div className="flex flex-col gap-3">
          <textarea
            id="input-nl"
            rows={2}
            className="w-full p-4 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none focus:ring-2 focus:ring-indigo-500 transition-all resize-none"
            placeholder='e.g. "Define a relation for mother" or "Create a context named hogwarts"'
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                processNaturalLanguage();
              }
            }}
          />
          <button
            id="btn-process"
            onClick={processNaturalLanguage}
            disabled={loading || !input.trim()}
            className="w-full bg-indigo-600 disabled:bg-indigo-300 text-white h-12 rounded-lg font-bold transition-colors hover:bg-indigo-700"
          >
            {loading ? "Processing…" : "Process Intent"}
          </button>
        </div>

        {/* Status bar */}
        <div className="p-3 rounded-lg bg-zinc-100 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 text-sm">
          <span className="font-semibold text-zinc-700 dark:text-zinc-300">Status: </span>
          <span className="text-zinc-600 dark:text-zinc-400">{statusMsg}</span>
        </div>

        {/* Pipeline result card */}
        {lastResult && !lastResult.error && (
          <div className="flex flex-col gap-3 p-4 rounded-xl border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800/50 text-sm">
            {/* Category badge */}
            {lastResult.category && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-zinc-400 uppercase tracking-wider w-24">Category</span>
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${categoryColor(lastResult.category)}`}>
                  {lastResult.category}
                </span>
              </div>
            )}
            {/* Generated command */}
            {lastResult.command && (
              <div className="flex items-start gap-2">
                <span className="text-xs text-zinc-400 uppercase tracking-wider w-24 pt-0.5">Command</span>
                <code className="flex-1 font-mono text-xs bg-zinc-900 text-emerald-400 px-3 py-2 rounded break-all">
                  {lastResult.command}
                </code>
              </div>
            )}
            {/* Java response */}
            {lastResult.javaResponse && (
              <div className="flex items-start gap-2">
                <span className="text-xs text-zinc-400 uppercase tracking-wider w-24 pt-0.5">Java</span>
                <span className="flex-1 text-zinc-600 dark:text-zinc-400 break-all">
                  {lastResult.javaResponse}
                </span>
              </div>
            )}
            {/* Retries */}
            {(lastResult.retryCount ?? 0) > 0 && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-zinc-400 uppercase tracking-wider w-24">Retries</span>
                <span className="text-amber-600 dark:text-amber-400 font-semibold">
                  {lastResult.retryCount}
                </span>
              </div>
            )}
            {/* Error history */}
            {lastResult.errors && lastResult.errors.length > 0 && (
              <details className="mt-1">
                <summary className="text-xs text-zinc-400 cursor-pointer hover:text-zinc-600">
                  {lastResult.errors.length} error(s) in retry history
                </summary>
                <ul className="mt-2 space-y-1 pl-2 border-l-2 border-red-300">
                  {lastResult.errors.map((e, i) => (
                    <li key={i} className="text-xs text-red-500 dark:text-red-400">{e}</li>
                  ))}
                </ul>
              </details>
            )}
          </div>
        )}

      </main>
    </div>
  );
}