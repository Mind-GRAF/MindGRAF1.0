"use client";
import { useState } from 'react';

export default function Home() {
  const [input, setInput] = useState("");
  const [context, setContext] = useState("hogwarts");
  const [attitude, setAttitude] = useState("belief");
  const [status, setStatus] = useState("Idle. Awaiting commands.");

  // NEW: The Boot Sequence
const initializeEnvironment = async () => {
    setStatus("Booting Environment...");
    try {
      // 1. MUST BE FIRST: Fire the Auto-Wizard to fully allocate MindGRAF memory
      const bootRes = await fetch('http://localhost:8080/execute', { method: 'POST', body: `boot-wizard ${attitude}` });
      if (!bootRes.ok) throw new Error("Java rejected the Boot Wizard.");

      // 2. MUST BE SECOND: Define the Context (it will now inherit the correct array sizes!)
      const ctxRes = await fetch('http://localhost:8080/execute', { method: 'POST', body: `define-context ${context}` });
      if (!ctxRes.ok) throw new Error("Java rejected Context.");
      
      setStatus(`Environment Initialized! Context: ${context}, Attitude: ${attitude}. Ready for inputs.`);
    } catch (error) {
      if (error instanceof Error) setStatus(`Boot Error: ${error.message}`);
    }
  };

  const processNaturalLanguage = async () => {
    if (!input) return;
    setStatus("Translating intent with Gemini...");

    try {
      const translateRes = await fetch('/api/translate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ englishText: input }),
      });
      
      if (!translateRes.ok) throw new Error("Translation failed");
      const { command } = await translateRes.json();
      console.log("Gemini Output:", command);
      
      setStatus(`Configuring state...`);

      // Set the active states for this specific node
      await fetch('http://localhost:8080/execute', { method: 'POST', body: `set-curr-context ${context}` });
      await fetch('http://localhost:8080/execute', { method: 'POST', body: `set-attitude ${attitude}` });

      setStatus(`Executing: ${command}`);

      // Insert the node
      const javaRes = await fetch('http://localhost:8080/execute', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: command, 
      });
      
      if (!javaRes.ok) throw new Error("Java execution failed.");
      const result = await javaRes.text();
      setStatus(`Success: Node added!`); 
      setInput(""); 

    } catch (error) {
      if (error instanceof Error) setStatus(`Error: ${error.message}`);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-zinc-50 dark:bg-black p-8 font-sans">
      <main className="w-full max-w-2xl flex flex-col gap-6 bg-white dark:bg-zinc-900 p-8 rounded-xl shadow-lg border border-zinc-200 dark:border-zinc-800">
        
        <div className="flex justify-between items-center border-b border-zinc-200 dark:border-zinc-800 pb-4">
          <h1 className="text-3xl font-semibold tracking-tight text-black dark:text-zinc-50">MindGRAF NLI</h1>
          <button 
            onClick={initializeEnvironment}
            className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded font-bold transition-colors text-sm"
          >
            Initialize Environment
          </button>
        </div>
        
        <div className="grid grid-cols-2 gap-4 mt-2">
          <div>
            <label className="text-sm font-bold text-zinc-500 mb-1 block">Context Namespace</label>
            <input 
              className="w-full p-3 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none"
              value={context}
              onChange={(e) => setContext(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-bold text-zinc-500 mb-1 block">Active Attitude</label>
            <input 
              className="w-full p-3 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none"
              value={attitude}
              onChange={(e) => setAttitude(e.target.value)}
            />
          </div>
        </div>

        <div className="flex flex-col gap-4 mt-2">
          <input 
            className="w-full p-4 border rounded-lg bg-zinc-50 dark:bg-zinc-800 dark:border-zinc-700 text-black dark:text-white outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            placeholder="e.g., Dina is a student"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && processNaturalLanguage()}
          />
          
          <button 
            onClick={processNaturalLanguage}
            className="w-full bg-indigo-600 text-white h-12 rounded-lg font-bold transition-colors hover:bg-indigo-700"
          >
            Process Intent
          </button>
        </div>

        <div className="mt-4 p-4 rounded-lg bg-zinc-100 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800">
          <strong className="text-zinc-900 dark:text-zinc-100">System Status: </strong> 
          <span className="text-zinc-700 dark:text-zinc-400">{status}</span>
        </div>
        
      </main>
    </div>
  );
}