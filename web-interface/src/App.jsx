// src/App.jsx
import React from "react";
import { AppStateProvider } from "./context/AppStateContext";
import { ModeProvider } from "./context/ModeContext";
import SetupFlow from "./components/SetupFlow";
import MainApp from "./components/MainApp";

export default function App() {
  return (
    <AppStateProvider>
      <ModeProvider>
        <div className="min-h-screen bg-slate-100">
          <SetupFlow />
          <MainApp />
        </div>
      </ModeProvider>
    </AppStateProvider>
  );
}
