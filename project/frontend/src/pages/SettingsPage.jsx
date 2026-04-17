import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function SettingsPage() {
  const { user, theme, setTheme, setUser } = useAppContext();
  const [profile, setProfile] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    async function loadUser() {
      const data = await apiRequest(`/user?userId=${user.userId}`);
      setProfile(data);
      setTheme(data.theme || "light");
    }
    if (user) loadUser();
  }, [user, setTheme]);

  const changeTheme = async (newTheme) => {
    setTheme(newTheme);
    await apiRequest("/user/theme", "PUT", { userId: user.userId, theme: newTheme });
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("authUser");
    localStorage.removeItem("dietPlanMeta");
    setUser(null);
    navigate("/login", { replace: true });
  };

  return (
    <div className="panel">
      <div className="section-heading">
        <h2>Settings</h2>
        <p>Manage account preferences and appearance.</p>
      </div>
      <div className="settings-list">
        <p>
          <strong>Name:</strong> {profile?.fullName}
        </p>
        <p>
          <strong>Email:</strong> {profile?.email}
        </p>
      </div>
      <div className="row">
        <button
          className={theme === "light" ? "btn-primary" : ""}
          disabled={theme === "light"}
          onClick={() => changeTheme("light")}
        >
          Light
        </button>
        <button
          className={theme === "dark" ? "btn-primary" : ""}
          disabled={theme === "dark"}
          onClick={() => changeTheme("dark")}
        >
          Dark
        </button>
      </div>

      <div className="settings-actions">
        <button className="btn-danger" type="button" onClick={logout}>
          Logout
        </button>
      </div>
    </div>
  );
}
