import { BrowserRouter, NavLink, Navigate, Route, Routes } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import WorkflowDetails from "./pages/WorkflowDetails";
import Workflows from "./pages/Workflows";
import RecoveryPage from "./pages/RecoveryPage";
import EventsTesterPage from "./pages/EventsTesterPage";
import AuthPage from "./pages/AuthPage";
import "./App.css";

const isAuthed = () => localStorage.getItem("sfd_admin") === "1";

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  if (!isAuthed()) return <Navigate to="/auth" replace />;
  return children;
};

function App() {
  const authed = isAuthed();

  return (
    <BrowserRouter>
      <header className="topbar">
        <div className="topbar-inner">
          <div className="brand">Silent Failure Detector</div>
          <nav className="nav">
            <NavLink to="/" className="nav-link">
              Dashboard
            </NavLink>
            <NavLink to="/workflows" className="nav-link">
              Workflows
            </NavLink>
            <NavLink to="/events" className="nav-link">
              Events Lab
            </NavLink>
            {!authed && (
              <NavLink to="/auth" className="nav-link">
                Admin Login
              </NavLink>
            )}
            {authed && (
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  localStorage.removeItem("sfd_admin");
                  window.location.href = "/";
                }}
              >
                Log out
              </button>
            )}
          </nav>
        </div>
      </header>

      <main className="app-shell">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/auth" element={<AuthPage />} />
          <Route
            path="/workflows"
            element={
              <ProtectedRoute>
                <Workflows />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workflows/:id"
            element={
              <ProtectedRoute>
                <WorkflowDetails />
              </ProtectedRoute>
            }
          />
          <Route
            path="/workflows/:workflowId/recovery/:stepName"
            element={
              <ProtectedRoute>
                <RecoveryPage />
              </ProtectedRoute>
            }
          />
          <Route path="/events" element={<EventsTesterPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
