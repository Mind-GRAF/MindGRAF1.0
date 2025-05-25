import TopBar from "./components/TopBar";
import Sidebar from "./components/Sidebar";
import GraphCanvas from "./components/GraphCanvas";
import CommandConsole from "./components/CommandConsole";

export default function App() {
  return (
    <div className="flex flex-col h-screen w-screen">
      <TopBar />
      <div className="flex flex-1">
        <div className="flex flex-col flex-1">
          <GraphCanvas />
          <div className="h-[200px] overflow-y-auto border-t bg-gray-50">
            <CommandConsole />
          </div>
        </div>
        <Sidebar />
      </div>
    </div>
  );
}
