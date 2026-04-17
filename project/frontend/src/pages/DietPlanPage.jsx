import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { useAppContext } from "../context/AppContext";

export default function DietPlanPage() {
  const { user } = useAppContext();
  const [plans, setPlans] = useState([]);
  const [week, setWeek] = useState(1);
  const [meta, setMeta] = useState(null);

  const loadPlans = async () => {
    const data = await apiRequest(`/diet-plans?userId=${user.userId}`);
    setPlans(data);
    const metadata = await apiRequest(`/diet-plans/meta?userId=${user.userId}`);
    setMeta(metadata);
  };

  useEffect(() => {
    if (user) loadPlans();
  }, [user]);

  const currentWeek = plans.find((p) => p.weekNumber === week);

  const toggleDone = async (day, completed) => {
    await apiRequest("/diet-meals/toggle", "PUT", {
      userId: user.userId,
      weekNumber: week,
      day,
      completed: !completed
    });
    await loadPlans();
  };

  return (
    <div className="panel">
      <div className="section-heading">
        <h2>Diet Plan</h2>
        <p>Weekly menu up to one month. Update profile after each month for a new plan.</p>
      </div>
      {meta?.usedFallbackPlan && (
        <p className="warning">External API unavailable, fallback meal template is in use.</p>
      )}
      <div className="filter-row">
        <label className="inline-label">
          <span>Week</span>
          <select value={week} onChange={(e) => setWeek(Number(e.target.value))}>
            {[1, 2, 3, 4].map((w) => (
              <option key={w} value={w}>
                Week {w}
              </option>
            ))}
          </select>
        </label>
      </div>
      <table className="data-table">
        <thead>
          <tr>
            <th>Day</th>
            <th>Morning</th>
            <th>Afternoon</th>
            <th>Evening</th>
            <th>Night</th>
            <th>Completed</th>
          </tr>
        </thead>
        <tbody>
          {currentWeek?.meals?.map((meal) => (
            <tr key={meal.day}>
              <td>{meal.day}</td>
              <td>{meal.morning}</td>
              <td>{meal.afternoon}</td>
              <td>{meal.evening}</td>
              <td>{meal.night}</td>
              <td>
                <button
                  className={meal.completed ? "btn-success" : "btn-primary"}
                  onClick={() => toggleDone(meal.day, meal.completed)}
                >
                  {meal.completed ? "Done" : "Mark Done"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
