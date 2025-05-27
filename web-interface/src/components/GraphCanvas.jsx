import React, { useEffect, useRef, useState } from "react";
import { useAppState } from "../context/AppStateContext";
import { runCommand } from "../api/runCommand";
import cytoscape from "cytoscape";
import {
  Trash2,
  Link,
  X,
  Maximize2,
  RotateCcw,
  Play,
  Pause,
  Info,
  Layers,
  Zap,
  Save,
  FolderOpen,
  Download,
  Upload,
  Settings,
  Eye,
  EyeOff,
} from "lucide-react";

export default function GraphCanvas() {
  const { state, dispatch } = useAppState();
  const containerRef = useRef(null);
  const cyRef = useRef(null);

  // State for graph management
  const [nodeCount, setNodeCount] = useState(0);
  const [edgeCount, setEdgeCount] = useState(0);
  const [edgeMode, setEdgeMode] = useState(false);
  const [selectedNodeId, setSelectedNodeId] = useState(null);
  const [hoveredNode, setHoveredNode] = useState(null);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showNodeDetails, setShowNodeDetails] = useState(true);
  const [autoSave, setAutoSave] = useState(true);

  // State for node editing
  const [editingNode, setEditingNode] = useState(null);
  const [nodeLabel, setNodeLabel] = useState("");
  const [nodeContext, setNodeContext] = useState("");
  const [nodeAttitude, setNodeAttitude] = useState("");

  // State for edge editing
  const [selectedEdgeId, setSelectedEdgeId] = useState(null);
  const [edgeLabel, setEdgeLabel] = useState("");

  // Enhanced Cytoscape styles with modern aesthetic
  const cytoscapeStyles = [
    {
      selector: "node",
      style: {
        label: "data(label)",
        "text-valign": "center",
        "text-halign": "center",
        "text-wrap": "wrap",
        "text-max-width": "80px",
        "font-size": "11px",
        "font-weight": "600",
        "font-family": "Inter, system-ui, sans-serif",
        "border-width": 3,
        padding: "12px",
        "text-outline-width": 2,
        "text-outline-color": "white",
        "text-outline-opacity": 0.8,
        "shadow-blur": 15,
        "shadow-color": "#000",
        "shadow-opacity": 0.2,
        "shadow-offset-x": 2,
        "shadow-offset-y": 2,
      },
    },
    // Enhanced node type styles with context indicators
    {
      selector: 'node[type="PropositionNode"]',
      style: {
        "background-color": "#3b82f6",
        "background-image": "linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)",
        shape: "ellipse",
        "border-color": "#1e40af",
        width: "70px",
        height: "70px",
      },
    },
    {
      selector: 'node[type="ActNode"]',
      style: {
        "background-color": "#10b981",
        "background-image": "linear-gradient(135deg, #10b981 0%, #047857 100%)",
        shape: "ellipse",
        "border-color": "#065f46",
        width: "70px",
        height: "60px",
      },
    },
    {
      selector: 'node[type="IndividualNode"]',
      style: {
        "background-color": "#f59e0b",
        "background-image": "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)",
        shape: "ellipse",
        "border-color": "#b45309",
        width: "70px",
        height: "70px",
      },
    },
    {
      selector: 'node[type="RuleNode"]',
      style: {
        "background-color": "#8b5cf6",
        "background-image": "linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)",
        shape: "ellipse",
        "border-color": "#6d28d9",
        width: "80px",
        height: "80px",
      },
    },
    {
      selector: 'node[type="AndOr"]',
      style: {
        "background-color": "#ef4444",
        "background-image": "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)",
        shape: "diamond",
        "border-color": "#b91c1c",
        width: "75px",
        height: "75px",
      },
    },
    {
      selector: 'node[type="Thresh"]',
      style: {
        "background-color": "#06b6d4",
        "background-image": "linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)",
        shape: "hexagon",
        "border-color": "#0e7490",
        width: "75px",
        height: "75px",
      },
    },
    // Attitude-based border styles
    {
      selector: 'node[attitude="belief"]',
      style: {
        "border-color": "#3b82f6",
        "border-width": 4,
      },
    },
    {
      selector: 'node[attitude="desire"]',
      style: {
        "border-color": "#10b981",
        "border-width": 4,
      },
    },
    {
      selector: 'node[attitude="intention"]',
      style: {
        "border-color": "#8b5cf6",
        "border-width": 4,
      },
    },
    {
      selector: 'node[attitude="fear"]',
      style: {
        "border-color": "#ef4444",
        "border-width": 4,
      },
    },
    {
      selector: 'node[attitude="obligation"]',
      style: {
        "border-color": "#f59e0b",
        "border-width": 4,
      },
    },
    // Enhanced edge styles
    {
      selector: "edge",
      style: {
        "curve-style": "bezier",
        width: 4,
        "line-color": "#64748b",
        "line-gradient-stop-colors": "#64748b #374151",
        "target-arrow-color": "#374151",
        "target-arrow-shape": "triangle",
        "arrow-scale": 2,
        label: "data(label)",
        "font-size": "10px",
        "font-weight": "600",
        "font-family": "Inter, system-ui, sans-serif",
        "text-rotation": "autorotate",
        "text-margin-y": -12,
        "text-background-color": "white",
        "text-background-opacity": 0.9,
        "text-background-padding": "3px",
        "text-border-width": 1,
        "text-border-color": "#e2e8f0",
        "text-border-opacity": 0.5,
        "shadow-blur": 8,
        "shadow-color": "#000",
        "shadow-opacity": 0.1,
      },
    },
    // Enhanced interaction styles
    {
      selector: ".selected",
      style: {
        "border-width": 5,
        "border-color": "#ef4444",
        "border-style": "solid",
        "shadow-blur": 25,
        "shadow-color": "#ef4444",
        "shadow-opacity": 0.6,
      },
    },
    {
      selector: ".highlighted",
      style: {
        "border-width": 5,
        "border-color": "#22c55e",
        "border-style": "dashed",
        "shadow-blur": 25,
        "shadow-color": "#22c55e",
        "shadow-opacity": 0.4,
      },
    },
    {
      selector: ".hovered",
      style: {
        "border-width": 4,
        "border-color": "#f97316",
        "border-style": "solid",
        "shadow-blur": 20,
        "shadow-color": "#f97316",
        "shadow-opacity": 0.5,
        transform: "scale(1.1)",
      },
    },
    // Context filtering styles
    {
      selector: ".dimmed",
      style: {
        opacity: 0.3,
      },
    },
  ];

  // Initialize Cytoscape
  useEffect(() => {
    if (containerRef.current && !cyRef.current) {
      console.log("Initializing Cytoscape with AppState integration");

      cyRef.current = cytoscape({
        container: containerRef.current,
        style: cytoscapeStyles,
        layout: {
          name: "preset",
          animate: true,
          animationDuration: 500,
        },
        wheelSensitivity: 0.1,
        minZoom: 0.3,
        maxZoom: 3,
      });

      // Load saved graph data if available
      loadGraphFromState();
    }

    return () => {
      if (cyRef.current) {
        cyRef.current.destroy();
        cyRef.current = null;
      }
    };
  }, []);

  // Auto-save graph state when it changes
  useEffect(() => {
    if (cyRef.current && autoSave) {
      const saveTimer = setTimeout(() => {
        saveGraphToState();
      }, 1000); // Save 1 second after changes

      return () => clearTimeout(saveTimer);
    }
  }, [nodeCount, edgeCount, autoSave]);

  // Load graph data from AppState
  const loadGraphFromState = () => {
    if (!cyRef.current || !state.drawingData) return;

    try {
      const { nodes, edges } = state.drawingData;

      // Clear existing elements
      cyRef.current.elements().remove();

      // Add nodes
      if (nodes) {
        nodes.forEach((nodeData) => {
          cyRef.current.add({
            group: "nodes",
            data: nodeData.data,
            position: nodeData.position,
          });
        });
        setNodeCount(nodes.length);
      }

      // Add edges
      if (edges) {
        edges.forEach((edgeData) => {
          cyRef.current.add({
            group: "edges",
            data: edgeData.data,
          });
        });
        setEdgeCount(edges.length);
      }

      console.log("Graph loaded from state");
    } catch (error) {
      console.error("Error loading graph from state:", error);
    }
  };

  // Save graph data to AppState
  const saveGraphToState = () => {
    if (!cyRef.current) return;

    try {
      const nodes = cyRef.current.nodes().map((node) => ({
        data: node.data(),
        position: node.position(),
      }));

      const edges = cyRef.current.edges().map((edge) => ({
        data: edge.data(),
      }));

      const graphData = { nodes, edges };

      dispatch({
        type: "SAVE_DRAWING_DATA",
        payload: graphData,
      });

      console.log("Graph saved to state");
    } catch (error) {
      console.error("Error saving graph to state:", error);
    }
  };

  // Set up event handlers
  useEffect(() => {
    if (!cyRef.current) return;

    const nodeClickHandler = function (evt) {
      const nodeId = this.id();
      cyRef.current.elements().removeClass("selected");

      if (edgeMode) {
        if (!selectedNodeId) {
          setSelectedNodeId(nodeId);
          this.addClass("selected");
        } else if (selectedNodeId !== nodeId) {
          const edgeId = `edge-${Date.now()}`;
          cyRef.current.add({
            group: "edges",
            data: {
              id: edgeId,
              source: selectedNodeId,
              target: nodeId,
              label: "relation",
            },
          });
          setEdgeMode(false);
          setSelectedNodeId(null);
          setEdgeCount((prev) => prev + 1);

          // Save to AppState
          const newEdge = {
            id: edgeId,
            source: selectedNodeId,
            target: nodeId,
            label: "relation",
            context: state.currentContext,
            attitude: state.currentAttitude,
          };
          dispatch({ type: "ADD_EDGE", payload: newEdge });
        }
      } else {
        this.addClass("selected");
        setSelectedNodeId(nodeId);

        // Set editing data
        const nodeData = this.data();
        setEditingNode(nodeId);
        setNodeLabel(nodeData.label || "");
        setNodeContext(nodeData.context || state.currentContext);
        setNodeAttitude(nodeData.attitude || state.currentAttitude);
      }
      evt.originalEvent.stopPropagation();
    };

    const nodeMouseOverHandler = function (evt) {
      const nodeId = this.id();
      setHoveredNode(nodeId);
      this.addClass("hovered");
    };

    const nodeMouseOutHandler = function (evt) {
      setHoveredNode(null);
      this.removeClass("hovered");
    };

    const backgroundClickHandler = function (evt) {
      if (evt.target === cyRef.current) {
        cyRef.current.elements().removeClass("selected");
        setSelectedNodeId(null);
        setEditingNode(null);
      }
    };

    cyRef.current.on("tap", "node", nodeClickHandler);
    cyRef.current.on("mouseover", "node", nodeMouseOverHandler);
    cyRef.current.on("mouseout", "node", nodeMouseOutHandler);
    cyRef.current.on("tap", backgroundClickHandler);

    return () => {
      if (cyRef.current) {
        cyRef.current.removeListener("tap", "node", nodeClickHandler);
        cyRef.current.removeListener("mouseover", "node", nodeMouseOverHandler);
        cyRef.current.removeListener("mouseout", "node", nodeMouseOutHandler);
        cyRef.current.removeListener("tap", backgroundClickHandler);
      }
    };
  }, [
    edgeMode,
    selectedNodeId,
    edgeCount,
    state.currentContext,
    state.currentAttitude,
  ]);

  const handleDrop = (e) => {
    e.preventDefault();
    const nodeType = e.dataTransfer.getData("text/plain");
    if (!nodeType || !cyRef.current) return;

    const rect = containerRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const nodeId = `${nodeType.toLowerCase()}-${Date.now()}`;
    const nodeLabel = `${nodeType}\n${nodeCount + 1}`;

    // Create node in Cytoscape
    cyRef.current.add({
      group: "nodes",
      data: {
        id: nodeId,
        label: nodeLabel,
        type: nodeType,
        context: state.currentContext,
        attitude: state.currentAttitude,
      },
      position: { x, y },
    });

    // Add to AppState
    const newNode = {
      id: nodeId,
      label: nodeLabel,
      type: nodeType,
      context: state.currentContext,
      attitude: state.currentAttitude,
      position: { x, y },
    };
    dispatch({ type: "ADD_NODE", payload: newNode });

    setNodeCount((prev) => prev + 1);

    // Animate the new node
    const newNodeEl = cyRef.current.getElementById(nodeId);
    newNodeEl.style("opacity", 0);
    newNodeEl.animate({
      style: { opacity: 1 },
      duration: 300,
      easing: "ease-out",
    });

    // Send to backend if needed
    try {
      const proposition = `${nodeType}(${nodeId})`;
      runCommand(
        `add-to-context c{${state.currentContext}} a{${state.currentAttitude}} ${proposition}`
      );
    } catch (error) {
      console.error("Error adding to backend:", error);
    }
  };

  const updateNodeDetails = () => {
    if (!editingNode || !cyRef.current) return;

    const node = cyRef.current.getElementById(editingNode);
    if (!node) return;

    // Update node data
    node.data({
      ...node.data(),
      label: nodeLabel,
      context: nodeContext,
      attitude: nodeAttitude,
    });

    // Update in AppState
    const updatedNode = {
      id: editingNode,
      label: nodeLabel,
      context: nodeContext,
      attitude: nodeAttitude,
    };
    dispatch({ type: "UPDATE_NODE", payload: updatedNode });

    setEditingNode(null);
  };

  const deleteSelectedNode = () => {
    if (!selectedNodeId || !cyRef.current) return;

    const node = cyRef.current.getElementById(selectedNodeId);
    if (node) {
      node.remove();
      dispatch({ type: "REMOVE_NODE", payload: selectedNodeId });
      setNodeCount((prev) => prev - 1);
      setSelectedNodeId(null);
      setEditingNode(null);
    }
  };

  const toggleEdgeMode = () => {
    if (edgeMode) {
      cyRef.current.elements().removeClass("selected");
      setSelectedNodeId(null);
    }
    setEdgeMode(!edgeMode);
  };

  const resetGraph = () => {
    if (!cyRef.current) return;

    cyRef.current.elements().remove();
    setNodeCount(0);
    setEdgeCount(0);
    setEdgeMode(false);
    setSelectedNodeId(null);
    setEditingNode(null);

    // Clear from AppState
    dispatch({ type: "SAVE_DRAWING_DATA", payload: null });
    dispatch({ type: "RESET_STATE" });
  };

  const fitToView = () => {
    if (cyRef.current) {
      cyRef.current.fit(null, 50);
    }
  };

  const toggleAnimation = () => {
    setIsAnimating(!isAnimating);
    if (!isAnimating) {
      cyRef.current
        .nodes()
        .animate({
          style: { opacity: 0.7 },
          duration: 1000,
          easing: "ease-in-out",
        })
        .animate({
          style: { opacity: 1 },
          duration: 1000,
          easing: "ease-in-out",
        });
    }
  };

  const filterByCurrentContext = () => {
    if (!cyRef.current) return;

    cyRef.current.nodes().forEach((node) => {
      const nodeContext = node.data("context");
      if (nodeContext === state.currentContext) {
        node.removeClass("dimmed");
      } else {
        node.addClass("dimmed");
      }
    });
  };

  const clearFilters = () => {
    if (!cyRef.current) return;
    cyRef.current.nodes().removeClass("dimmed");
  };

  const exportGraph = () => {
    const graphData = {
      nodes: cyRef.current.nodes().map((node) => ({
        data: node.data(),
        position: node.position(),
      })),
      edges: cyRef.current.edges().map((edge) => ({
        data: edge.data(),
      })),
      metadata: {
        contexts: state.contexts,
        attitudes: state.attitudes,
        currentContext: state.currentContext,
        currentAttitude: state.currentAttitude,
        exportDate: new Date().toISOString(),
      },
    };

    const blob = new Blob([JSON.stringify(graphData, null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `mindgraf-graph-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="relative w-full h-[750px] bg-gradient-to-br from-slate-50 via-white to-blue-50">
      {/* Canvas */}
      <div
        id="graph-canvas"
        ref={containerRef}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
        className="w-full h-full border-2 border-slate-200 bg-white shadow-inner rounded-lg overflow-hidden"
        style={{
          backgroundImage: `
            radial-gradient(circle at 1px 1px, rgba(148, 163, 184, 0.15) 1px, transparent 0)
          `,
          backgroundSize: "20px 20px",
        }}
      />

      {/* Enhanced Controls */}
      <div className="absolute top-4 right-4 z-10 flex flex-col gap-2">
        <div className="bg-white/90 backdrop-blur-md rounded-xl p-3 shadow-lg border border-white/20">
          <div className="flex flex-col gap-2">
            <button
              onClick={saveGraphToState}
              className="flex items-center space-x-2 bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <Save className="w-4 h-4" />
              <span>Save</span>
            </button>
            <button
              onClick={exportGraph}
              className="flex items-center space-x-2 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <Download className="w-4 h-4" />
              <span>Export</span>
            </button>
            <button
              onClick={filterByCurrentContext}
              className="flex items-center space-x-2 bg-purple-500 hover:bg-purple-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <Eye className="w-4 h-4" />
              <span>Filter</span>
            </button>
            <button
              onClick={clearFilters}
              className="flex items-center space-x-2 bg-gray-500 hover:bg-gray-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <EyeOff className="w-4 h-4" />
              <span>Clear</span>
            </button>
            <button
              onClick={toggleEdgeMode}
              className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm ${
                edgeMode
                  ? "bg-red-500 hover:bg-red-600 text-white"
                  : "bg-blue-500 hover:bg-blue-600 text-white"
              }`}
            >
              {edgeMode ? (
                <X className="w-4 h-4" />
              ) : (
                <Link className="w-4 h-4" />
              )}
              <span>{edgeMode ? "Cancel" : "Edge"}</span>
            </button>
            <button
              onClick={fitToView}
              className="flex items-center space-x-2 bg-purple-500 hover:bg-purple-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <Maximize2 className="w-4 h-4" />
              <span>Fit</span>
            </button>
            <button
              onClick={resetGraph}
              className="flex items-center space-x-2 bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-all shadow-sm"
            >
              <Trash2 className="w-4 h-4" />
              <span>Clear</span>
            </button>
          </div>
        </div>
      </div>

      {/* Edge Mode Indicator */}
      {edgeMode && (
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-10 bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-full text-sm font-medium shadow-lg animate-pulse flex items-center space-x-2">
          <Zap className="w-4 h-4" />
          <span>
            {selectedNodeId ? "Select target node" : "Select source node"} for
            edge
          </span>
        </div>
      )}

      {/* Node Editing Panel */}
      {editingNode && (
        <div className="absolute top-4 left-4 bg-white/95 backdrop-blur-md border border-slate-200 rounded-xl shadow-xl p-4 z-20 w-80">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-slate-800">Edit Node</h3>
            <button
              onClick={() => setEditingNode(null)}
              className="text-slate-400 hover:text-slate-600"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Label
              </label>
              <input
                type="text"
                value={nodeLabel}
                onChange={(e) => setNodeLabel(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Context
              </label>
              <select
                value={nodeContext}
                onChange={(e) => setNodeContext(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {state.contexts.map((context) => (
                  <option key={context} value={context}>
                    {context}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Attitude
              </label>
              <select
                value={nodeAttitude}
                onChange={(e) => setNodeAttitude(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {state.attitudes.map((attitude) => (
                  <option key={attitude} value={attitude}>
                    {attitude}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex space-x-2">
              <button
                onClick={updateNodeDetails}
                className="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Update
              </button>
              <button
                onClick={deleteSelectedNode}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Edge Editing Panel */}
      {selectedEdgeId && (
        <div className="absolute top-4 left-4 bg-white/95 backdrop-blur-md border border-slate-200 rounded-xl shadow-xl p-4 z-20 w-80">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-slate-800">Edit Relation</h3>
            <button
              onClick={() => setSelectedEdgeId(null)}
              className="text-slate-400 hover:text-slate-600"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Relation Name
              </label>
              <input
                type="text"
                value={edgeLabel}
                onChange={(e) => setEdgeLabel(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="flex space-x-2">
              <button
                onClick={updateEdgeDetails}
                className="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Update
              </button>
              <button
                onClick={deleteSelectedEdge}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Enhanced Network Statistics */}
      <div className="absolute bottom-4 left-4 bg-white/90 backdrop-blur-md px-4 py-3 rounded-xl text-sm shadow-lg border border-white/20">
        <div className="space-y-2">
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
              <span className="font-medium text-slate-700">
                Nodes: {nodeCount}
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
              <span className="font-medium text-slate-700">
                Edges: {edgeCount}
              </span>
            </div>
          </div>
          <div className="flex items-center space-x-4 text-xs text-slate-600">
            <span>Context: {state.currentContext}</span>
            <span>Attitude: {state.currentAttitude}</span>
          </div>
          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              id="autosave"
              checked={autoSave}
              onChange={(e) => setAutoSave(e.target.checked)}
              className="w-3 h-3"
            />
            <label htmlFor="autosave" className="text-xs text-slate-600">
              Auto-save
            </label>
          </div>
        </div>
      </div>

      {/* Network Status Indicator */}
      <div className="absolute bottom-4 right-4 flex items-center space-x-2 bg-white/90 backdrop-blur-md px-4 py-3 rounded-xl shadow-lg border border-white/20">
        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
        <span className="text-sm font-medium text-slate-700">Graph Active</span>
        {state.lastSaved && (
          <span className="text-xs text-slate-500">
            Saved: {new Date(state.lastSaved).toLocaleTimeString()}
          </span>
        )}
      </div>
    </div>
  );
}
