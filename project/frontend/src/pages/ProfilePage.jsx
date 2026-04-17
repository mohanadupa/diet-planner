import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function ProfilePage() {
  const { user } = useAppContext();
  const [stats, setStats] = useState(null);

  useEffect(() => {
    async function loadStats() {
      const data = await apiRequest(`/profile/stats?userId=${user.userId}`);
      setStats(data);
    }
    if (user) loadStats();
  }, [user]);

  return (
    <div className="panel">
      <div className="section-heading">
        <h2>Profile Performance</h2>
        <p>Track your consistency and completion rate.</p>
      </div>
      <div className="stats-grid">
        <div className="stat-card">
          <p className="stat-label">Completion</p>
          <h3>{stats?.completionPercentage ?? 0}%</h3>
        </div>
        <div className="stat-card">
          <p className="stat-label">Status</p>
          <h3>{stats?.rating ?? "Bad"}</h3>
        </div>
      </div>
      {stats?.updateAvailable && (
        <p className="info-banner">
          Monthly update is available. Refresh health details to generate a new plan.
        </p>
      )}
    </div>
  );
}
