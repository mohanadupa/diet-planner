import { useNavigate } from "react-router-dom";

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="page center landing-shell">
      <div className="landing-bg" aria-hidden="true" />
      <div className="card landing-card landing-hero">
        <div className="brand-logo">DM</div>
        <h1>NeutriGuide-Personalized Diet Planner</h1>
        <p>
          Personalized plans, daily meal tracking, and progress insights in one
          simple dashboard.
        </p>
        <button className="btn-primary" onClick={() => navigate("/login")}>
          Get Started
        </button>
      </div>
    </div>
  );
}
