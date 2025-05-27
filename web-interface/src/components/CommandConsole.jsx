import React, { useState, useRef, useEffect } from "react";
import { useAppState } from "../context/AppStateContext";
import { runCommand } from "../api/runCommand";
import {
  Terminal,
  Send,
  Clock,
  CheckCircle,
  XCircle,
  Loader2,
  History,
  Copy,
  Trash2,
  Settings,
  Database,
  Eye,
  Plus,
  Layers,
} from "lucide-react";

function CommandConsole() {
  const { state, dispatch } = useAppState();
  const [command, setCommand] = useState("");
  const [response, setResponse] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [commandHistory, setCommandHistory] = useState([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const [lastExecutionTime, setLastExecutionTime] = useState(null);
  const [executionStatus, setExecutionStatus] = useState(null);

  const textareaRef = useRef(null);
  const responseRef = useRef(null);

  useEffect(() => {
    if (responseRef.current) {
      responseRef.current.scrollTop = responseRef.current.scrollHeight;
    }
  }, [response]);

  // Parse backend responses and update AppState accordingly
  const parseAndUpdateState = (cmd, result) => {
    try {
      const lowerCmd = cmd.toLowerCase().trim();

      // Context creation commands
      if (
        lowerCmd.includes("define-context") &&
        !result.toLowerCase().includes("error")
      ) {
        const contextMatch = cmd.match(/define-context\s+c?\{?([^}]+)\}?/i);
        if (contextMatch) {
          const contextName = contextMatch[1].trim();
          if (!state.contexts.includes(contextName)) {
            dispatch({ type: "ADD_CONTEXT", payload: contextName });
          }
        }
      }
      // Enhanced parsing for more commands
      if (
        lowerCmd.includes("add-to-context") &&
        !result.toLowerCase().includes("error")
      ) {
        // Extract proposition and create visual node
        const propMatch = cmd.match(/add-to-context.*?([A-Za-z]+\([^)]*\))/i);
        if (propMatch) {
          const proposition = propMatch[1];
          // Create a visual representation
          // This would need to integrate with your graph canvas
        }
      }

      // Context switching commands
      if (
        lowerCmd.includes("set-curr-context") &&
        !result.toLowerCase().includes("error")
      ) {
        const contextMatch = cmd.match(/set-curr-context\s+([^\s]+)/i);
        if (contextMatch) {
          const contextName = contextMatch[1].trim();
          dispatch({ type: "SET_CURRENT_CONTEXT", payload: contextName });
        }
      }

      // Attitude switching commands
      if (
        lowerCmd.includes("set-attitude") &&
        !result.toLowerCase().includes("error")
      ) {
        const attitudeMatch = cmd.match(/set-attitude\s+([^\s]+)/i);
        if (attitudeMatch) {
          const attitudeName = attitudeMatch[1].trim();
          if (state.attitudes.includes(attitudeName)) {
            dispatch({ type: "SET_CURRENT_ATTITUDE", payload: attitudeName });
          }
        }
      }

      // Mode changes
      if (
        lowerCmd.includes("set-mode") &&
        !result.toLowerCase().includes("error")
      ) {
        const modeMatch = cmd.match(/set-mode-(\d+)/i);
        if (modeMatch) {
          const mode = parseInt(modeMatch[1]);
          dispatch({ type: "SET_CURRENT_MODE", payload: mode });
        }
      }

      // Add-to-context commands (could create new nodes)
      if (
        lowerCmd.includes("add-to-context") &&
        !result.toLowerCase().includes("error")
      ) {
        const addMatch = cmd.match(
          /add-to-context\s+c\{([^}]+)\}\s+a\{([^}]+)\}\s+(.+)/i
        );
        if (addMatch) {
          const [, contextName, attitudeName, proposition] = addMatch;
          // You could parse the proposition and create a node representation
          // For now, we'll just update the last activity timestamp
          dispatch({
            type: "SAVE_DRAWING_DATA",
            payload: state.drawingData, // Trigger a save timestamp update
          });
        }
      }
    } catch (error) {
      console.error("Error parsing command result:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!command.trim() || isLoading) return;

    setIsLoading(true);
    setExecutionStatus(null);
    const startTime = Date.now();

    // Add to history
    setCommandHistory((prev) => [command, ...prev.slice(0, 49)]);
    setHistoryIndex(-1);

    try {
      const result = await runCommand(command);
      const executionTime = Date.now() - startTime;

      setResponse(result);
      setLastExecutionTime(executionTime);
      setExecutionStatus("success");

      // Parse response and update AppState
      parseAndUpdateState(command, result);

      setCommand("");
    } catch (error) {
      const executionTime = Date.now() - startTime;
      setResponse(`Error: ${error.message || "Command execution failed"}`);
      setLastExecutionTime(executionTime);
      setExecutionStatus("error");
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    // Handle command history navigation
    if (e.key === "ArrowUp" && commandHistory.length > 0) {
      e.preventDefault();
      const newIndex = Math.min(historyIndex + 1, commandHistory.length - 1);
      setHistoryIndex(newIndex);
      setCommand(commandHistory[newIndex]);
    } else if (e.key === "ArrowDown") {
      e.preventDefault();
      if (historyIndex > 0) {
        const newIndex = historyIndex - 1;
        setHistoryIndex(newIndex);
        setCommand(commandHistory[newIndex]);
      } else {
        setHistoryIndex(-1);
        setCommand("");
      }
    }
    // Handle Ctrl+Enter for submission
    else if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
      handleSubmit(e);
    }
    // Handle Tab for command completion
    else if (e.key === "Tab") {
      e.preventDefault();
      const suggestions = getSuggestions(command);
      if (suggestions.length === 1) {
        setCommand(suggestions[0]);
      }
    }
  };

  const getSuggestions = (input) => {
    const baseCommands = [
      "get-attitudes",
      "get-contexts",
      "get-curr-context",
      "get-curr-attitude",
      "set-curr-context",
      "set-attitude",
      "define-context",
      "define-relation",
      "add-to-context",
      "remove-from-context",
      "set-mode-1",
      "set-mode-2",
      "set-mode-3",
      "clear-network",
      "clear-contexts",
    ];

    return baseCommands.filter((cmd) =>
      cmd.toLowerCase().startsWith(input.toLowerCase().trim())
    );
  };

  const copyResponse = () => {
    navigator.clipboard.writeText(response);
  };

  const clearConsole = () => {
    setResponse("");
    setExecutionStatus(null);
    setLastExecutionTime(null);
  };

  const insertSampleCommand = (sampleCmd) => {
    setCommand(sampleCmd);
    textareaRef.current?.focus();
  };

  const insertContextualCommand = (cmdTemplate) => {
    const cmd = cmdTemplate
      .replace("{context}", state.currentContext)
      .replace("{attitude}", state.currentAttitude);
    setCommand(cmd);
    textareaRef.current?.focus();
  };

  // Dynamic sample commands based on current state
  const getContextualCommands = () => [
    "get-attitudes",
    "get-contexts",
    `set-curr-context ${state.currentContext}`,
    `set-attitude ${state.currentAttitude}`,
    `add-to-context c{${state.currentContext}} a{${state.currentAttitude}} P(x)`,
    "define-context ",
    "get-mode",
    "set-mode-1",
  ];

  const getQuickTemplates = () => [
    {
      label: "Add Proposition",
      cmd: `add-to-context c{${state.currentContext}} a{${state.currentAttitude}} `,
      icon: Plus,
    },
    {
      label: "Create Context",
      cmd: "define-context ",
      icon: Layers,
    },
    {
      label: "Query State",
      cmd: "get-curr-context",
      icon: Eye,
    },
    {
      label: "View Data",
      cmd: "get-contexts",
      icon: Database,
    },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 rounded-2xl shadow-2xl border border-slate-700 overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-slate-800 to-slate-700 px-6 py-4 border-b border-slate-600">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-green-400 to-blue-500 rounded-lg flex items-center justify-center">
                <Terminal className="w-5 h-5 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">
                  Mind GRAF Console
                </h2>
                <p className="text-sm text-slate-300">
                  Execute commands and interact with the system
                </p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              {/* Current State Display */}
              <div className="text-right text-sm text-slate-300">
                <div>
                  Context:{" "}
                  <span className="text-blue-400 font-medium">
                    {state.currentContext}
                  </span>
                </div>
                <div>
                  Attitude:{" "}
                  <span className="text-green-400 font-medium">
                    {state.currentAttitude}
                  </span>
                </div>
                <div>
                  Mode:{" "}
                  <span className="text-purple-400 font-medium">
                    {state.currentMode}
                  </span>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                {lastExecutionTime && (
                  <div className="flex items-center space-x-1 text-slate-300 text-sm">
                    <Clock className="w-4 h-4" />
                    <span>{lastExecutionTime}ms</span>
                  </div>
                )}
                {executionStatus && (
                  <div
                    className={`flex items-center space-x-1 text-sm ${
                      executionStatus === "success"
                        ? "text-green-400"
                        : "text-red-400"
                    }`}
                  >
                    {executionStatus === "success" ? (
                      <CheckCircle className="w-4 h-4" />
                    ) : (
                      <XCircle className="w-4 h-4" />
                    )}
                    <span>
                      {executionStatus === "success" ? "Success" : "Error"}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Quick Templates */}
        <div className="px-6 py-4 bg-slate-800/50 border-b border-slate-700">
          <div className="flex items-center space-x-2 mb-3">
            <Settings className="w-4 h-4 text-slate-400" />
            <span className="text-sm font-medium text-slate-300">
              Quick Templates:
            </span>
          </div>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
            {getQuickTemplates().map((template, index) => (
              <button
                key={index}
                onClick={() => insertContextualCommand(template.cmd)}
                className="flex items-center space-x-2 bg-slate-700 hover:bg-slate-600 text-slate-200 px-3 py-2 rounded-lg text-sm transition-colors border border-slate-600"
              >
                <template.icon className="w-4 h-4" />
                <span>{template.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Contextual Commands */}
        <div className="px-6 py-4 bg-slate-800/30 border-b border-slate-700">
          <div className="flex items-center space-x-2 mb-2">
            <History className="w-4 h-4 text-slate-400" />
            <span className="text-sm font-medium text-slate-300">
              Contextual Commands:
            </span>
          </div>
          <div className="flex flex-wrap gap-2">
            {getContextualCommands().map((cmd, index) => (
              <button
                key={index}
                onClick={() => insertSampleCommand(cmd)}
                className="text-xs bg-slate-700 hover:bg-slate-600 text-slate-200 px-3 py-1 rounded-full transition-colors border border-slate-600"
              >
                {cmd}
              </button>
            ))}
          </div>
        </div>

        {/* Command Input */}
        <div className="p-6">
          <div className="space-y-4">
            <div className="relative">
              <textarea
                ref={textareaRef}
                className="w-full p-4 bg-slate-800 border border-slate-600 rounded-xl text-white placeholder-slate-400 resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
                rows="4"
                placeholder={`Enter Mind GRAF command here... (Current: ${state.currentContext}/${state.currentAttitude})\nUse ↑/↓ for history, Ctrl+Enter to execute, Tab for suggestions`}
                value={command}
                onChange={(e) => setCommand(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={isLoading}
              />
              <div className="absolute bottom-3 right-3 flex items-center space-x-2 text-xs text-slate-500">
                <span>{command.length}/1000</span>
                {getSuggestions(command).length > 0 && (
                  <span className="text-blue-400">Tab for suggestions</span>
                )}
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <button
                onClick={handleSubmit}
                disabled={!command.trim() || isLoading}
                className="flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 disabled:from-slate-600 disabled:to-slate-700 text-white px-6 py-3 rounded-xl font-medium transition-all shadow-lg disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <Loader2 className="w-5 h-5 animate-spin" />
                ) : (
                  <Send className="w-5 h-5" />
                )}
                <span>{isLoading ? "Executing..." : "Run Command"}</span>
              </button>

              <button
                onClick={clearConsole}
                className="flex items-center space-x-2 bg-slate-700 hover:bg-slate-600 text-slate-200 px-4 py-3 rounded-xl font-medium transition-colors"
              >
                <Trash2 className="w-4 h-4" />
                <span>Clear</span>
              </button>
            </div>
          </div>
        </div>

        {/* Response Section */}
        {response && (
          <div className="border-t border-slate-700">
            <div className="px-6 py-4 bg-slate-800/30">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-lg font-semibold text-white flex items-center space-x-2">
                  <Terminal className="w-5 h-5" />
                  <span>Output</span>
                </h3>
                <button
                  onClick={copyResponse}
                  className="flex items-center space-x-2 bg-slate-700 hover:bg-slate-600 text-slate-200 px-3 py-2 rounded-lg text-sm transition-colors"
                >
                  <Copy className="w-4 h-4" />
                  <span>Copy</span>
                </button>
              </div>
              <div
                ref={responseRef}
                className="bg-slate-900 border border-slate-600 rounded-xl p-4 text-sm font-mono text-slate-200 whitespace-pre-wrap max-h-96 overflow-y-auto custom-scrollbar"
              >
                {response}
              </div>
            </div>
          </div>
        )}

        {/* Enhanced Command History */}
        {commandHistory.length > 0 && (
          <div className="border-t border-slate-700 px-6 py-4 bg-slate-800/20">
            <details className="group">
              <summary className="flex items-center space-x-2 text-slate-300 cursor-pointer hover:text-white transition-colors">
                <History className="w-4 h-4" />
                <span className="font-medium">
                  Command History ({commandHistory.length})
                </span>
                <div className="ml-auto group-open:rotate-180 transition-transform">
                  <svg
                    className="w-4 h-4"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
              </summary>
              <div className="mt-3 space-y-1 max-h-40 overflow-y-auto">
                {commandHistory.slice(0, 10).map((cmd, index) => (
                  <button
                    key={index}
                    onClick={() => insertSampleCommand(cmd)}
                    className="w-full text-left p-2 bg-slate-700/50 hover:bg-slate-700 text-slate-300 rounded-lg text-sm font-mono transition-colors"
                  >
                    {cmd}
                  </button>
                ))}
              </div>
            </details>
          </div>
        )}

        {/* State Information Panel */}
        <div className="border-t border-slate-700 px-6 py-3 bg-slate-900/50">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <div className="flex items-center space-x-4">
              <span>Contexts: {state.contexts.length}</span>
              <span>Attitudes: {state.attitudes.length}</span>
              <span>Nodes: {Object.keys(state.nodes).length}</span>
              <span>Relations: {Object.keys(state.relations).length}</span>
            </div>
            <div className="flex items-center space-x-4">
              <span>
                Setup: {state.isSetupComplete ? "Complete" : "Pending"}
              </span>
              {state.lastSaved && (
                <span>
                  Saved: {new Date(state.lastSaved).toLocaleTimeString()}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      <style jsx>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 8px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: #1e293b;
          border-radius: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: #475569;
          border-radius: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: #64748b;
        }
      `}</style>
    </div>
  );
}

export default CommandConsole;
