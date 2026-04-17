import { Navigate, Route, Routes } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import AuthPage from "./pages/AuthPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import HealthPage from "./pages/HealthPage";
import DietPlanPage from "./pages/DietPlanPage";
import ProfilePage from "./pages/ProfilePage";
import SettingsPage from "./pages/SettingsPage";
import DashboardLayout from "./layout/DashboardLayout";
import { useAppContext } from "./context/AppContext";

function Protected({ children }) {
  const { user } = useAppContext();
  return user ? children : <Navigate to="/auth" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/auth" element={<AuthPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route
        path="/health"
        element={
          <Protected>
            <HealthPage />
          </Protected>
        }
      />
      <Route
        path="/dashboard"
        element={
          <Protected>
            <DashboardLayout />
          </Protected>
        }
      >
        <Route path="diet-plan" element={<DietPlanPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
