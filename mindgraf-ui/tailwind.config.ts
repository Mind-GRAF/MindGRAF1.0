// tailwind.config.ts
import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "media",
  content: [
    "./app/**/*.{js,ts,jsx,tsx}",
    "./pages/**/*.{js,ts,jsx,tsx}",
    "./components/**/*.{js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        primary: "#3b82f6",
        secondary: "#6366f1",
        accent: "#10b981",
        cardBg: "rgba(255,255,255,0.8)",
        cardBgDark: "rgba(30,30,30,0.8)"
      },
      fontFamily: {
        sans: ["Inter", "Arial", "Helvetica", "sans-serif"],
        mono: ["var(--font-geist-mono)"]
      }
    }
  },
  plugins: []
};

export default config;
