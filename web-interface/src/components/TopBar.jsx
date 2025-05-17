import { useMode } from "../context/ModeContext";

export default function TopBar() {
  const { mode, setMode } = useMode();
  const modes = ["draw", "search", "trace"];

  return (
    <div className="bg-gray-900 text-white flex items-center justify-between p-4 shadow-md">
      <h1 className="text-xl font-bold">Mind GRAF</h1>
      <div className="space-x-2">
        <button className="bg-blue-500 px-4 py-1 rounded text-sm hover:bg-blue-600">
          DRAW
        </button>
        <button className="bg-gray-700 px-4 py-1 rounded text-sm hover:bg-gray-600">
          SEARCH
        </button>
        <button className="bg-gray-700 px-4 py-1 rounded text-sm hover:bg-gray-600">
          TRACE
        </button>
      </div>
    </div>
  );
}
