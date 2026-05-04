import { Annotation } from "@langchain/langgraph";

/**
 * Shared state flowing through every node in the MindGRAF translation graph.
 *
 * reducer: (_prev, next) => next  means: always replace with the latest value.
 * errorHistory uses concat so every error in the retry loop is preserved.
 */
export const GraphState = Annotation.Root({
  /** The original natural language text from the user */
  userInput: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "",
  }),

  /** The router's classification: "structural" | "other" (more added in future phases) */
  category: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "",
  }),

  /** The CLI command string produced by a specialist node */
  generatedCommand: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "",
  }),

  /** The raw text response from the Java/Javalin server */
  javaResponse: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "",
  }),

  /** How many times we have retried generation after an error */
  retryCount: Annotation<number>({
    reducer: (_prev, next) => next,
    default: () => 0,
  }),

  /** Accumulated error messages across retry attempts */
  errorHistory: Annotation<string[]>({
    reducer: (prev, next) => [...prev, ...next],
    default: () => [],
  }),

  /** The active context name (e.g. "hogwarts") */
  contextName: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "default",
  }),

  /** The active attitude name (e.g. "belief") */
  attitudeName: Annotation<string>({
    reducer: (_prev, next) => next,
    default: () => "belief",
  }),

  /** Whether the command ultimately succeeded */
  success: Annotation<boolean>({
    reducer: (_prev, next) => next,
    default: () => false,
  }),
});

export type GraphStateType = typeof GraphState.State;
