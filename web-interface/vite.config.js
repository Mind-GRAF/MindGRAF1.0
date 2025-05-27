// vite.config.js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    hmr: {
      overlay: false,
    },
    host: true,
    port: 5173,
  },
  optimizeDeps: {
    include: ["cytoscape", "lucide-react"],
  },
  build: {
    rollupOptions: {
      external: [],
    },
  },
});
