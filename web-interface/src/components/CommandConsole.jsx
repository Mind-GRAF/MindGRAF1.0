import React, { useState } from "react";
import { runCommand } from "../api/runCommand";

function CommandConsole() {
  const [command, setCommand] = useState("");
  const [response, setResponse] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!command.trim()) return;

    const result = await runCommand(command);
    setResponse(result);
  };

  return (
    <div className="p-4 border rounded-md shadow bg-white text-black">
      <h2 className="text-xl font-bold mb-2">Command Console</h2>
      <form onSubmit={handleSubmit} className="flex flex-col space-y-2">
        <textarea
          className="p-2 border rounded resize-none"
          rows="4"
          placeholder="Enter CLI command here..."
          value={command}
          onChange={(e) => setCommand(e.target.value)}
        />
        <button
          type="submit"
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Run Command
        </button>
      </form>
      {response && (
        <div className="mt-4 bg-gray-100 p-3 rounded text-sm whitespace-pre-wrap">
          {response}
        </div>
      )}
    </div>
  );
}

export default CommandConsole;
