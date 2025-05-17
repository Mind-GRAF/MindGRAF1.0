import TopBar from "./components/TopBar";
import Sidebar from "./components/Sidebar";
import GraphCanvas from "./components/GraphCanvas";

export default function App() {
  return (
    <div className="flex flex-col h-screen w-screen">
      <TopBar />
      <div className="flex flex-1">
        <div className="flex-1">
          <GraphCanvas />
        </div>
        <Sidebar />
      </div>
    </div>
  );
}
