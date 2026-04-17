import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const { setUser } = useAppContext();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const data = await apiRequest("/auth/login", "POST", form);
      localStorage.setItem("authToken", data.token);
      setUser(data);
      navigate("/health");
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page center auth-shell">
      <form className="card auth-card" onSubmit={submit}>
        <h2>Login</h2>
        <p className="auth-subtitle">Welcome back to Diet Manager.</p>
        <input
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />
        {error && <p className="error">{error}</p>}
        <button className="btn-primary" type="submit">
          Login
        </button>
        <p className="auth-switch">
          No account? <Link to="/signup">Create one</Link>
        </p>
      </form>
    </div>
  );
}
