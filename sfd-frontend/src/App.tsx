import { BrowserRouter, NavLink, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import WorkflowDetails from "./pages/WorkflowDetails";
import Workflows from "./pages/Workflows";
import RecoveryPage from "./pages/RecoveryPage";
import EventsTesterPage from "./pages/EventsTesterPage";
import AuthPage from "./pages/AuthPage";
import "./App.css";
import AuthWrapper from "./components/AuthWrraper";

const getUserRole = () => localStorage.getItem("userRole");
const isLoggedIn = () => Boolean(localStorage.getItem("userEmail"));
const isAdmin = () => getUserRole() === "ADMIN";

const AccessPrompt = ({
  title,
  message,
  loginLabel = "Go to Login",
}: {
  title: string;
  message: string;
  loginLabel?: string;
}) => {
  const navigate = useNavigate();
  return (
    <div className="page">
      <div className="modal-backdrop">
        <div className="panel modal-card">
          <h3 style={{ margin: "0 0 8px" }}>{title}</h3>
          <p style={{ margin: "0 0 20px", color: "#555" }}>{message}</p>
          <div className="hero-actions" style={{ justifyContent: "flex-end" }}>
            <button className="btn btn-ghost" onClick={() => navigate("/") }>
              Back to Dashboard
            </button>
            <button className="btn btn-primary" onClick={() => navigate("/auth", { state: { from: window.location.pathname } })}>
              {loginLabel}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

const TopBar = () => {
  const navigate = useNavigate();
  const authed = isLoggedIn();

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <div className="brand brand-with-back">
          <button
            type="button"
            className="btn btn-ghost back-arrow"
            onClick={() => navigate(-1)}
            aria-label="Go back"
          >
            ←
          </button>
          <span>Failure Detector</span>
        </div>
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
              Login
            </NavLink>
          )}
          {authed && (
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => {
                localStorage.removeItem("userEmail");
                localStorage.removeItem("userRole");
                window.location.href = "/";
              }}
            >
              Log out
            </button>
          )}
        </nav>
      </div>
    </header>
  );
};

function App() {
  return (
    <BrowserRouter>
      <TopBar />

      <main className="app-shell">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/auth" element={<AuthWrapper />} />
          <Route
            path="/workflows"
            element={
              isLoggedIn() ? (
                isAdmin() ? (
                  <Workflows />
                ) : (
                  <AccessPrompt
                    title="Access Denied"
                    message="Only admins can access workflows."
                  />
                )
              ) : (
                <AccessPrompt
                  title="Login Required"
                  message="Please log in to access workflows."
                />
              )
            }
          />
          <Route
            path="/workflows/:id"
            element={
              isLoggedIn() ? (
                <WorkflowDetails />
              ) : (
                <AccessPrompt
                  title="Login Required"
                  message="Please log in to access workflows."
                />
              )
            }
          />
          <Route
            path="/workflows/:workflowId/recovery/:stepName"
            element={
              isLoggedIn() ? (
                <RecoveryPage />
              ) : (
                <AccessPrompt
                  title="Login Required"
                  message="Please log in to access recovery actions."
                />
              )
            }
          />
                    <Route
            path="/events"
            element={
              isLoggedIn() ? (
                isAdmin() ? (
                  <AccessPrompt
                    title="Access Denied"
                    message="Admins cannot access Events Lab."
                  />
                ) : (
                  <EventsTesterPage />
                )
              ) : (
                <AccessPrompt
                  title="Login Required"
                  message="Please log in to create or submit events."
                />
              )
            }
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
