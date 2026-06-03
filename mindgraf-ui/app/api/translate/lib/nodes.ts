/**
 * nodes.ts — Phase 1–5: Router, Structural, Knowledge, Query, Inference & System Specialists, Validator, Executor.
 */
import { ChatGroq } from "@langchain/groq";
import { ChatGoogleGenerativeAI } from "@langchain/google-genai";
import { HumanMessage, SystemMessage } from "@langchain/core/messages";
import type { GraphStateType } from "./graph-state";
import {
  ROUTER_PROMPT,
  STRUCTURAL_SPECIALIST_PROMPT,
  KNOWLEDGE_SPECIALIST_PROMPT,
  QUERY_SPECIALIST_PROMPT,
  INFERENCE_SPECIALIST_PROMPT,
  SYSTEM_SPECIALIST_PROMPT,
  ERROR_RETRY_SUFFIX,
} from "./prompts";

// ─────────────────────────────────────────────────────────────────────────────
// LLM — Gemini 2.5 Flash, temperature=0 for deterministic syntax output
// ─────────────────────────────────────────────────────────────────────────────
const llm = new ChatGroq({
  apiKey: process.env.GROQ_API_KEY,
  model: "llama-3.1-8b-instant", // The incredibly fast, free Meta model
  temperature: 0,
  maxRetries: 0, // Keeps it strict so it doesn't loop
});


const JAVA_URL = "http://localhost:8080/execute";
export const MAX_RETRIES = 3;

// ─────────────────────────────────────────────────────────────────────────────
// COMMAND PREFIXES
// ─────────────────────────────────────────────────────────────────────────────
const STRUCTURAL_COMMANDS = [
  "define-context",
  "define-relation",
  "define-path",
  "define-frame",
  "define-semantic",
  "define-primitive-act",
  "define-nonprimitive-act",
  "set-mode-1",
  "set-mode-2",
  "set-mode-3",
  "get-mode",
  "get-rels",
] as const;

const KNOWLEDGE_COMMANDS = [
  "add-to-context",
  "remove-from-context",
  "add-bridge",
  "activate-node",
  "activate-node!",
] as const;

const QUERY_COMMANDS = [
  "ask-if-true",
  "ask-if-not",
  "ask-why",
  "ask-whynot",
  "get-curr-context",
  "get-attitudes",
  "get-curr-attitude",
  "get-contexts",
  "get-context-hyps",
  "get-supported",
  "describe-context",
] as const;

const INFERENCE_COMMANDS = [
  "forward-infer",
  "back-infer",
  "perform-act",
  "clear-infer",
] as const;

const SYSTEM_COMMANDS = [
  "set-curr-context",
  "set-attitude",
  "clear-network",
  "set-attitudes",
  "set-consistent-attitudes",
] as const;

export const ALL_KNOWN_COMMANDS = [...STRUCTURAL_COMMANDS, ...KNOWLEDGE_COMMANDS, ...QUERY_COMMANDS, ...INFERENCE_COMMANDS, ...SYSTEM_COMMANDS];

// Helper to safely extract text whether LangChain returns a string or an array of blocks
// Define the shape of LangChain's content blocks so TypeScript is happy
type LangChainContentBlock = { text?: string; [key: string]: unknown };

