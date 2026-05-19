# HTN Deterministic Backtracking Visualization

A Remotion-based video visualization of the deterministic planning and sibling-preserving backtracking behavior implemented in the Marwa-side HTN integration.

## Overview

This visualization demonstrates the whiteboard scenario from the thesis:
- **Act `a`** with plans **[p1, p2, p3]**
- **p1** expands to **[p4, p5, p6]**
- **p4** expands to **[a2, a3, a4]** via DoAll
- **a3** expands to **[p7, p8]**
- When a deeper branch fails, the scheduler:
  1. Pops the latest choice point
  2. Trims the pending act queue to the saved depth
  3. Trims the pending execution queue to the saved depth
  4. Resumes with the next sibling alternative

## Running the Visualization Locally

### Prerequisites

- Node.js 16+ installed
- `npm` or `yarn` package manager

### Setup

```bash
cd htn-visualization
npm install
```

### Preview Mode (Development)

Watch the visualization in your browser at `http://localhost:3000`:

```bash
npm start
```

This launches the Remotion preview server. You can:
- Play/pause the video
- Scrub through frames
- Adjust playback speed
- Take screenshots

### Render to File

Generate an MP4 video file of the visualization (takes ~2 minutes):

```bash
npm run build
```

Output: `backtracking-flow.mp4`

## File Structure

```
htn-visualization/
├── src/
│   ├── Root.tsx                    # Entry point; declares the composition
│   └── compositions/
│       ├── BacktrackingFlow.tsx    # Main visualization component
│       └── BacktrackingFlow.module.css
├── package.json
├── tsconfig.json
└── README.md
```

## Visualization Timeline (60 seconds at 30fps)

| Phase | Frames | Duration | Event |
|-------|--------|----------|-------|
| Setup | 0-150 | 0-5s | Initialize scheduler, begin planning |
| P1 Decomposition | 150-300 | 5-10s | DoOne selects p1, saves [p2, p3] as choice point |
| P4 Expansion | 300-450 | 10-15s | p1 expands to [p4, p5, p6], schedule p4 |
| A3 Expansion | 450-600 | 15-20s | p4 → a2, a3, a4 (DoAll); a3 → p7, p8 (primitives queued) |
| Failure Detection | 600-750 | 20-25s | Deeper branch fails; begin BACKTRACKING phase |
| Trim & Resume | 750-900 | 25-30s | Trim queues to saved depths, resume with p5 sibling |
| Continue Planning | 900-1050 | 30-35s | Plan with p5, p6 alternatives |
| Summary | 1050-1800 | 35-60s | Summary: all planning complete, execution ready |

## What the Visualization Shows

### Three Main Sections (Left to Right)

1. **Act Queue (Pending Plans)** — Shows the stack of control acts and plans waiting to be processed
   - As acts decompose, queue depth increases
   - When backtracking, items are trimmed from the top (shown in orange)
   - Failure indicators in red

2. **Execution Queue (Queued Primitives)** — Shows the queue of primitive acts waiting to execute
   - Primitives are added during planning but not executed
   - When backtracking, primitives from the failing branch are removed
   - Ensures "planning first, execution after" semantics

3. **Choice Point Stack** — Shows saved sibling alternatives at each decomposition point
   - Each choice point records the act-queue depth and execution-queue depth
   - When backtracking, the scheduler pops from this stack
   - Used to resume with the next sibling alternative

### Event Log (Bottom)

Real-time log of planning and backtracking events:
- ✓ indicates successful planning steps
- ⚠ indicates failures or backtracking
- → indicates trimming or resumption

## Key Insights

The visualization demonstrates:

1. **Deterministic Planning**: Plans are always tried in the same order (first alternative first)
2. **Sibling Preservation**: Alternatives are never discarded; they are saved as choice points
3. **Structural Backtracking**: Failed branches are undone by removing pending acts and primitives
4. **Planning-First Semantics**: Primitives are queued but not executed until planning is complete

## Customization

To change the visualization parameters, edit `src/compositions/BacktrackingFlow.tsx`:

- **Timeline phases**: Adjust frame numbers in `useMemo` interpolations
- **Queue colors/styling**: Modify the inline `style` objects in render functions
- **Act/primitive names**: Update the arrays (`acts`, `primitives`) in the render functions
- **Duration**: Change `durationInFrames` in `src/Root.tsx` (currently 1800 frames = 60 seconds at 30fps)

## Integration with Thesis

This visualization is part of the MARWA HTN Integration project. For background:

- See [../docs/MARWA_HTN_INTEGRATION_CHANGES.md](../docs/MARWA_HTN_INTEGRATION_CHANGES.md) for the full before/after code and rationale
- Code changes are in:
  - `src/main/java/edu/guc/mind_graf/mgip/Scheduler.java` (choice-point stack, backtracking logic)
  - `src/main/java/edu/guc/mind_graf/nodes/ActNode.java` (deferred primitive execution)
  - `src/main/java/edu/guc/mind_graf/nodes/DoOneNode.java` (deterministic sibling preservation)
  - `src/main/java/edu/guc/mind_graf/nodes/DoAllNode.java` (deterministic scheduling)
  - `src/main/java/edu/guc/mind_graf/set/NodeSet.java` (ordered collection via LinkedHashMap)

## Troubleshooting

### "Cannot find module 'remotion'"

Make sure you ran `npm install` in the `htn-visualization` folder:

```bash
npm install
```

### Preview not loading

Port 3000 may be in use. To use a different port:

```bash
PORT=3001 npm start
```

### Video rendering fails

Ensure you have ffmpeg installed on your system. On macOS:

```bash
brew install ffmpeg
```

On Ubuntu:

```bash
sudo apt-get install ffmpeg
```

## Questions or Issues?

Refer to the main thesis documentation or the Remotion docs at https://www.remotion.dev.
