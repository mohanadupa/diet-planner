import { createContext, useContext, useEffect, useMemo, useState } from "react";

const AppContext = createContext(null);

function safeJsonParse(value) {
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function extractJwtSubject(token) {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    const payloadJson = atob(padded);
    const payload = JSON.parse(payloadJson);
    return typeof payload?.sub === "string" ? payload.sub : null;
  } catch {
    return null;
  }
}

function getInitialUser() {
  const storedUser = safeJsonParse(localStorage.getItem("authUser"));
  if (storedUser?.userId) return storedUser;
  const token = localStorage.getItem("authToken");
  const userId = token ? extractJwtSubject(token) : null;
  return userId ? { userId } : null;
}

export function AppProvider({ children }) {
  const [user, setUser] = useState(() => getInitialUser());
  const [theme, setTheme] = useState("light");

  useEffect(() => {
    if (user) {
      localStorage.setItem("authUser", JSON.stringify(user));
    } else {
      localStorage.removeItem("authUser");
      localStorage.removeItem("authToken");
    }
  }, [user]);

  const value = useMemo(
    () => ({ user, setUser, theme, setTheme }),
    [user, theme]
  );

  return (
    <AppContext.Provider value={value}>
      <div className={`theme-${theme}`}>{children}</div>
    </AppContext.Provider>
  );
}

export function useAppContext() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useAppContext must be used within AppProvider");
  }
  return context;
}
