import { createContext, useContext, useState } from "react";

const ModeContext = createContext();

export const ModeProvider = ({ children }) => {
  const [mode, setMode] = useState("draw"); // default to 'draw'

  return (
    <ModeContext.Provider value={{ mode, setMode }}>
      {children}
    </ModeContext.Provider>
  );
};

export const useMode = () => useContext(ModeContext);
