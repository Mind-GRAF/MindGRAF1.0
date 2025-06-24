// src/context/AppStateContext.jsx
import React, { createContext, useContext, useReducer, useEffect } from "react";
import { runCommand } from "../api/runCommand";
const AppStateContext = createContext();

const initialState = {
  // System setup state
  isSetupComplete: false,
  setupStep: "attitudes", // 'attitudes' -> 'consistent' -> 'conjunction' -> 'consequence' -> 'telescopable' -> 'uvbr' -> 'complete'

  // Mind GRAF core state
  attitudes: ["belief"], // Always start with belief
  contexts: ["default"], // Always start with default context
  consistentAttitudes: [],
  conjunctionAttitudes: [],
  consequenceAttitudes: [],
  telescopableAttitudes: [],
  uvbrEnabled: false,
  cacheEnabled: false,
  automaticHandling: false,
  mergeFunctionNumber: 1,

  // Current user selections
  currentContext: "default",
  currentAttitude: "belief",
  currentMode: 3,

  // Network data
  nodes: {}, // { nodeId: { id, label, type, context, attitude, position } }
  edges: {}, // { edgeId: { id, source, target, label } }
  relations: {}, // { relationName: { name, type, limit, path } }

  // Drawing state
  drawingData: null, // Cytoscape graph data for persistence

  // Session info
  lastSaved: null,
  // Highlighted state
  highlightedAttitude: null,
};

