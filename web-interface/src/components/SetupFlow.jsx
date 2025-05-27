// src/components/SetupFlow.jsx
import React, { useState } from "react";
import { useAppState } from "../context/AppStateContext";
import { Settings, ArrowRight, Check, Brain, Sparkles } from "lucide-react";

export default function SetupFlow() {
  const { state, dispatch } = useAppState();
  const [input, setInput] = useState("");

  const steps = [
    {
      key: "attitudes",
      title: "Set Attitudes",
      description: "Define the mental attitudes for your Mind GRAF system",
    },
    {
      key: "consistent",
      title: "Consistent Attitudes",
      description: "Specify which attitudes should be consistent",
    },
    {
      key: "conjunction",
      title: "Conjunction Attitudes",
      description: "Set attitudes closed under conjunction",
    },
    {
      key: "consequence",
      title: "Consequence Attitudes",
      description: "Set attitudes closed under consequence",
    },
    {
      key: "telescopable",
      title: "Telescopable Attitudes",
      description: "Define telescopable attitudes",
    },
    {
      key: "uvbr",
      title: "UVBR Setting",
      description: "Enable or disable Unique Variable Binding Rule",
    },
  ];

  const currentStepIndex = steps.findIndex(
    (step) => step.key === state.setupStep
  );
  const currentStep = steps[currentStepIndex];

  const handleSubmit = (e) => {
    e.preventDefault();

    switch (state.setupStep) {
      case "attitudes":
        if (input.toLowerCase() === "n") {
          // User chose to proceed with just 'belief'
          dispatch({ type: "SET_ATTITUDES", payload: ["belief"] });
        } else {
          const attitudes = input
            .split(",")
            .map((s) => s.trim())
            .filter((s) => s);
          if (attitudes.length === 0) attitudes.push("belief");
          dispatch({
            type: "SET_ATTITUDES",
            payload: ["belief", ...attitudes.filter((a) => a !== "belief")],
          });
        }
        dispatch({ type: "SET_SETUP_STEP", payload: "consistent" });
        break;

      case "consistent":
        // Parse consistent attitudes (e.g., "{belief,desire} {intention,fear}")
        const consistentSets = input.match(/\{[^}]+\}/g) || [];
        const parsed = consistentSets.map((set) =>
          set
            .slice(1, -1)
            .split(",")
            .map((s) => s.trim())
        );
        dispatch({ type: "SET_CONSISTENT_ATTITUDES", payload: parsed });
        dispatch({ type: "SET_SETUP_STEP", payload: "conjunction" });
        break;

      case "conjunction":
        const conjunctionAtt = input
          .split(",")
          .map((s) => s.trim())
          .filter((s) => s);
        dispatch({
          type: "SET_CONJUNCTION_ATTITUDES",
          payload: conjunctionAtt,
        });
        dispatch({ type: "SET_SETUP_STEP", payload: "consequence" });
        break;

      case "consequence":
        const consequenceAtt = input
          .split(",")
          .map((s) => s.trim())
          .filter((s) => s);
        dispatch({
          type: "SET_CONSEQUENCE_ATTITUDES",
          payload: consequenceAtt,
        });
        dispatch({ type: "SET_SETUP_STEP", payload: "telescopable" });
        break;

      case "telescopable":
        const telescopableAtt = input
          .split(",")
          .map((s) => s.trim())
          .filter((s) => s);
        dispatch({
          type: "SET_TELESCOPABLE_ATTITUDES",
          payload: telescopableAtt,
        });
        dispatch({ type: "SET_SETUP_STEP", payload: "uvbr" });
        break;

      case "uvbr":
        const uvbrEnabled =
          input.toLowerCase() === "on" || input.toLowerCase() === "yes";
        dispatch({ type: "SET_UVBR", payload: uvbrEnabled });
        dispatch({ type: "COMPLETE_SETUP" });
        break;
    }

    setInput("");
  };

  const skipStep = () => {
    const nextStepIndex = currentStepIndex + 1;
    if (nextStepIndex < steps.length) {
      dispatch({ type: "SET_SETUP_STEP", payload: steps[nextStepIndex].key });
    } else {
      dispatch({ type: "COMPLETE_SETUP" });
    }
  };

  if (state.isSetupComplete) {
    return null;
  }

  return (
    <div className="fixed inset-0 bg-gradient-to-br from-slate-900 via-blue-900 to-purple-900 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full p-8 border border-white/20">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <Brain className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-slate-800 mb-2 flex items-center justify-center gap-2">
            <Sparkles className="w-8 h-8 text-purple-500" />
            Mind GRAF Setup
          </h1>
          <p className="text-slate-600">
            Configure your artificial mind before you begin
          </p>
        </div>

        {/* Progress */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            {steps.map((step, index) => (
              <div key={step.key} className="flex items-center">
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-all ${
                    index < currentStepIndex
                      ? "bg-green-500 text-white shadow-lg"
                      : index === currentStepIndex
                      ? "bg-blue-500 text-white shadow-lg animate-pulse"
                      : "bg-slate-200 text-slate-600"
                  }`}
                >
                  {index < currentStepIndex ? (
                    <Check className="w-4 h-4" />
                  ) : (
                    index + 1
                  )}
                </div>
                {index < steps.length - 1 && (
                  <div
                    className={`w-12 h-1 mx-2 transition-all ${
                      index < currentStepIndex ? "bg-green-500" : "bg-slate-200"
                    }`}
                  />
                )}
              </div>
            ))}
          </div>
          <div className="text-center">
            <h2 className="text-xl font-semibold text-slate-800">
              {currentStep.title}
            </h2>
            <p className="text-slate-600 text-sm mt-1">
              {currentStep.description}
            </p>
          </div>
        </div>

        {/* Input Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              {getInputLabel(state.setupStep)}
            </label>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              className="w-full p-4 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              placeholder={getInputPlaceholder(state.setupStep)}
              autoFocus
            />
            <p className="text-xs text-slate-500 mt-2">
              {getInputHint(state.setupStep)}
            </p>
          </div>

          <div className="flex gap-4">
            <button
              type="button"
              onClick={skipStep}
              className="flex-1 bg-slate-200 hover:bg-slate-300 text-slate-700 px-6 py-3 rounded-xl font-medium transition-colors"
            >
              Skip
            </button>
            <button
              type="submit"
              className="flex-1 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white px-6 py-3 rounded-xl font-medium transition-all flex items-center justify-center gap-2 shadow-lg"
            >
              Continue
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </form>

        {/* Current Progress Info */}
        <div className="mt-6 p-4 bg-slate-50 rounded-xl border border-slate-200">
          <div className="text-sm text-slate-600">
            <strong>Progress:</strong> Step {currentStepIndex + 1} of{" "}
            {steps.length}
          </div>
          {state.attitudes.length > 1 && (
            <div className="text-sm text-slate-600 mt-1">
              <strong>Attitudes defined:</strong> {state.attitudes.join(", ")}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function getInputLabel(step) {
  switch (step) {
    case "attitudes":
      return 'Enter Attitudes (comma-separated) or "N" to proceed with defaults';
    case "consistent":
      return "Enter Consistent Attitude Sets";
    case "conjunction":
      return "Enter Conjunction Attitudes";
    case "consequence":
      return "Enter Consequence Attitudes";
    case "telescopable":
      return "Enter Telescopable Attitudes";
    case "uvbr":
      return "Enable UVBR?";
    default:
      return "Input";
  }
}

function getInputPlaceholder(step) {
  switch (step) {
    case "attitudes":
      return 'desire, intention, fear, obligation (or just "N")';
    case "consistent":
      return "{belief,desire} {intention,fear}";
    case "conjunction":
      return "belief, intention";
    case "consequence":
      return "belief, desire";
    case "telescopable":
      return "belief, intention";
    case "uvbr":
      return "on/off or yes/no";
    default:
      return "";
  }
}

function getInputHint(step) {
  switch (step) {
    case "attitudes":
      return 'Define the mental attitudes your artificial mind will use. "belief" is always included. Enter "N" to use defaults.';
    case "consistent":
      return "Use curly braces {} to group attitudes that should be consistent with each other.";
    case "conjunction":
      return "Attitudes that should be closed under conjunction (AND operations).";
    case "consequence":
      return "Attitudes that should be closed under consequence (IF-THEN operations).";
    case "telescopable":
      return "Attitudes that support telescoping operations.";
    case "uvbr":
      return "Unique Variable Binding Rule - prevents variable naming conflicts.";
    default:
      return "";
  }
}
