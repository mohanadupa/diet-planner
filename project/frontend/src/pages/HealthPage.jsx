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
      <form className="card large form-panel" onSubmit={submit}>
        <div className="section-heading">
          <h2>Goal and Health Details</h2>
          <p>Set your profile once to generate your first monthly meal plan.</p>
        </div>
        <div className="row">
          <button
            className={form.goalType === "WEIGHT_GAIN" ? "btn-primary" : ""}
            type="button"
            onClick={() => setForm({ ...form, goalType: "WEIGHT_GAIN" })}
          >
            Weight Gain
          </button>
          <button
            className={form.goalType === "WEIGHT_LOSS" ? "btn-primary" : ""}
            type="button"
            onClick={() => setForm({ ...form, goalType: "WEIGHT_LOSS" })}
          >
            Weight Loss
          </button>
        </div>
        <input type="number" placeholder="Age" value={form.age} onChange={(e) => setForm({ ...form, age: Number(e.target.value) })} />
        <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
          <option value="MALE">Male</option>
          <option value="FEMALE">Female</option>
          <option value="OTHER">Other</option>
        </select>
        <select value={form.activityLevel} onChange={(e) => setForm({ ...form, activityLevel: e.target.value })}>
          <option value="LOW">Low Activity</option>
          <option value="MODERATE">Moderate Activity</option>
          <option value="HIGH">High Activity</option>
        </select>
        <input type="number" placeholder="Height (cm)" value={form.heightCm} onChange={(e) => setForm({ ...form, heightCm: Number(e.target.value) })} />
        <input type="number" placeholder="Weight (kg)" value={form.weightKg} onChange={(e) => setForm({ ...form, weightKg: Number(e.target.value) })} />
        <p className="bmi-pill">
          BMI: <strong>{bmi}</strong>
        </p>
        <button className="btn-primary" type="submit">
          Save and Continue
        </button>
      </form>
    </div>
  );
}