function appStateReducer(state, action) {
  switch (action.type) {
    // Setup actions
    case "SET_ATTITUDES":
      const newAttitudes = action.payload.includes("belief")
        ? action.payload
        : ["belief", ...action.payload];
      return {
        ...state,
        attitudes: newAttitudes,
        currentAttitude: newAttitudes[0],
      };

    case "SET_CONSISTENT_ATTITUDES":
      return {
        ...state,
        consistentAttitudes: action.payload,
      };

    case "SET_CONJUNCTION_ATTITUDES":
      return {
        ...state,
        conjunctionAttitudes: action.payload,
      };

    case "SET_CONSEQUENCE_ATTITUDES":
      return {
        ...state,
        consequenceAttitudes: action.payload,
      };

    case "SET_TELESCOPABLE_ATTITUDES":
      return {
        ...state,
        telescopableAttitudes: action.payload,
      };

    case "SET_UVBR":
      return {
        ...state,
        uvbrEnabled: action.payload,
      };

    case "SET_CACHE":
      return {
        ...state,
        cacheEnabled: action.payload,
      };

    case "SET_AUTOMATIC_HANDLING":
      return {
        ...state,
        automaticHandling: action.payload,
      };

    case "SET_MERGE_FUNCTION":
      return {
        ...state,
        mergeFunctionNumber: action.payload,
      };

    case "COMPLETE_SETUP":
      return {
        ...state,
        isSetupComplete: true,
        setupStep: "complete",
      };

    case "SET_SETUP_STEP":
      return {
        ...state,
        setupStep: action.payload,
      };

    // Context and attitude actions
    case "ADD_CONTEXT":
      // If we only have "default" and we're adding a real context, replace it
      if (state.contexts.length === 1 && state.contexts[0] === "default") {
        return {
          ...state,
          contexts: [action.payload],
          currentContext: action.payload, // Auto-switch to the new context
        };
      }

      // Otherwise, add normally (no duplicates)
      if (state.contexts.includes(action.payload)) {
        return state;
      }

      return {
        ...state,
        contexts: [...state.contexts, action.payload],
      };

    case "SET_CURRENT_CONTEXT":
      return {
        ...state,
        currentContext: action.payload,
      };

    case "SET_CURRENT_ATTITUDE":
      return {
        ...state,
        currentAttitude: action.payload,
      };

    case "SET_CURRENT_MODE":
      return {
        ...state,
        currentMode: action.payload,
      };

    case "SET_HIGHLIGHTED_ATTITUDE":
      return {
        ...state,
        highlightedAttitude: action.payload,
      };

    // Node actions
    case "ADD_NODE":
      return {
        ...state,
        nodes: {
          ...state.nodes,
          [action.payload.id]: {
            ...action.payload,
            createdAt: new Date().toISOString(),
          },
        },
      };

    case "UPDATE_NODE":
      const existingNode = state.nodes[action.payload.id];
      if (!existingNode) return state;

      return {
        ...state,
        nodes: {
          ...state.nodes,
          [action.payload.id]: {
            ...existingNode,
            ...action.payload,
            updatedAt: new Date().toISOString(),
          },
        },
      };

    case "REMOVE_NODE":
      const { [action.payload]: removed, ...remainingNodes } = state.nodes;
      return {
        ...state,
        nodes: remainingNodes,
      };

    // Edge actions
    case "ADD_EDGE":
      return {
        ...state,
        edges: {
          ...state.edges,
          [action.payload.id]: {
            ...action.payload,
            createdAt: new Date().toISOString(),
          },
        },
      };

    case "UPDATE_EDGE":
      const existingEdge = state.edges[action.payload.id];
      if (!existingEdge) return state;

      return {
        ...state,
        edges: {
          ...state.edges,
          [action.payload.id]: {
            ...existingEdge,
            ...action.payload,
            updatedAt: new Date().toISOString(),
          },
        },
      };

    case "REMOVE_EDGE":
      const { [action.payload]: removedEdge, ...remainingEdges } = state.edges;
      return {
        ...state,
        edges: remainingEdges,
      };

    case "UPDATE_ALL_NODES_CONTEXT":
      const updatedNodes = {};
      Object.keys(state.nodes).forEach((nodeId) => {
        updatedNodes[nodeId] = {
          ...state.nodes[nodeId],
          context: action.payload,
          updatedAt: new Date().toISOString(),
        };
      });
      return {
        ...state,
        nodes: updatedNodes,
      };

    case "UPDATE_NODE_ATTITUDES_FOR_CONTEXT":
      const { nodeId, context, attitudes } = action.payload;
      const node = state.nodes[nodeId];
      if (!node || node.type !== "PropositionNode") return state;

      return {
        ...state,
        nodes: {
          ...state.nodes,
          [nodeId]: {
            ...node,
            // Store attitudes per context: { contextName: [attitudes] }
            attitudesByContext: {
              ...node.attitudesByContext,
              [context]: attitudes,
            },
            // Current attitude is based on current context
            attitude:
              attitudes.length > 0 ? attitudes[0] : state.currentAttitude,
            updatedAt: new Date().toISOString(),
          },
        },
      };

    // Relation actions
    case "ADD_RELATION":
      return {
        ...state,
        relations: {
          ...state.relations,
          [action.payload.name]: {
            ...action.payload,
            createdAt: new Date().toISOString(),
          },
        },
      };

    // Drawing state actions
    case "SAVE_DRAWING_DATA":
      return {
        ...state,
        drawingData: action.payload,
        lastSaved: new Date().toISOString(),
      };

    // Bulk state operations
    case "LOAD_STATE":
      return {
        ...state,
        ...action.payload,
        lastSaved: new Date().toISOString(),
      };

    case "RESET_STATE":
      return {
        ...initialState,
        isSetupComplete: state.isSetupComplete, // Keep setup status
        attitudes: state.attitudes,
        contexts: state.contexts,
      };

    default:
      console.warn("Unknown action type:", action.type);
      return state;
  }
}

