import React, { useState, useEffect } from "react";
import { useAppState } from "../context/AppStateContext";
import { runCommand, syncContextsWithBackend } from "../api/runCommand";
import {
  Circle,
  Square,
  Triangle,
  Diamond,
  Hexagon,
  Settings,
  Eye,
  Plus,
  ChevronDown,
  ChevronRight,
  Layers,
  Heart,
  Target,
  User,
  Zap,
  Filter,
  AlertCircle,
  CheckCircle,
  Trash2,
  Route,
} from "lucide-react";

export default function Sidebar() {
  const { state, dispatch } = useAppState();
  const [expandedSections, setExpandedSections] = useState({
    nodes: true,
    context: true,
    relations: false,
  });
  const [newContextName, setNewContextName] = useState("");
  const [isCreatingContext, setIsCreatingContext] = useState(false);
  const [contextError, setContextError] = useState("");
  const [contextSuccess, setContextSuccess] = useState("");

  // Relations & Paths state
  const [newRelationName, setNewRelationName] = useState("");
  const [relations, setRelations] = useState([
    "loves",
    "agent",
    "object",
    "causes",
    "believes",
  ]);
  const [selectedPath, setSelectedPath] = useState("");
  const [pathResult, setPathResult] = useState("");

  // Clear success/error messages after 3 seconds
  useEffect(() => {
    if (contextSuccess || contextError) {
      const timer = setTimeout(() => {
        setContextSuccess("");
        setContextError("");
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [contextSuccess, contextError]);

  const handleDragStart = (e, type) => {
    e.dataTransfer.setData("text/plain", type);
  };

  const toggleSection = (section) => {
    setExpandedSections((prev) => ({
      ...prev,
      [section]: !prev[section],
    }));
  };

  // Enhanced context change with backend sync and visual updates
  const handleContextChange = async (contextName) => {
    console.log(`[Sidebar] Changing context to: ${contextName}`);

    try {
      // Update local state first
      dispatch({ type: "SET_CURRENT_CONTEXT", payload: contextName });

      // Sync with backend
      const result = await runCommand(`set-curr-context ${contextName}`);
      if (result.includes("Error:")) {
        console.warn(`[Sidebar] Backend context change warning: ${result}`);
        setContextError(`Backend warning: ${result}`);
      } else {
        console.log(`[Sidebar] Backend context change successful: ${result}`);
        setContextSuccess(`Context switched to "${contextName}"`);
      }

      // Force visual update by dispatching a refresh event
      window.dispatchEvent(
        new CustomEvent("contextChanged", {
          detail: {
            newContext: contextName,
            previousContext: state.currentContext,
          },
        })
      );
    } catch (error) {
      console.error(`[Sidebar] Error changing context:`, error);
      setContextError(`Failed to change context: ${error.message}`);
    }
  };

  // Enhanced attitude change with backend sync
  const handleAttitudeChange = async (attitudeName) => {
    console.log(`[Sidebar] Changing attitude to: ${attitudeName}`);

    try {
      // Update local state first
      dispatch({ type: "SET_CURRENT_ATTITUDE", payload: attitudeName });

      // Sync with backend
      const result = await runCommand(`set-attitude ${attitudeName}`);
      if (result.includes("Error:")) {
        console.warn(`[Sidebar] Backend attitude change warning: ${result}`);
        setContextError(`Backend warning: ${result}`);
      } else {
        console.log(`[Sidebar] Backend attitude change successful: ${result}`);
        setContextSuccess(`Attitude set to "${attitudeName}"`);
      }

      // Force visual update
      window.dispatchEvent(
        new CustomEvent("attitudeChanged", {
          detail: {
            newAttitude: attitudeName,
            previousAttitude: state.currentAttitude,
          },
        })
      );
    } catch (error) {
      console.error(`[Sidebar] Error changing attitude:`, error);
      setContextError(`Failed to change attitude: ${error.message}`);
    }
  };

  // Enhanced context creation with proper backend sync
  const handleCreateContext = async () => {
    if (!newContextName.trim()) {
      setContextError("Context name cannot be empty");
      return;
    }

    if (state.contexts.includes(newContextName.trim())) {
      setContextError("Context already exists");
      return;
    }

    setIsCreatingContext(true);
    setContextError("");

    try {
      console.log(`[Sidebar] Creating context: ${newContextName.trim()}`);

      // First send to backend - this is the key fix!
      const result = await runCommand(
        `define-context ${newContextName.trim()}`
      );

      // Check if backend succeeded
      if (!result.toLowerCase().includes("error")) {
        // Add to local state only if backend succeeded
        dispatch({ type: "ADD_CONTEXT", payload: newContextName.trim() });
        setContextSuccess(
          `Context "${newContextName.trim()}" created successfully!`
        );
        setNewContextName("");

        console.log(
          `[Sidebar] Context created successfully: ${newContextName.trim()}`
        );
      } else {
        setContextError(`Backend error: ${result}`);
        console.error(`[Sidebar] Backend context creation failed: ${result}`);
      }
    } catch (error) {
      setContextError(`Failed to create context: ${error.message}`);
      console.error(`[Sidebar] Context creation error:`, error);
    } finally {
      setIsCreatingContext(false);
    }
  };

  // NEW: Add relation functionality
  const handleAddRelation = () => {
    if (!newRelationName.trim()) {
      setContextError("Relation name cannot be empty");
      return;
    }

    if (relations.includes(newRelationName.trim())) {
      setContextError("Relation already exists");
      return;
    }

    setRelations((prev) => [...prev, newRelationName.trim()]);
    dispatch({
      type: "ADD_RELATION",
      payload: {
        name: newRelationName.trim(),
        type: "custom",
        context: state.currentContext,
      },
    });
    setNewRelationName("");
    setContextSuccess(`Relation "${newRelationName.trim()}" added!`);
    console.log(`[Sidebar] Added relation: ${newRelationName.trim()}`);
  };

  // NEW: Remove relation functionality
  const handleRemoveRelation = (relationName) => {
    setRelations((prev) => prev.filter((rel) => rel !== relationName));
    dispatch({ type: "REMOVE_RELATION", payload: relationName });
    setContextSuccess(`Relation "${relationName}" removed!`);
    console.log(`[Sidebar] Removed relation: ${relationName}`);
  };

  // NEW: Create path functionality
  const handleCreatePath = () => {
    if (!selectedPath) {
      setContextError("Please select a path type");
      return;
    }

    // Trigger path highlighting on the graph
    window.dispatchEvent(
      new CustomEvent("highlightPath", {
        detail: {
          pathType: selectedPath,
          context: state.currentContext,
        },
      })
    );

    setPathResult(`Path "${selectedPath}" created and highlighted on graph!`);
    setContextSuccess(`Path "${selectedPath}" is now active`);
    console.log(`[Sidebar] Created path: ${selectedPath}`);
  };

  // NEW: Handle path template selection
  const handlePathTemplate = (pathTemplate) => {
    setSelectedPath(pathTemplate.path);

    // Immediately highlight the path
    window.dispatchEvent(
      new CustomEvent("highlightPath", {
        detail: {
          pathType: pathTemplate.path,
          context: state.currentContext,
          name: pathTemplate.name,
        },
      })
    );

    setContextSuccess(`"${pathTemplate.name}" path activated!`);
    console.log(`[Sidebar] Activated path template: ${pathTemplate.name}`);
  };

  // Enhanced highlighting function
  const handleHighlightNodes = () => {
    const newHighlight =
      state.highlightedAttitude === state.currentAttitude
        ? null
        : state.currentAttitude;

    console.log(
      `[Sidebar] Toggling highlight for attitude: ${state.currentAttitude}, new state: ${newHighlight}`
    );

    dispatch({
      type: "SET_HIGHLIGHTED_ATTITUDE",
      payload: newHighlight,
    });

    // Show feedback
    if (newHighlight) {
      setContextSuccess(`Highlighting nodes with "${newHighlight}" attitude`);
    } else {
      setContextSuccess("Cleared attitude highlighting");
    }
  };

  const handleModeChange = async (mode) => {
    dispatch({ type: "SET_CURRENT_MODE", payload: mode });
    try {
      await runCommand(`set-mode-${mode}`);
    } catch (error) {
      console.error("Error setting mode:", error);
    }
  };

  const nodeTypes = [
    {
      type: "PropositionNode",
      icon: Circle,
      color: "bg-blue-500",
      desc: "Beliefs & Facts",
    },
    { type: "ActNode", icon: Target, color: "bg-green-500", desc: "Actions" },
    {
      type: "IndividualNode",
      icon: User,
      color: "bg-yellow-500",
      desc: "Entities",
    },
    {
      type: "RuleNode",
      icon: Zap,
      color: "bg-purple-500",
      desc: "Rules & Logic",
    },
    { type: "AndOr", icon: Diamond, color: "bg-red-500", desc: "And/Or Logic" },
    {
      type: "Thresh",
      icon: Hexagon,
      color: "bg-cyan-500",
      desc: "Threshold Logic",
    },
  ];

  const attitudeIcons = {
    belief: Heart,
    desire: Target,
    intention: Zap,
    fear: Triangle,
    obligation: Square,
    love: Heart,
    // Add more attitudes as needed
  };

  const attitudeColors = {
    belief: "text-blue-500",
    desire: "text-green-500",
    intention: "text-purple-500",
    fear: "text-red-500",
    obligation: "text-yellow-500",
    love: "text-pink-500",
    // Add more attitudes as needed
  };

  // Path templates
  const pathTemplates = [
    {
      name: "Love Relationship",
      path: "agent-loves-object",
      desc: "Find who loves whom",
    },
    {
      name: "Belief Chain",
      path: "believer-believes-proposition",
      desc: "Find who believes what",
    },
    {
      name: "Action Chain",
      path: "agent-performs-action",
      desc: "Find who does what",
    },
    {
      name: "Cause Effect",
      path: "cause-effect-result",
      desc: "Find what causes what",
    },
  ];

  return (
    <div className="w-80 bg-gradient-to-b from-slate-50 to-slate-100 border-l border-slate-200 shadow-xl overflow-y-auto">
      {/* Header */}
      <div className="bg-white border-b border-slate-200 p-4 shadow-sm">
        <div className="flex items-center space-x-2">
          <Settings className="w-5 h-5 text-slate-600" />
          <h2 className="text-lg font-bold text-slate-800">Control Panel</h2>
        </div>
        <p className="text-xs text-slate-500 mt-1">
          Configure your Mind GRAF network
        </p>
      </div>

      <div className="p-4 space-y-6">
        {/* Context Section */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <button
            onClick={() => toggleSection("context")}
            className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center space-x-2">
              <Layers className="w-4 h-4 text-slate-600" />
              <span className="font-medium text-slate-800">
                Context & Attitude
              </span>
            </div>
            {expandedSections.context ? (
              <ChevronDown className="w-4 h-4" />
            ) : (
              <ChevronRight className="w-4 h-4" />
            )}
          </button>

          {expandedSections.context && (
            <div className="p-4 pt-0 space-y-4">
              {/* Context Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Active Context ({state.contexts.length} available)
                </label>
                <select
                  value={state.currentContext}
                  onChange={(e) => handleContextChange(e.target.value)}
                  className="w-full p-3 border border-slate-300 rounded-lg bg-white text-slate-800 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  {state.contexts.map((context) => (
                    <option key={context} value={context}>
                      {context}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-slate-500 mt-1">
                  Current:{" "}
                  <span className="font-medium text-blue-600">
                    {state.currentContext}
                  </span>
                </p>
              </div>

              {/* Create New Context */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Create New Context
                </label>
                <div className="flex space-x-2">
                  <input
                    type="text"
                    value={newContextName}
                    onChange={(e) => setNewContextName(e.target.value)}
                    placeholder="Enter context name"
                    className="flex-1 p-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    onKeyPress={(e) =>
                      e.key === "Enter" && handleCreateContext()
                    }
                  />
                  <button
                    onClick={handleCreateContext}
                    disabled={isCreatingContext || !newContextName.trim()}
                    className="bg-blue-500 hover:bg-blue-600 disabled:bg-slate-400 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1"
                  >
                    {isCreatingContext ? (
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <Plus className="w-4 h-4" />
                    )}
                  </button>
                </div>

                {/* Success/Error Messages */}
                {contextSuccess && (
                  <div className="flex items-center space-x-2 mt-2 text-green-600 text-sm">
                    <CheckCircle className="w-4 h-4" />
                    <span>{contextSuccess}</span>
                  </div>
                )}
                {contextError && (
                  <div className="flex items-center space-x-2 mt-2 text-red-600 text-sm">
                    <AlertCircle className="w-4 h-4" />
                    <span>{contextError}</span>
                  </div>
                )}
              </div>

              {/* Attitude Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Current Attitude ({state.attitudes.length} available)
                </label>
                <div className="grid grid-cols-1 gap-2">
                  {state.attitudes.map((attitude) => {
                    const Icon = attitudeIcons[attitude] || Circle;
                    const isSelected = state.currentAttitude === attitude;
                    const isHighlighted =
                      state.highlightedAttitude === attitude;
                    return (
                      <button
                        key={attitude}
                        onClick={() => handleAttitudeChange(attitude)}
                        className={`
                          flex items-center space-x-3 p-3 rounded-lg border transition-all relative
                          ${
                            isSelected
                              ? "bg-blue-50 border-blue-200 shadow-sm"
                              : "bg-white border-slate-200 hover:border-slate-300"
                          }
                          ${isHighlighted ? "ring-2 ring-green-400" : ""}
                        `}
                      >
                        <Icon
                          className={`w-4 h-4 ${
                            attitudeColors[attitude] || "text-slate-500"
                          }`}
                        />
                        <span
                          className={`text-sm font-medium ${
                            isSelected ? "text-blue-800" : "text-slate-700"
                          }`}
                        >
                          {attitude.charAt(0).toUpperCase() + attitude.slice(1)}
                        </span>
                        {isSelected && (
                          <div className="ml-auto w-2 h-2 bg-blue-500 rounded-full"></div>
                        )}
                        {isHighlighted && (
                          <div className="absolute top-1 right-1 w-2 h-2 bg-green-500 rounded-full"></div>
                        )}
                      </button>
                    );
                  })}
                </div>

                <div className="mt-3 space-y-2">
                  <button
                    onClick={handleHighlightNodes}
                    className={`w-full px-4 py-2 rounded-lg text-sm font-medium transition-all shadow-sm flex items-center justify-center space-x-2 ${
                      state.highlightedAttitude === state.currentAttitude
                        ? "bg-gradient-to-r from-red-500 to-red-600 text-white hover:from-red-600 hover:to-red-700"
                        : "bg-gradient-to-r from-green-500 to-emerald-600 text-white hover:from-green-600 hover:to-emerald-700"
                    }`}
                  >
                    <Filter className="w-4 h-4" />
                    <span>
                      {state.highlightedAttitude === state.currentAttitude
                        ? "Clear Highlight"
                        : `Highlight "${state.currentAttitude}"`}
                    </span>
                  </button>

                  <p className="text-xs text-slate-500 text-center">
                    {state.highlightedAttitude
                      ? `Highlighting: ${state.highlightedAttitude}`
                      : "No attitude highlighted"}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Node Types Section */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <button
            onClick={() => toggleSection("nodes")}
            className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center space-x-2">
              <Circle className="w-4 h-4 text-slate-600" />
              <span className="font-medium text-slate-800">Node Types</span>
            </div>
            {expandedSections.nodes ? (
              <ChevronDown className="w-4 h-4" />
            ) : (
              <ChevronRight className="w-4 h-4" />
            )}
          </button>

          {expandedSections.nodes && (
            <div className="p-4 pt-0">
              <p className="text-xs text-slate-500 mb-3">
                Drag nodes to the canvas to create them
              </p>
              <div className="space-y-2">
                {nodeTypes.map(({ type, icon: Icon, color, desc }) => (
                  <div
                    key={type}
                    draggable
                    onDragStart={(e) => handleDragStart(e, type)}
                    className="group flex items-center space-x-3 p-3 bg-slate-50 border border-slate-200 rounded-lg cursor-move hover:bg-white hover:shadow-md hover:border-slate-300 transition-all"
                  >
                    <div
                      className={`w-8 h-8 ${color} rounded-lg flex items-center justify-center shadow-sm group-hover:shadow-md transition-shadow`}
                    >
                      <Icon className="w-4 h-4 text-white" />
                    </div>
                    <div className="flex-1">
                      <div className="font-medium text-slate-800 text-sm">
                        {type}
                      </div>
                      <div className="text-xs text-slate-500">{desc}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* ENHANCED Relations and Paths Section */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <button
            onClick={() => toggleSection("relations")}
            className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center space-x-2">
              <Route className="w-4 h-4 text-slate-600" />
              <span className="font-medium text-slate-800">
                Relations & Paths
              </span>
            </div>
            {expandedSections.relations ? (
              <ChevronDown className="w-4 h-4" />
            ) : (
              <ChevronRight className="w-4 h-4" />
            )}
          </button>

          {expandedSections.relations && (
            <div className="p-4 pt-0 space-y-4">
              {/* Relations Management */}
              <div className="bg-slate-50 rounded-lg p-3 border">
                <h4 className="text-sm font-medium text-slate-700 mb-2 flex items-center space-x-2">
                  <Settings className="w-4 h-4" />
                  <span>Define Relations</span>
                </h4>
                <div className="flex space-x-2 mb-2">
                  <input
                    type="text"
                    value={newRelationName}
                    onChange={(e) => setNewRelationName(e.target.value)}
                    placeholder="Relation name (e.g., loves, agent)"
                    className="flex-1 p-2 border border-slate-300 rounded text-xs focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    onKeyPress={(e) => e.key === "Enter" && handleAddRelation()}
                  />
                  <button
                    onClick={handleAddRelation}
                    disabled={!newRelationName.trim()}
                    className="bg-blue-500 hover:bg-blue-600 disabled:bg-slate-400 text-white px-3 py-1 rounded text-xs transition-colors"
                  >
                    Add
                  </button>
                </div>
                <div className="space-y-1 max-h-24 overflow-y-auto">
                  {relations.map((rel) => (
                    <div
                      key={rel}
                      className="flex justify-between items-center text-xs bg-white p-2 rounded border"
                    >
                      <span className="text-slate-700 font-medium">{rel}</span>
                      <button
                        onClick={() => handleRemoveRelation(rel)}
                        className="text-red-500 hover:text-red-700 hover:bg-red-50 w-5 h-5 rounded flex items-center justify-center transition-colors"
                      >
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Path Builder */}
              <div className="bg-slate-50 rounded-lg p-3 border">
                <h4 className="text-sm font-medium text-slate-700 mb-2 flex items-center space-x-2">
                  <Route className="w-4 h-4" />
                  <span>Path Builder</span>
                </h4>
                <div className="space-y-2">
                  <select
                    value={selectedPath}
                    onChange={(e) => setSelectedPath(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded text-xs focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">Select path type...</option>
                    <option value="agent-loves-object">
                      agent-loves-object (Find who loves whom)
                    </option>
                    <option value="believer-believes-proposition">
                      believer-believes-proposition (Find who believes what)
                    </option>
                    <option value="agent-performs-action">
                      agent-performs-action (Find who does what)
                    </option>
                    <option value="cause-effect-result">
                      cause-effect-result (Find what causes what)
                    </option>
                  </select>
                  <button
                    onClick={handleCreatePath}
                    disabled={!selectedPath}
                    className="w-full bg-purple-500 hover:bg-purple-600 disabled:bg-slate-400 text-white px-3 py-2 rounded text-xs font-medium transition-colors"
                  >
                    Create & Highlight Path
                  </button>
                  {pathResult && (
                    <p className="text-xs text-purple-600 mt-1">{pathResult}</p>
                  )}
                </div>
              </div>

              {/* Quick Path Templates */}
              <div className="bg-blue-50 rounded-lg p-3 border border-blue-200">
                <h4 className="text-xs font-medium text-blue-800 mb-2 flex items-center space-x-2">
                  <Zap className="w-4 h-4" />
                  <span>Quick Path Templates</span>
                </h4>
                <div className="space-y-1">
                  {pathTemplates.map((pathTemplate) => (
                    <button
                      key={pathTemplate.name}
                      onClick={() => handlePathTemplate(pathTemplate)}
                      className="w-full text-left p-2 bg-white rounded border text-xs hover:bg-blue-100 transition-colors"
                    >
                      <div className="font-medium text-blue-800">
                        {pathTemplate.name}
                      </div>
                      <div className="text-blue-600">{pathTemplate.path}</div>
                      <div className="text-slate-500 text-xs mt-1">
                        {pathTemplate.desc}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Current Relations Display */}
              <button className="w-full bg-gradient-to-r from-blue-500 to-blue-600 text-white px-4 py-3 rounded-lg text-sm font-medium hover:from-blue-600 hover:to-blue-700 transition-all shadow-sm flex items-center justify-center space-x-2">
                <Eye className="w-4 h-4" />
                <span>View All Relations ({relations.length})</span>
              </button>
            </div>
          )}
        </div>

        {/* Enhanced State Info Panel */}
        <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
          <h3 className="font-medium text-sm text-blue-800 mb-2">
            Current State
          </h3>
          <div className="space-y-1 text-xs text-blue-600">
            <div className="flex justify-between">
              <span>Context:</span>
              <span className="font-medium">{state.currentContext}</span>
            </div>
            <div className="flex justify-between">
              <span>Attitude:</span>
              <span className="font-medium">{state.currentAttitude}</span>
            </div>
            <div className="flex justify-between">
              <span>Mode:</span>
              <span className="font-medium">{state.currentMode}</span>
            </div>
            <div className="flex justify-between">
              <span>Highlighting:</span>
              <span className="font-medium">
                {state.highlightedAttitude || "None"}
              </span>
            </div>
            <div className="flex justify-between">
              <span>Relations:</span>
              <span className="font-medium">{relations.length}</span>
            </div>
            <div className="flex justify-between">
              <span>Nodes:</span>
              <span className="font-medium">
                {Object.keys(state.nodes).length}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
