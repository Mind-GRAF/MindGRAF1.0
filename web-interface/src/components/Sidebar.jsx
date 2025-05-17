import React from "react";

export default function Sidebar() {
  const handleDragStart = (e, type) => {
    e.dataTransfer.setData("text/plain", type);
  };

  return (
    <div className="w-64 bg-gray-100 border-l border-gray-300 p-4 overflow-y-auto">
      <h2 className="text-lg font-semibold mb-4">Elements</h2>

      <div
        draggable
        onDragStart={(e) => handleDragStart(e, "Node")}
        className="block p-2 bg-white border border-gray-200 rounded shadow-sm mb-2 cursor-move hover:bg-gray-50"
      >
        Node
      </div>

      <div
        draggable
        onDragStart={(e) => handleDragStart(e, "Context")}
        className="block p-2 bg-white border border-gray-200 rounded shadow-sm mb-2 cursor-move hover:bg-gray-50"
      >
        Context
      </div>

      <div
        draggable
        onDragStart={(e) => handleDragStart(e, "Attitude Frame")}
        className="block p-2 bg-white border border-gray-200 rounded shadow-sm mb-2 cursor-move hover:bg-gray-50"
      >
        Attitude Frame
      </div>
    </div>
  );
}
