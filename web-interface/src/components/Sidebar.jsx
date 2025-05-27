import React, { useState, useEffect } from "react";
import { useAppState } from "../context/AppStateContext";
import { runCommand } from "../api/runCommand";
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

  const handleContextChange = (contextName) => {
    dispatch({ type: "SET_CURRENT_CONTEXT", payload: contextName });
    // Also send command to backend
    runCommand(`set-curr-context ${contextName}`).catch(console.error);
  };

  const handleAttitudeChange = (attitudeName) => {
    dispatch({ type: "SET_CURRENT_ATTITUDE", payload: attitudeName });
    // Also send command to backend
    runCommand(`set-attitude ${attitudeName}`).catch(console.error);
  };

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
      const result = await runCommand(
        `define-context c{${newContextName.trim()}}`
      );

      // Add to local state
      dispatch({ type: "ADD_CONTEXT", payload: newContextName.trim() });

      setContextSuccess(
        `Context "${newContextName.trim()}" created successfully!`
      );
      setNewContextName("");
    } catch (error) {
      setContextError(`Failed to create context: ${error.message}`);
    } finally {
      setIsCreatingContext(false);
    }
  };

  const handleHighlightNodes = async () => {
    try {
      // This could trigger highlighting nodes of the current attitude in the graph
      console.log(
        `Highlighting nodes for attitude: ${state.currentAttitude} in context: ${state.currentContext}`
      );
      // You can add graph highlighting logic here later
    } catch (error) {
      console.error("Error highlighting nodes:", error);
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
    // Add more attitudes as needed
  };

  const attitudeColors = {
    belief: "text-blue-500",
    desire: "text-green-500",
    intention: "text-purple-500",
    fear: "text-red-500",
    obligation: "text-yellow-500",
    // Add more attitudes as needed
  };

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
                    return (
                      <button
                        key={attitude}
                        onClick={() => handleAttitudeChange(attitude)}
                        className={`
                          flex items-center space-x-3 p-3 rounded-lg border transition-all
                          ${
                            isSelected
                              ? "bg-blue-50 border-blue-200 shadow-sm"
                              : "bg-white border-slate-200 hover:border-slate-300"
                          }
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
                      </button>
                    );
                  })}
                </div>
                <button
                  onClick={handleHighlightNodes}
                  className="w-full mt-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:from-green-600 hover:to-emerald-700 transition-all shadow-sm flex items-center justify-center space-x-2"
                >
                  <Filter className="w-4 h-4" />
                  <span>Highlight Nodes</span>
                </button>
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

        {/* Relations and Paths Section */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <button
            onClick={() => toggleSection("relations")}
            className="w-full flex items-center justify-between p-4 hover:bg-slate-50 transition-colors"
          >
            <div className="flex items-center space-x-2">
              <Zap className="w-4 h-4 text-slate-600" />
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
            <div className="p-4 pt-0 space-y-2">
              <button className="w-full bg-gradient-to-r from-blue-500 to-blue-600 text-white px-4 py-3 rounded-lg text-sm font-medium hover:from-blue-600 hover:to-blue-700 transition-all shadow-sm flex items-center justify-center space-x-2">
                <Eye className="w-4 h-4" />
                <span>
                  View Relations ({Object.keys(state.relations).length})
                </span>
              </button>
              <button className="w-full bg-gradient-to-r from-purple-500 to-purple-600 text-white px-4 py-3 rounded-lg text-sm font-medium hover:from-purple-600 hover:to-purple-700 transition-all shadow-sm flex items-center justify-center space-x-2">
                <Settings className="w-4 h-4" />
                <span>View Paths</span>
              </button>
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div className="bg-gradient-to-r from-slate-800 to-slate-900 rounded-xl p-4 text-white shadow-lg">
          <h3 className="font-medium text-sm mb-3 flex items-center space-x-2">
            <Plus className="w-4 h-4" />
            <span>Quick Actions</span>
          </h3>
          <div className="space-y-2">
            <button
              onClick={() => setNewContextName("")}
              className="w-full bg-white/10 hover:bg-white/20 text-white px-3 py-2 rounded-lg text-sm transition-all backdrop-blur-sm"
            >
              Create Context
            </button>
            <button className="w-full bg-white/10 hover:bg-white/20 text-white px-3 py-2 rounded-lg text-sm transition-all backdrop-blur-sm">
              Define Relation
            </button>
          </div>
        </div>

        {/* State Info Panel */}
        <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
          <h3 className="font-medium text-sm text-blue-800 mb-2">
            Current State
          </h3>
          <div className="space-y-1 text-xs text-blue-600">
            <div>
              Context:{" "}
              <span className="font-medium">{state.currentContext}</span>
            </div>
            <div>
              Attitude:{" "}
              <span className="font-medium">{state.currentAttitude}</span>
            </div>
            <div>
              Mode: <span className="font-medium">{state.currentMode}</span>
            </div>
            <div>
              Nodes:{" "}
              <span className="font-medium">
                {Object.keys(state.nodes).length}
              </span>
            </div>
            <div>
              Relations:{" "}
              <span className="font-medium">
                {Object.keys(state.relations).length}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
