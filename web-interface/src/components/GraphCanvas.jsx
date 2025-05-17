import React, { useEffect, useRef, useState } from "react";
import cytoscape from "cytoscape";

export default function GraphCanvas() {
  const containerRef = useRef(null);
  const cyRef = useRef(null);
  const [nodeCount, setNodeCount] = useState(0);
  const [edgeCount, setEdgeCount] = useState(0);
  const [cableMode, setCableMode] = useState(false);
  const [selectedNodeId, setSelectedNodeId] = useState(null);

  // Initialize Cytoscape
  useEffect(() => {
    // Only initialize once
    if (containerRef.current && !cyRef.current) {
      console.log("Initializing Cytoscape");

      cyRef.current = cytoscape({
        container: containerRef.current,
        style: [
          {
            selector: "node",
            style: {
              label: "data(label)",
              "text-valign": "center",
              "text-halign": "center",
              "text-wrap": "wrap",
              "text-max-width": "80px",
              "font-size": "12px",
              "border-width": 2,
              padding: "10px",
            },
          },
          {
            selector: 'node[type="Node"]',
            style: {
              "background-color": "#3b82f6",
              shape: "ellipse",
              "border-color": "#2563eb",
              width: "50px",
              height: "50px",
            },
          },
          {
            selector: 'node[type="Context"]',
            style: {
              "background-color": "#10b981",
              shape: "rectangle",
              "border-color": "#059669",
              width: "80px",
              height: "60px",
            },
          },
          {
            selector: 'node[type="Attitude Frame"]',
            style: {
              "background-color": "#f59e0b",
              shape: "diamond",
              "border-color": "#d97706",
              width: "70px",
              height: "70px",
            },
          },
          {
            selector: "edge",
            style: {
              "curve-style": "bezier",
              width: 3,
              "line-color": "#64748b",
              "target-arrow-color": "#64748b",
              "target-arrow-shape": "triangle",
              "arrow-scale": 1.5,
              label: "data(label)",
              "font-size": "10px",
              "text-rotation": "autorotate",
              "text-margin-y": -10,
            },
          },
          {
            selector: ".selected",
            style: {
              "border-width": 4,
              "border-color": "#ef4444",
              "border-style": "solid",
            },
          },
        ],
        layout: { name: "preset" },
        // Disable wheel sensitivity to avoid the warning
        wheelSensitivity: 0.1,
      });

      // Add some initial test elements
      /* cyRef.current.add([
        { 
          group: 'nodes', 
          data: { id: 'initial-node-1', label: 'Test Node 1', type: 'Node' },
          position: { x: 100, y: 100 }
        },
        { 
          group: 'nodes', 
          data: { id: 'initial-node-2', label: 'Test Node 2', type: 'Node' },
          position: { x: 300, y: 100 }
        }
      ]); */
    }

    // Cleanup on unmount
    return () => {
      if (cyRef.current) {
        cyRef.current.destroy();
        cyRef.current = null;
      }
    };
  }, []); // Run only once on mount

  // Set up event handlers with proper state access
  useEffect(() => {
    if (!cyRef.current) return;

    // Node selection handler
    const nodeClickHandler = function (evt) {
      const nodeId = this.id();
      console.log("Node clicked:", nodeId);

      // Remove selection class from all elements
      cyRef.current.elements().removeClass("selected");

      // Handle cable mode
      if (cableMode) {
        if (!selectedNodeId) {
          // First node selection in cable mode
          setSelectedNodeId(nodeId);
          this.addClass("selected");
          console.log("First node selected for cable:", nodeId);
        } else if (selectedNodeId !== nodeId) {
          // Second node selection - create the edge
          console.log("Creating edge from", selectedNodeId, "to", nodeId);
          const edgeId = `edge-${edgeCount + 1}`;

          cyRef.current.add({
            group: "edges",
            data: {
              id: edgeId,
              source: selectedNodeId,
              target: nodeId,
              label: "Cable",
            },
          });

          // Exit cable mode
          setCableMode(false);
          setSelectedNodeId(null);
          setEdgeCount((prev) => prev + 1);
          console.log("Edge created:", edgeId);
        }
      } else {
        // Regular selection
        this.addClass("selected");
        setSelectedNodeId(nodeId);
      }

      // Stop event propagation
      evt.originalEvent.stopPropagation();
    };

    // Background click handler
    const backgroundClickHandler = function (evt) {
      if (evt.target === cyRef.current) {
        console.log("Background clicked");
        // Clear selection if clicking on background
        cyRef.current.elements().removeClass("selected");
        setSelectedNodeId(null);
      }
    };

    // Attach event handlers
    cyRef.current.on("tap", "node", nodeClickHandler);
    cyRef.current.on("tap", backgroundClickHandler);

    // Clean up event handlers when dependencies change
    return () => {
      if (cyRef.current) {
        cyRef.current.removeListener("tap", "node", nodeClickHandler);
        cyRef.current.removeListener("tap", backgroundClickHandler);
      }
    };
  }, [cableMode, selectedNodeId, edgeCount]); // Include all state that the handlers depend on

  // Handle drag and drop
  const handleDrop = (e) => {
    e.preventDefault();
    const type = e.dataTransfer.getData("text/plain");
    console.log("Element dropped:", type);

    if (!type || !cyRef.current) {
      console.log("No type data or cytoscape instance not available");
      return;
    }

    // Get position relative to the container
    const rect = containerRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Handle cable (special case)
    if (type === "Cable") {
      console.log("Entering cable mode");
      setCableMode(true);
      // Show a message or visual indicator
      return;
    }

    // Create a new node
    const nodeId = `node-${nodeCount + 1}`;
    console.log("Adding node:", nodeId, "at", x, y);

    cyRef.current.add({
      group: "nodes",
      data: {
        id: nodeId,
        label: type,
        type: type,
      },
      position: { x, y },
    });

    setNodeCount((prev) => prev + 1);
  };

  // Helper functions for debugging
  const addTestNode = () => {
    if (!cyRef.current) return;

    // Generate random position within visible area for better testing
    const width = containerRef.current.clientWidth * 0.8;
    const height = containerRef.current.clientHeight * 0.8;
    const x = Math.random() * width + 50;
    const y = Math.random() * height + 50;

    // Generate a unique ID with timestamp to avoid collisions
    const timestamp = new Date().getTime();
    const nodeId = `test-node-${nodeCount + 1}-${timestamp}`;

    // Check if the node with this ID already exists
    if (cyRef.current.getElementById(nodeId).length > 0) {
      console.warn(`Node with ID ${nodeId} already exists, skipping creation`);
      return;
    }

    cyRef.current.add({
      group: "nodes",
      data: {
        id: nodeId,
        label: `Test Node ${nodeCount + 1}`,
        type: "Node",
      },
      position: { x, y },
    });

    setNodeCount((prev) => prev + 1);
    console.log("Added test node:", nodeId);
  };

  const toggleCableMode = () => {
    // If we're turning off cable mode, clear selections
    if (cableMode) {
      cyRef.current.elements().removeClass("selected");
      setSelectedNodeId(null);
    }
    setCableMode(!cableMode);
    console.log("Cable mode:", !cableMode);
  };

  const resetGraph = () => {
    if (!cyRef.current) return;
    cyRef.current.elements().remove();
    setNodeCount(0);
    setEdgeCount(0);
    setCableMode(false);
    setSelectedNodeId(null);
    console.log("Graph reset");
  };

  return (
    <div className="relative w-full h-[750px]">
      <div
        id="graph-canvas"
        ref={containerRef}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
        className="w-full h-full border border-gray-200 bg-gray-50"
      />

      <div className="absolute top-2 right-2 z-10 flex flex-col gap-1">
        <button
          onClick={addTestNode}
          className="bg-blue-500 text-white text-xs px-3 py-1 rounded hover:bg-blue-600"
        >
          Add Test Node
        </button>
        <button
          onClick={resetGraph}
          className="bg-blue-500 text-white text-xs px-3 py-1 rounded hover:bg-blue-600"
        >
          Clear the Network
        </button>
        <button
          onClick={toggleCableMode}
          className="bg-blue-500 text-white text-xs px-3 py-1 rounded hover:bg-blue-600"
        >
          {cableMode ? "Cancel Cable" : "Start Cable"}
        </button>
      </div>

      {cableMode && (
        <div className="absolute top-2 left-1/2 -translate-x-1/2 px-4 py-2 bg-blue-500 text-white rounded text-sm z-10 pointer-events-none">
          {selectedNodeId ? "Select target node" : "Select source node"} for
          cable
        </div>
      )}
    </div>
  );
}
