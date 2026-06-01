"use client";
import { useState, useRef, useEffect, useCallback } from "react";

// ─── SVG Icons ─────────────────────────────────────────────────────────────
const BrainIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 4.5a2.5 2.5 0 0 0-4.96-.46 2.5 2.5 0 0 0-1.98 3 2.5 2.5 0 0 0-1.32 4.24 3 3 0 0 0 .34 5.58 2.5 2.5 0 0 0 2.96 3.08 2.5 2.5 0 0 0 4.91.05L12 20V4.5Z" />
    <path d="M12 4.5a2.5 2.5 0 0 1 4.96-.46 2.5 2.5 0 0 1 1.98 3 2.5 2.5 0 0 1 1.32 4.24 3 3 0 0 1-.34 5.58 2.5 2.5 0 0 1-2.96 3.08 2.5 2.5 0 0 1-4.91.05L12 20V4.5Z" />
  </svg>
);

const UserIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

const TerminalIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="4 17 10 11 4 5" />
    <line x1="12" y1="19" x2="20" y2="19" />
  </svg>
);

const NetworkIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 3a3 3 0 0 0-3 3v12a3 3 0 0 0 3 3 3 3 0 0 0 3-3 3 3 0 0 0-3-3H6a3 3 0 0 0-3 3 3 3 0 0 0 3 3 3 3 0 0 0 3-3V6a3 3 0 0 0-3-3 3 3 0 0 0-3 3 3 3 0 0 0 3 3h12a3 3 0 0 0 3-3 3 3 0 0 0-3-3z" />
  </svg>
);

const GlobeIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
    <path d="M2 12h20" />
  </svg>
);

const SearchIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8" />
    <path d="m21 21-4.3-4.3" />
  </svg>
);

const ZapIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
  </svg>
);

const SendIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="12" y1="19" x2="12" y2="5" />
    <polyline points="5 12 12 5 19 12" />
  </svg>
);

const SettingsIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="3" />
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
  </svg>
);

const PlayIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="5 3 19 12 5 21 5 3" />
  </svg>
);

const AlertIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8" x2="12" y2="12" />
    <line x1="12" y1="16" x2="12.01" y2="16" />
  </svg>
);

const CheckIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
    <polyline points="22 4 12 14.01 9 11.01" />
  </svg>
);

// ─── Types ──────────────────────────────────────────────────────────────────
interface TranslateResponse {
  command?: string;
  category?: string;
  javaResponse?: string;
  retryCount?: number;
  success?: boolean;
  errors?: string[];
  error?: string;
}

interface ChatMessage {
  id: string;
  type: "user" | "ai" | "system" | "error";
  text: string;
  response?: TranslateResponse;
  timestamp: Date;
}

