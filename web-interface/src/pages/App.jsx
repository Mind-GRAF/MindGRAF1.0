import TopBar from "./components/TopBar";
import Sidebar from "./components/Sidebar";
import Canvas from "./components/Canvas";

export default function App() {
  return (
    <div className="flex flex-col h-screen w-screen">
      <TopBar />
      <div className="flex flex-1">
        <div className="flex-1">
          <Canvas />
        </div>
        <Sidebar />
      </div>
    </div>
  );
}
