// src/components/MainApp.jsx
import React from "react";
import { useAppState } from "../context/AppStateContext";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import GraphCanvas from "./GraphCanvas";
import CommandConsole from "./CommandConsole";
import { useMode } from "../context/ModeContext";
import { Route, BarChart3 } from "lucide-react";

export default function MainApp() {
  const { isSetupComplete } = useAppState();
  const { mode } = useMode();

  // Don't render main app until setup is complete
  if (!isSetupComplete) {
    return null;
  }

  const renderMainContent = () => {
    switch (mode) {
      case "cli":
        return (
          <div className="flex-1 bg-gradient-to-br from-slate-50 to-slate-100 overflow-auto">
            <CommandConsole />
          </div>
        );
      case "trace":
        return (
          <div className="flex-1 flex items-center justify-center bg-gradient-to-br from-green-50 via-white to-emerald-50">
            <div className="text-center space-y-6 p-8">
              <div className="w-24 h-24 bg-gradient-to-br from-green-500 to-emerald-600 rounded-2xl flex items-center justify-center mx-auto shadow-lg">
                <Route className="w-12 h-12 text-white" />
              </div>
              <div className="space-y-2">
                <h2 className="text-2xl font-bold text-slate-800">
                  Trace Mode
                </h2>
                <p className="text-slate-600 max-w-md">
                  Trace inference paths and reasoning chains through your
                  network. Visualize how conclusions are reached and beliefs are
                  supported.
                </p>
              </div>
              <div className="bg-white/60 backdrop-blur-sm rounded-xl p-6 border border-white/20 shadow-lg">
                <p className="text-sm text-slate-500 mb-4">Future Work</p>
                <div className="flex items-center justify-center space-x-2">
                  <div className="w-3 h-3 bg-green-400 rounded-full animate-pulse"></div>
                  <div className="w-8 h-1 bg-green-200 rounded-full"></div>
                  <div
                    className="w-3 h-3 bg-emerald-400 rounded-full animate-pulse"
                    style={{ animationDelay: "0.5s" }}
                  ></div>
                  <div className="w-8 h-1 bg-emerald-200 rounded-full"></div>
                  <div
                    className="w-3 h-3 bg-green-500 rounded-full animate-pulse"
                    style={{ animationDelay: "1s" }}
                  ></div>
                </div>
              </div>
            </div>
          </div>
        );
      case "draw":
      default:
        return (
          <div className="flex flex-col flex-1 bg-gradient-to-br from-slate-50 to-white">
            <GraphCanvas />
          </div>
        );
    }
  };

  return (
    <div className="flex flex-col h-screen w-screen bg-slate-100 overflow-hidden">
      <TopBar />
      <div className="flex flex-1 min-h-0">
        {renderMainContent()}
        <Sidebar />
      </div>

      {/* Status Bar */}
      <div className="bg-slate-800 text-slate-300 px-4 py-2 text-xs flex items-center justify-between border-t border-slate-700">
        <div className="flex items-center space-x-4">
          <span className="flex items-center space-x-2">
            <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
            <span>Mind GRAF Ready</span>
          </span>
          <span>Mode: {mode.toUpperCase()}</span>
        </div>
        <div className="flex items-center space-x-4">
          <span>Backend: Connected</span>
          <span>Version: 1.0.0</span>
        </div>
      </div>
    </div>
  );
}
