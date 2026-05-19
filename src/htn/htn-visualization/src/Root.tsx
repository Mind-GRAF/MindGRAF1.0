import React from "react";
import { Composition, registerRoot } from "remotion";
import { BacktrackingFlow } from "./compositions/BacktrackingFlow";

export const Root: React.FC = () => {
  return (
    <Composition
      id="BacktrackingFlow"
      component={BacktrackingFlow}
      durationInFrames={1800}
      fps={30}
      width={1920}
      height={1080}
    />
  );
};

registerRoot(Root);
