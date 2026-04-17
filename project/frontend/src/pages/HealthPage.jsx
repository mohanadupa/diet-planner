import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function HealthPage() {
  const { user } = useAppContext();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    age: 25,
    gender: "MALE",
    activityLevel: "MODERATE",
    heightCm: 170,
    weightKg: 70,
    goalType: "WEIGHT_LOSS"
  });

  const bmi = form.heightCm > 0
    ? (form.weightKg / ((form.heightCm / 100) * (form.heightCm / 100))).toFixed(2)
    : "0.00";

  const submit = async (e) => {
    e.preventDefault();
    const response = await apiRequest("/health-profile", "POST", {
      ...form,
      bmi: Number(bmi),
      userId: user.userId
    });
    localStorage.setItem("dietPlanMeta", JSON.stringify(response));
    navigate("/dashboard/diet-plan");
  };

  return (
    <div className="page">
      <form className="card large form-panel health-form" onSubmit={submit}>
        <div className="section-heading">
          <h2>Goal and Health Details</h2>
          <p>Set your profile once to generate your first monthly meal plan.</p>
        </div>

        <div className="field">
          <div className="field-label">Goal</div>
          <div className="segmented">
            <button
              className={form.goalType === "WEIGHT_LOSS" ? "is-active" : ""}
              type="button"
              onClick={() => setForm({ ...form, goalType: "WEIGHT_LOSS" })}
            >
              Weight loss
            </button>
            <button
              className={form.goalType === "WEIGHT_GAIN" ? "is-active" : ""}
              type="button"
              onClick={() => setForm({ ...form, goalType: "WEIGHT_GAIN" })}
            >
              Weight gain
            </button>
          </div>
        </div>

        <div className="field-grid">
          <label className="field">
            <div className="field-label">Age</div>
            <input
              type="number"
              inputMode="numeric"
              min={1}
              placeholder="e.g. 25"
              value={form.age}
              onChange={(e) => setForm({ ...form, age: Number(e.target.value) })}
              required
            />
          </label>

          <label className="field">
            <div className="field-label">Gender</div>
            <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </label>

          <label className="field">
            <div className="field-label">Activity level</div>
            <select value={form.activityLevel} onChange={(e) => setForm({ ...form, activityLevel: e.target.value })}>
              <option value="LOW">Low</option>
              <option value="MODERATE">Moderate</option>
              <option value="HIGH">High</option>
            </select>
          </label>

          <label className="field">
            <div className="field-label">Height (cm)</div>
            <input
              type="number"
              inputMode="decimal"
              min={1}
              placeholder="e.g. 170"
              value={form.heightCm}
              onChange={(e) => setForm({ ...form, heightCm: Number(e.target.value) })}
              required
            />
          </label>

          <label className="field">
            <div className="field-label">Weight (kg)</div>
            <input
              type="number"
              inputMode="decimal"
              min={1}
              placeholder="e.g. 70"
              value={form.weightKg}
              onChange={(e) => setForm({ ...form, weightKg: Number(e.target.value) })}
              required
            />
          </label>

          <div className="field bmi-card" aria-label="Body mass index">
            <div className="field-label">BMI</div>
            <div className="bmi-value">
              <strong>{bmi}</strong>
              <span className="bmi-hint">Auto-calculated from height & weight</span>
            </div>
          </div>
        </div>

        <div className="form-actions">
          <button className="btn-primary" type="submit">
            Save and Continue
          </button>
        </div>
      </form>
    </div>
  );
}
