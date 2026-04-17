import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function SignupPage() {
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const { setUser } = useAppContext();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const data = await apiRequest("/auth/signup", "POST", form);
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
        <h2>Sign Up</h2>
        <p className="auth-subtitle">Create your Diet Manager account.</p>
        <input
          placeholder="Full Name"
          value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
          required
        />
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
          Create Account
        </button>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
}
