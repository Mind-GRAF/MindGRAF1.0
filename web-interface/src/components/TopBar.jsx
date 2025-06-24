import React from "react";
import { useMode } from "../context/ModeContext";
import { useAppState } from "../context/AppStateContext";
import {
  Brain,
  Terminal,
  Route,
  Sparkles,
  Settings,
  Database,
} from "lucide-react";

export default function TopBar() {
  const { mode, setMode } = useMode();
  const { state } = useAppState();

  const modes = [
    { key: "draw", label: "DRAW", icon: Brain },
    { key: "trace", label: "TRACE", icon: Route },
    { key: "cli", label: "CLI", icon: Terminal },
  ];

  const getStatusColor = () => {
    if (!state.isSetupComplete) return "bg-yellow-400";
    return "bg-green-400";
  };

  const getStatusText = () => {
    if (!state.isSetupComplete) return "Setup Required";
    return "Ready";
  };

  return (
    <div className="bg-gradient-to-r from-slate-900 via-blue-900 to-slate-900 text-white shadow-xl border-b border-blue-800/50">
      <div className="flex items-center justify-between p-4">
        {/* Logo Section */}
        <div className="flex items-center space-x-3">
          <div className="relative">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-purple-600 rounded-lg flex items-center justify-center shadow-lg">
              <Sparkles className="w-6 h-6 text-white" />
            </div>
            <div
              className={`absolute -top-1 -right-1 w-3 h-3 ${getStatusColor()} rounded-full animate-pulse`}
            ></div>
          </div>
          <div>
            <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
              Mind GRAF
            </h1>
            <p className="text-xs text-blue-300 opacity-75">
              Graphical Reasoner with Attitude Frames
            </p>
          </div>
        </div>

        {/* System Status & Context Info */}
        {state.isSetupComplete && (
          <div className="hidden md:flex items-center space-x-6 text-sm">
            {/* Current Context/Attitude */}
            <div className="flex items-center space-x-4 bg-slate-800/50 px-4 py-2 rounded-lg backdrop-blur-sm border border-slate-700/50">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-blue-400 rounded-full"></div>
                <span className="text-slate-300">Context:</span>
                <span className="text-blue-400 font-medium">
                  {state.currentContext}
                </span>
              </div>
              <div className="w-px h-4 bg-slate-600"></div>
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                <span className="text-slate-300">Attitude:</span>
                <span className="text-green-400 font-medium">
                  {state.currentAttitude}
                </span>
              </div>
            </div>

            {/* Network Stats */}
            <div className="flex items-center space-x-4 bg-slate-800/50 px-4 py-2 rounded-lg backdrop-blur-sm border border-slate-700/50">
              <div className="flex items-center space-x-2">
                <Database className="w-4 h-4 text-slate-400" />
                <span className="text-slate-300">
                  {Object.keys(state.nodes).length} nodes
                </span>
              </div>
              <div className="w-px h-4 bg-slate-600"></div>
              <div className="flex items-center space-x-2">
                <Settings className="w-4 h-4 text-slate-400" />
                <span className="text-slate-300">
                  {state.contexts.length} contexts
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Mode Selection */}
        <div className="flex items-center space-x-2 bg-slate-800/50 p-2 rounded-xl backdrop-blur-sm border border-slate-700/50">
          {modes.map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              onClick={() => setMode(key)}
              className={`
                flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200
                ${
                  mode === key
                    ? "bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg transform scale-105"
                    : "text-slate-300 hover:text-white hover:bg-slate-700/50"
                }
              `}
            >
              <Icon className="w-4 h-4" />
              <span className="hidden sm:inline">{label}</span>
            </button>
          ))}
        </div>

        {/* Status Indicator */}
        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-2 text-sm">
            <div
              className={`w-2 h-2 ${getStatusColor()} rounded-full animate-pulse`}
            ></div>
            <span className="text-slate-300 hidden sm:inline">
              {getStatusText()}
            </span>
          </div>
        </div>
      </div>

      {/* Mobile Context Bar */}
      {state.isSetupComplete && (
        <div className="md:hidden px-4 pb-3 border-t border-slate-700/50">
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center space-x-3">
              <span className="text-slate-400">Context:</span>
              <span className="text-blue-400 font-medium">
                {state.currentContext}
              </span>
              <span className="text-slate-400">Attitude:</span>
              <span className="text-green-400 font-medium">
                {state.currentAttitude}
              </span>
            </div>
            <div className="flex items-center space-x-3 text-slate-400">
              <span>{Object.keys(state.nodes).length} nodes</span>
              <span>{state.contexts.length} contexts</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