// ─── Formatter ─────────────────────────────────────────────────────────────
const formatTime = (date: Date) => {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// ─── Example prompts ────────────────────────────────────────────────────────
const PROMPTS = [
  { icon: <NetworkIcon />, text: "Define a relation called mother" },
  { icon: <GlobeIcon />, text: "Create a context named hogwarts" },
  { icon: <SearchIcon />, text: "Who is Harry Potter's mother?" },
  { icon: <ZapIcon />, text: "Add a node for Hermione Granger" },
];

export default function Home() {
  // ── State ───────────────────────────────────────────────────────────────────
  const [input, setInput] = useState("");
  const [context, setContext] = useState("hogwarts");
  const [attitude, setAttitude] = useState("belief");
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [statusType, setStatusType] = useState<"idle" | "ready" | "error" | "loading">("idle");
  const [booted, setBooted] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

  const chatEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // ── Auto-scroll ─────────────────────────────────────────────────────────────
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  // ── Auto-resize textarea ───────────────────────────────────────────────────
  const autoResize = useCallback(() => {
    const ta = textareaRef.current;
    if (!ta) return;
    ta.style.height = "24px";
    ta.style.height = Math.min(ta.scrollHeight, 120) + "px";
  }, []);

  useEffect(() => {
    autoResize();
  }, [input, autoResize]);

  // ── Boot / Context Switch ──────────────────────────────────────────────────
  const initializeEnvironment = async () => {
    setLoading(true);
    setStatusType("loading");
    setSettingsOpen(false);

    // Add a system message to chat to show we are doing setup
    const initMsgId = crypto.randomUUID();
    setMessages((prev) => [
      ...prev,
      {
        id: initMsgId,
        type: "system",
        text: `Starting initialization for Context: "${context}" & Attitude: "${attitude}"...`,
        timestamp: new Date(),
      },
    ]);

    try {
      if (!booted) {
        const bootRes = await fetch("http://localhost:8080/execute", {
          method: "POST",
          body: `boot-wizard ${attitude}`,
        });
        if (!bootRes.ok) throw new Error("Java engine rejected the Boot Wizard initialization.");
        setBooted(true);
      }

      const ctxRes = await fetch("http://localhost:8080/execute", {
        method: "POST",
        body: `define-context ${context}`,
      });
      if (!ctxRes.ok) {
        const t = await ctxRes.text();
        if (!t.toLowerCase().includes("already exist")) {
          throw new Error(`Java rejected Context definition for "${context}".`);
        }
      }

      const setCtxRes = await fetch("http://localhost:8080/execute", {
        method: "POST",
        body: `set-curr-context ${context}`,
      });
      if (!setCtxRes.ok) throw new Error("Java rejected Context switch command.");

      const setAttRes = await fetch("http://localhost:8080/execute", {
        method: "POST",
        body: `set-attitude ${attitude}`,
      });
      if (!setAttRes.ok) throw new Error("Java rejected Attitude switch command.");

      // Success
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          type: "system",
          text: "Initialization successful. MindGRAF engine is ready.",
          timestamp: new Date(),
        },
      ]);
      setStatusType("ready");

    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      // Log error directly in chat
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          type: "error",
          text: `Initialization Error: ${msg}`,
          timestamp: new Date(),
        },
      ]);
      setStatusType("error");
    } finally {
      setLoading(false);
    }
  };

  // ── Process natural language ──────────────────────────────────────────────
  const processNaturalLanguage = async (overrideInput?: string) => {
    const text = (overrideInput ?? input).trim();
    if (!text) return;

    const userMsg: ChatMessage = {
      id: crypto.randomUUID(),
      type: "user",
      text,
      timestamp: new Date(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setLoading(true);
    setStatusType("loading");

    try {
      const res = await fetch("/api/translate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          englishText: text,
          contextName: context,
          attitudeName: attitude,
        }),
      });

      const data: TranslateResponse = await res.json();

      if (!res.ok || data.error) {
        setMessages((prev) => [
          ...prev,
          {
            id: crypto.randomUUID(),
            type: "error",
            text: `Pipeline Error: ${data.error ?? "Unknown issue occurred"}`,
            timestamp: new Date(),
          },
        ]);
        setStatusType("error");
      } else {
        const aiMsg: ChatMessage = {
          id: crypto.randomUUID(),
          type: "ai",
          text: data.success ? "Command Executed Successfully" : "Command processed with warnings",
          response: data,
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, aiMsg]);
        setStatusType(data.success ? "ready" : "error");
        
        if (!data.success) {
           setMessages((prev) => [
            ...prev,
            {
              id: crypto.randomUUID(),
              type: "error",
              text: `Max retries reached — command may not have been accepted.`,
              timestamp: new Date(),
            },
          ]);
        }
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          type: "error",
          text: `Network Error: ${msg}`,
          timestamp: new Date(),
        },
      ]);
      setStatusType("error");
    } finally {
      setLoading(false);
    }
  };

  const hasInput = input.trim().length > 0;

  // Header status string based on type
  const getHeaderStatusText = () => {
    switch (statusType) {
      case "loading": return "Processing...";
      case "ready": return "Connected";
      case "error": return "Error State";
      default: return "Idle";
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <>
      {/* Ambient background orbs */}
      <div className="ambient-bg" aria-hidden="true">
        <div className="ambient-orb ambient-orb-1" />
        <div className="ambient-orb ambient-orb-2" />
        <div className="ambient-orb ambient-orb-3" />
      </div>

      <div className="app-shell">
        {/* ── Header ──────────────────────────────────────────────────────── */}
        <header className="app-header">
          <div className="header-left">
            <span className="logo-icon" aria-hidden="true"><BrainIcon /></span>
            <span className="app-title">MindGRAF</span>
            <span className="app-subtitle">NLI</span>
          </div>

          <div className="header-right">
            <div className={`status-dot ${statusType}`} />
            <span className="status-text">{getHeaderStatusText()}</span>

            <button
              id="btn-settings"
              className={`header-btn ${settingsOpen ? "active" : ""}`}
              onClick={() => setSettingsOpen((o) => !o)}
              title="Settings"
            >
              <SettingsIcon />
              <span>Settings</span>
            </button>

            <button
              id="btn-init"
              className="header-btn primary"
              onClick={initializeEnvironment}
              disabled={loading}
            >
              <PlayIcon />
              <span>{booted ? "Switch Context" : "Initialize"}</span>
            </button>
          </div>
        </header>

        {/* ── Settings Panel ──────────────────────────────────────────────── */}
        <div className={`settings-panel ${settingsOpen ? "open" : ""}`}>
          <div className="settings-grid">
            <div className="settings-field">
              <label htmlFor="input-context" className="settings-label">
                Context
              </label>
              <input
                id="input-context"
                className="settings-input"
                value={context}
                onChange={(e) => setContext(e.target.value)}
              />
            </div>
            <div className="settings-field">
              <label htmlFor="input-attitude" className="settings-label">
                Attitude
              </label>
              <input
                id="input-attitude"
                className="settings-input"
                value={attitude}
                onChange={(e) => setAttitude(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* ── Chat Area ───────────────────────────────────────────────────── */}
        <div className="chat-area">
          {messages.length === 0 && !loading ? (
            /* Empty state */
            <div className="empty-state">
              <div className="empty-logo"><BrainIcon /></div>
              <h1 className="empty-title">What would you like to explore?</h1>
              <p className="empty-subtitle">
                Ask anything in natural language — I&apos;ll translate it into MindGRAF
                commands and execute them for you.
              </p>
              <div className="prompt-cards">
                {PROMPTS.map((p, i) => (
                  <button
                    key={i}
                    className="prompt-card"
                    onClick={() => processNaturalLanguage(p.text)}
                  >
                    <div className="prompt-card-icon">{p.icon}</div>
                    <div className="prompt-card-text">{p.text}</div>
                  </button>
                ))}
              </div>
            </div>
          ) : (
            /* Messages */
            <>
              {messages.map((msg) => (
                <div key={msg.id} className={`message ${msg.type}`}>
                  
                  {/* System & Error messages don't have avatars, they are full width banners */}
                  {msg.type === "system" || msg.type === "error" ? (
                    <div className={`system-banner ${msg.type}`}>
                      {msg.type === "system" ? <CheckIcon /> : <AlertIcon />}
                      <span className="system-text">{msg.text}</span>
                      <span className="system-time">{formatTime(msg.timestamp)}</span>
                    </div>
                  ) : (
                    <>
                      {/* Avatar for User / AI */}
                      <div className={`avatar ${msg.type}`}>
                        {msg.type === "user" ? <UserIcon /> : <TerminalIcon />}
                      </div>

                      <div className="msg-content">
                        {/* Header */}
                        <div className="msg-header">
                          <span className={`msg-name ${msg.type}`}>
                            {msg.type === "user" ? "You" : "MindGRAF"}
                          </span>
                          <span className="msg-time">{formatTime(msg.timestamp)}</span>
                        </div>

                        {/* Content */}
                        {msg.type === "user" ? (
                          <div className="msg-text">{msg.text}</div>
                        ) : (
                          <div className="response-card">
                            {msg.response ? (
                              <>
                                {/* Category Label */}
                                {msg.response.category && (
                                  <div className="card-category">
                                    <span className="cat-dot" />
                                    {msg.response.category}
                                  </div>
                                )}

                                {/* Command Block */}
                                {msg.response.command && (
                                  <div className="card-section">
                                    <span className="section-label">Generated Command</span>
                                    <div className="command-block">
                                      {msg.response.command}
                                    </div>
                                  </div>
                                )}

                                {/* Response Block */}
                                {msg.response.javaResponse && (
                                  <div className="card-section">
                                    <span className="section-label">Response</span>
                                    <div className="response-text">
                                      {msg.response.javaResponse}
                                    </div>
                                  </div>
                                )}

                                {/* Errors Details */}
                                {msg.response.errors && msg.response.errors.length > 0 && (
                                  <div className="card-section">
                                    <details>
                                      <summary className="error-toggle">
                                        {msg.response.errors.length} error(s) in retry history
                                      </summary>
                                      <ul className="error-list">
                                        {msg.response.errors.map((e, i) => (
                                          <li key={i} className="error-item">{e}</li>
                                        ))}
                                      </ul>
                                    </details>
                                  </div>
                                )}
                              </>
                            ) : (
                              <div className="response-text">{msg.text}</div>
                            )}
                          </div>
                        )}
                      </div>
                    </>
                  )}
                </div>
              ))}

              {/* Loading indicator */}
              {loading && (
                <div className="message ai">
                  <div className="avatar ai">
                    <TerminalIcon />
                  </div>
                  <div className="msg-content">
                    <div className="msg-header">
                      <span className="msg-name ai">MindGRAF</span>
                    </div>
                    <div className="loading-dots">
                      <span />
                      <span />
                      <span />
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
          <div ref={chatEndRef} />
        </div>

        {/* ── Footer / Input ──────────────────────────────────────────────── */}
        <footer className="app-footer">
          <div className="input-container">
            <div className="input-wrapper">
              <textarea
                ref={textareaRef}
                id="input-nl"
                className="chat-input"
                rows={1}
                placeholder="Describe what you want to do in natural language..."
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    processNaturalLanguage();
                  }
                }}
              />
              <button
                id="btn-process"
                className={`send-btn ${hasInput ? "active" : ""}`}
                onClick={() => processNaturalLanguage()}
                disabled={loading || !hasInput}
                aria-label="Send message"
              >
                <SendIcon />
              </button>
            </div>
            <div className="keyboard-hint">
              <span><kbd>Enter</kbd> to send</span>
              <span><kbd>Shift</kbd> + <kbd>Enter</kbd> for new line</span>
            </div>
          </div>
        </footer>
      </div>
    </>
  );
}