export function AppStateProvider({ children }) {
  const [state, dispatch] = useReducer(appStateReducer, initialState);

  // Auto-save to localStorage whenever state changes
  useEffect(() => {
    try {
      const stateToSave = {
        ...state,
        // Don't save drawing data to localStorage (too large)
        drawingData: null,
      };
      localStorage.setItem("mindgraf-state", JSON.stringify(stateToSave));
    } catch (error) {
      console.error("Failed to save state to localStorage:", error);
    }
  }, [state]);

  // Load from localStorage on mount
  useEffect(() => {
    try {
      const saved = localStorage.getItem("mindgraf-state");
      if (saved) {
        const parsedState = JSON.parse(saved);
        // Only load if it's valid
        if (parsedState && typeof parsedState === "object") {
          dispatch({ type: "LOAD_STATE", payload: parsedState });
        }
      }
    } catch (error) {
      console.error("Failed to load saved state:", error);
      // Clear corrupted data
      localStorage.removeItem("mindgraf-state");
    }
  }, []);

  useEffect(() => {
    // Validate setup completion by checking backend sync
    if (state.isSetupComplete) {
      runCommand("get-attitudes")
        .then((result) => {
          // Verify backend has the same attitudes
          console.log("Backend attitudes:", result);
        })
        .catch(console.error);

      // Set to mode 3 as expected
      runCommand("set-mode-3").catch(console.error);
    }
  }, [state.isSetupComplete]);

  // Helper function to get nodes by context and attitude
  const getNodesByContextAndAttitude = (
    context = state.currentContext,
    attitude = state.currentAttitude
  ) => {
    return Object.values(state.nodes).filter(
      (node) => node.context === context && node.attitude === attitude
    );
  };

  // Helper function to check if a context exists
  const contextExists = (contextName) => {
    return state.contexts.includes(contextName);
  };

  // Helper function to check if an attitude exists
  const attitudeExists = (attitudeName) => {
    return state.attitudes.includes(attitudeName);
  };

  // Enhanced context value with helper functions
  const contextValue = {
    state,
    dispatch,
    // Helper functions
    getNodesByContextAndAttitude,
    contextExists,
    attitudeExists,
    // Quick accessors
    isSetupComplete: state.isSetupComplete,
    currentContext: state.currentContext,
    currentAttitude: state.currentAttitude,
    currentMode: state.currentMode,
    nodeCount: Object.keys(state.nodes).length,
    edgeCount: Object.keys(state.edges).length,
    relationCount: Object.keys(state.relations).length,
  };

  return (
    <AppStateContext.Provider value={contextValue}>
      {children}
    </AppStateContext.Provider>
  );
}

// Custom hook to use the app state
export function useAppState() {
  const context = useContext(AppStateContext);
  if (!context) {
    throw new Error("useAppState must be used within AppStateProvider");
  }
  return context;
}

// Action creators for common operations
export const actions = {
  // Setup actions
  setAttitudes: (attitudes) => ({ type: "SET_ATTITUDES", payload: attitudes }),
  setConsistentAttitudes: (attitudes) => ({
    type: "SET_CONSISTENT_ATTITUDES",
    payload: attitudes,
  }),
  completeSetup: () => ({ type: "COMPLETE_SETUP" }),

  // Context actions
  addContext: (contextName) => ({ type: "ADD_CONTEXT", payload: contextName }),
  setCurrentContext: (contextName) => ({
    type: "SET_CURRENT_CONTEXT",
    payload: contextName,
  }),
  setCurrentAttitude: (attitudeName) => ({
    type: "SET_CURRENT_ATTITUDE",
    payload: attitudeName,
  }),

  // Node actions
  addNode: (node) => ({ type: "ADD_NODE", payload: node }),
  updateNode: (node) => ({ type: "UPDATE_NODE", payload: node }),
  removeNode: (nodeId) => ({ type: "REMOVE_NODE", payload: nodeId }),

  // Edge actions
  addEdge: (edge) => ({ type: "ADD_EDGE", payload: edge }),
  updateEdge: (edge) => ({ type: "UPDATE_EDGE", payload: edge }),

  // Relation actions
  addRelation: (relation) => ({ type: "ADD_RELATION", payload: relation }),

  // Drawing actions
  saveDrawingData: (data) => ({ type: "SAVE_DRAWING_DATA", payload: data }),
};
