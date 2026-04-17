import { Link, Outlet } from "react-router-dom";

export default function DashboardLayout() {
  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-logo">DM</div>
          <h3>Diet Manager</h3>
        </div>
        <nav className="sidebar-nav">
          <Link to="/dashboard/diet-plan">Diet Plan</Link>
          <Link to="/dashboard/profile">Profile</Link>
          <Link to="/dashboard/settings">Settings</Link>
        </nav>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