// Helper to safely extract text whether LangChain returns a string or an array of blocks
function extractText(content: string | LangChainContentBlock[]): string {
  if (typeof content === "string") return content;
  if (Array.isArray(content)) {
    return content.map((block) => block.text || "").join("");
  }
  return "";
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: router
// ─────────────────────────────────────────────────────────────────────────────
export async function routerNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  const response = await llm.invoke([
    new SystemMessage(ROUTER_PROMPT),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim().toLowerCase();
  const validCategories = ["structural", "knowledge", "query", "inference", "system", "other"];
  const category = validCategories.includes(raw) ? raw : "other";

  return { category };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: structuralSpecialistNode
// ─────────────────────────────────────────────────────────────────────────────
export async function structuralSpecialistNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  
  let systemPrompt = STRUCTURAL_SPECIALIST_PROMPT;
  
  // The Self-Healing Injection: Send the error back to the LLM
  if (state.retryCount > 0 && state.errorHistory.length > 0) {
    const lastError = state.errorHistory[state.errorHistory.length - 1];
    systemPrompt += ERROR_RETRY_SUFFIX(state.generatedCommand, lastError);
  }

  const response = await llm.invoke([
    new SystemMessage(systemPrompt),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim();
  
  // Strip markdown code fences if Gemini accidentally adds them
  const command = raw
    .replace(/^```[a-z]*\n?/gm, "") 
    .replace(/```$/gm, "")          
    .split("\n")
    .map((l) => l.trim())
    .filter((l) => l.length > 0)
    .join("\n") || raw; // Take all lines

  return { generatedCommand: command };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: knowledgeSpecialistNode
// ─────────────────────────────────────────────────────────────────────────────
export async function knowledgeSpecialistNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  
  let systemPrompt = KNOWLEDGE_SPECIALIST_PROMPT;
  
  // The Self-Healing Injection: Send the error back to the LLM
  if (state.retryCount > 0 && state.errorHistory.length > 0) {
    const lastError = state.errorHistory[state.errorHistory.length - 1];
    systemPrompt += ERROR_RETRY_SUFFIX(state.generatedCommand, lastError);
  }

  const response = await llm.invoke([
    new SystemMessage(systemPrompt),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim();
  
  // Strip markdown code fences if Gemini accidentally adds them
  const command = raw
    .replace(/^```[a-z]*\n?/gm, "") 
    .replace(/```$/gm, "")          
    .split("\n")
    .map((l) => l.trim())
    .filter((l) => l.length > 0)
    .join("\n") || raw; // Take all lines

  return { generatedCommand: command };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: querySpecialistNode
// ─────────────────────────────────────────────────────────────────────────────
export async function querySpecialistNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  
  let systemPrompt = QUERY_SPECIALIST_PROMPT;
  
  // The Self-Healing Injection: Send the error back to the LLM
  if (state.retryCount > 0 && state.errorHistory.length > 0) {
    const lastError = state.errorHistory[state.errorHistory.length - 1];
    systemPrompt += ERROR_RETRY_SUFFIX(state.generatedCommand, lastError);
  }

  const response = await llm.invoke([
    new SystemMessage(systemPrompt),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim();
  
  // Strip markdown code fences if Gemini accidentally adds them
  const command = raw
    .replace(/^```[a-z]*\n?/gm, "") 
    .replace(/```$/gm, "")          
    .split("\n")
    .map((l) => l.trim())
    .filter((l) => l.length > 0)
    .join("\n") || raw; // Take all lines

  return { generatedCommand: command };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: inferenceSpecialistNode
// ─────────────────────────────────────────────────────────────────────────────
export async function inferenceSpecialistNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  
  let systemPrompt = INFERENCE_SPECIALIST_PROMPT;
  
  // The Self-Healing Injection: Send the error back to the LLM
  if (state.retryCount > 0 && state.errorHistory.length > 0) {
    const lastError = state.errorHistory[state.errorHistory.length - 1];
    systemPrompt += ERROR_RETRY_SUFFIX(state.generatedCommand, lastError);
  }

  const response = await llm.invoke([
    new SystemMessage(systemPrompt),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim();
  
  // Strip markdown code fences if Gemini accidentally adds them
  const command = raw
    .replace(/^```[a-z]*\n?/gm, "") 
    .replace(/```$/gm, "")          
    .split("\n")
    .map((l) => l.trim())
    .filter((l) => l.length > 0)
    .join("\n") || raw; // Take all lines

  return { generatedCommand: command };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: systemSpecialistNode
// ─────────────────────────────────────────────────────────────────────────────
export async function systemSpecialistNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  
  let systemPrompt = SYSTEM_SPECIALIST_PROMPT;
  
  // The Self-Healing Injection: Send the error back to the LLM
  if (state.retryCount > 0 && state.errorHistory.length > 0) {
    const lastError = state.errorHistory[state.errorHistory.length - 1];
    systemPrompt += ERROR_RETRY_SUFFIX(state.generatedCommand, lastError);
  }

  const response = await llm.invoke([
    new SystemMessage(systemPrompt),
    new HumanMessage(state.userInput),
  ]);

  const raw = extractText(response.content).trim();
  
  // Strip markdown code fences if Gemini accidentally adds them
  const command = raw
    .replace(/^```[a-z]*\n?/gm, "") 
    .replace(/```$/gm, "")          
    .split("\n")
    .map((l) => l.trim())
    .filter((l) => l.length > 0)
    .join("\n") || raw; // Take all lines

  return { generatedCommand: command };
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: validatorNode
// ─────────────────────────────────────────────────────────────────────────────
export async function validatorNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  const commands = state.generatedCommand.split("\n").map(c => c.trim()).filter(c => c);
  const errors: string[] = [];

  for (const cmd of commands) {
    // 1. Known prefix check
    const knownPrefix = ALL_KNOWN_COMMANDS.find((kw) => cmd === kw || cmd.startsWith(kw + " "));
    if (!knownPrefix) {
      const first = cmd.split(/\s/)[0];
      errors.push(`"${first}" is not a recognized command. Valid commands: ${ALL_KNOWN_COMMANDS.join(", ")}`);
    }

    // 2. Bracket balance
    let parens = 0, braces = 0;
    for (let i = 0; i < cmd.length; i++) {
      if (cmd[i] === "(") parens++;
      else if (cmd[i] === ")") parens--;
      else if (cmd[i] === "{") braces++;
      else if (cmd[i] === "}") braces--;
      if (parens < 0) { errors.push(`Unexpected ')' at pos ${i}`); parens = 0; }
      if (braces < 0) { errors.push(`Unexpected '}' at pos ${i}`); braces = 0; }
    }
    if (parens > 0) errors.push(`${parens} unclosed parenthesis(es)`);
    if (braces > 0) errors.push(`${braces} unclosed brace(s)`);

    // 3. Common JavaCC pitfalls
    if (/rel\s+\{/.test(cmd)) errors.push('Found "rel {" — must be "rel{" with no space before the brace.');
    if (/["']/.test(cmd)) errors.push("MindGRAF CLI does not use quotes. Remove them.");
    if (/\?[A-Za-z]/.test(cmd)) errors.push('Found "?Name" variable syntax — must be "Name?"');
    if (/define-relation\s+rel\(/.test(cmd)) errors.push('define-relation uses curly braces: rel{...}');
    if (/implies\s*\(/.test(cmd)) errors.push('"implies()" is not a valid command. Use entailment syntax: add-to-context {antecedent} &=> {consequent}');
    if (/forall\s+\(/.test(cmd)) errors.push('Found "forall (" — must be "forall(" with no space before the parenthesis.');
    if (/[^&vV\d]=>/.test(cmd) && !/[&v]\s*=>/.test(cmd)) errors.push('Bare "=>" is invalid. Use &=> (and-entailment), v=> (or-entailment), or N=> (numeric).');
  }

  if (errors.length > 0) {
    return { errorHistory: [`Validator: ${errors.join(" | ")}`] };
  }

  return {}; // Validation passed
}

// ─────────────────────────────────────────────────────────────────────────────
// NODE: executorNode
// ─────────────────────────────────────────────────────────────────────────────
export async function executorNode(
  state: GraphStateType
): Promise<Partial<GraphStateType>> {
  try {
    // STATELESS SYNC: Prep the Java environment before executing the command!
    if (state.contextName && state.contextName.trim() !== "") {
      await fetch(JAVA_URL, { method: "POST", body: `set-curr-context ${state.contextName.trim()}` }).catch(()=>null);
    }
    if (state.attitudeName && state.attitudeName.trim() !== "") {
      await fetch(JAVA_URL, { method: "POST", body: `set-attitude ${state.attitudeName.trim()}` }).catch(()=>null);
    }

    const commands = state.generatedCommand.split("\n").map(c => c.trim()).filter(c => c);
    const responses: string[] = [];

    for (const c of commands) {
      // Execute the actual AI command
      const res = await fetch(JAVA_URL, {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: c,
      });

      const responseText = await res.text();
      const lower = responseText.toLowerCase();

      // Soft Success Checks
      if (res.ok || lower.includes("already exist") || lower.includes("duplicate") || lower.includes("is created")) {
        responses.push(responseText);
      } else {
        // Real parser error -> Feed back into retry loop
        return {
          javaResponse: responseText,
          success: false,
          retryCount: state.retryCount + 1,
          errorHistory: [`Java: ${responseText} (on command: ${c})`],
        };
      }
    }

    return { javaResponse: responses.join("\n"), success: true };
  } catch (err) {
    const msg = err instanceof Error ? err.message : String(err);
    return {
      javaResponse: "",
      success: false,
      retryCount: state.retryCount + 1,
      errorHistory: [`Network Error: ${msg}`],
    };
  }
}
// ─────────────────────────────────────────────────────────────────────────────
// ROUTING FUNCTIONS  (read by conditional edges in graph.ts)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * After the router: route to the correct specialist.
 */
export function routeFromRouter(state: GraphStateType): string {
  if (state.category === "structural") return "structuralSpecialist";
  if (state.category === "knowledge") return "knowledgeSpecialist";
  if (state.category === "query") return "querySpecialist";
  if (state.category === "inference") return "inferenceSpecialist";
  if (state.category === "system") return "systemSpecialist";
  return "unsupported"; // Trap any command we haven't built yet!
}

/**
 * After validation: if there are new errors, send back to specialist; 
 * otherwise proceed to executor.
 */
export function routeAfterValidation(state: GraphStateType): string {
  const lastError = state.errorHistory[state.errorHistory.length - 1] ?? "";
  const validationFailed = lastError.startsWith("Validator:");
  if (validationFailed && state.retryCount < MAX_RETRIES) {
    return "retry";
  }
  return "execute";
}

/**
 * After execution: success -> end; error + retries remaining -> retry specialist.
 */
export function routeAfterExecution(state: GraphStateType): string {
  if (state.success) return "done";
  if (state.retryCount >= MAX_RETRIES) return "done"; // give up gracefully
  return "retry";
}

/**
 * From the retry hub: route back to the right specialist.
 */
export function routeRetry(state: GraphStateType): string {
  if (state.category === "structural") return "structural";
  if (state.category === "knowledge") return "knowledge";
  if (state.category === "query") return "query";
  if (state.category === "inference") return "inference";
  if (state.category === "system") return "system";
  return "structural"; // Fallback
}