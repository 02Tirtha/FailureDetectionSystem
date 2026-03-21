import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";

const AuthPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [mode, setMode] = useState<"login" | "signup">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState<"ADMIN" | "USER">("USER");

  const from = location.state?.from;

  const [errors, setErrors] = useState<{
    name?: string;
    email?: string;
    password?: string;
    form?: string;
  }>({});


  const [showSignupSuccess, setShowSignupSuccess] = useState(false);

  const getErrorMessage = (data: unknown) => {
    if (typeof data === "string" && data.trim()) {
      return data;
    }

    if (data && typeof data === "object") {
      const maybeMessage = (data as { message?: unknown }).message;
      if (typeof maybeMessage === "string" && maybeMessage.trim()) {
        return maybeMessage;
      }

      const maybeError = (data as { error?: unknown }).error;
      if (typeof maybeError === "string" && maybeError.trim()) {
        return maybeError;
      }

      const maybeErrors = (data as { errors?: unknown }).errors;
      if (Array.isArray(maybeErrors) && maybeErrors.length > 0) {
        const messages = maybeErrors
          .map((entry) => {
            if (typeof entry === "string") return entry;
            if (entry && typeof entry === "object") {
              const fieldMessage =
                (entry as { defaultMessage?: unknown; message?: unknown }).defaultMessage ??
                (entry as { message?: unknown }).message;
              if (typeof fieldMessage === "string") return fieldMessage;
            }
            return "";
          })
          .filter((message) => message);

        if (messages.length > 0) {
          return messages.join(", ");
        }
      }

      if (maybeErrors && typeof maybeErrors === "object" && !Array.isArray(maybeErrors)) {
        const messages = Object.values(maybeErrors)
          .map((entry) => (typeof entry === "string" ? entry : ""))
          .filter((message) => message);

        if (messages.length > 0) {
          return messages.join(", ");
        }
      }
    }

    return "Request failed. Please try again.";
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrors({});
      setShowSignupSuccess(false); //  reset popup

    try {
      const url =
        mode === "login"
          ? `${import.meta.env.VITE_API_URL}/api/auth/login`
          : `${import.meta.env.VITE_API_URL}/api/auth/register`;

      const body =
        mode === "login"
          ? { email, password }
          : { name, email, password, role };

      const res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
      });

      const contentType = res.headers.get("content-type") || "";
      const isJson = contentType.includes("application/json");
      const data = isJson ? await res.json() : await res.text();

      if (!res.ok) {
        const errorMessage = getErrorMessage(data);
        const lowerMessage = errorMessage.toLowerCase();

        if (
          mode === "signup" &&
          (res.status === 409 ||
            lowerMessage.includes("user already exists") ||
            lowerMessage.includes("email already exists") ||
            res.status >= 500)
        ) {
          setErrors({ email: "Email already exists." });
        } else {
          setErrors({ form: errorMessage });
        }

        return;
      }
      // ✅ LOGIN SUCCESS
      if (mode === "login") {
        if (!data || typeof data !== "object") {
          setErrors({ form: "Unexpected login response. Please try again." });
          return;
        }

        const userRole = (data as { role?: string }).role || "USER";
        const responseEmail = (data as { email?: string }).email || email;

        localStorage.setItem("userEmail", responseEmail);
        localStorage.setItem("userRole", userRole);

        const isWorkflowsRoot =
          typeof from === "string" && (from === "/workflows" || from.startsWith("/workflows?"));
        const isEventsRoute = typeof from === "string" && from.startsWith("/events");
        const canUseFrom =
          (!isWorkflowsRoot || userRole === "ADMIN") &&
          (!isEventsRoute || userRole !== "ADMIN");

        const redirectPath =
          (canUseFrom ? from : null) || (userRole === "ADMIN" ? "/workflows" : "/events");

        navigate(redirectPath, { replace: true });
        window.dispatchEvent(new Event("auth:changed"));
      }

      // ✅ SIGNUP SUCCESS
      if (mode === "signup") {
        setShowSignupSuccess(true);
        setMode("login");
      }
    } catch (err) {
      console.error(err);
      
      setErrors({ form: "Server error. Try again later." });
    }
  };

  return (
    <div className="page">
      <div className="panel" style={{ maxWidth: 520, margin: "0 auto" }}>
        <div className="hero">
          <div>
            <h2 className="page-title">
              {mode === "login" ? "Login" : "Create Account"}
            </h2>
            <p className="page-subtitle">
              Users can submit events. Admins can resolve failures.
            </p>
          </div>
        </div>

        <div className="hero-actions">
          <button
            type="button"
            className={`btn ${mode === "login" ? "btn-primary" : "btn-ghost"}`}
            onClick={() => setMode("login")}
          >
            Login
          </button>
          <button
            type="button"
            className={`btn ${mode === "signup" ? "btn-primary" : "btn-ghost"}`}
            onClick={() => setMode("signup")}
          >
            Sign Up
          </button>
        </div>

        <form onSubmit={handleSubmit} className="form-grid" style={{ marginTop: 16 }}>
          {mode === "signup" && (
            <div>
              <label>Name</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Jane Doe"
                className="input"
                required
              />
            </div>
          )}

          {mode === "signup" && (
            <div>
              <label>Role</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as "ADMIN" | "USER")}
                className="select"
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          )}
          {errors.email && (
            <div style={{ color: "#dc2626", fontSize: 13 }}>
              {errors.email}
            </div>
          )}
          <div>
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@company.com"
              className="input"
              required
            />
          </div>

          <div>
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="********"
              className="input"
              required
            />
          </div>

          {errors.form && (
            <div style={{ color: "#dc2626", fontSize: 13 }}>
              {errors.form}
            </div>
          )}

          <button type="submit" className="btn btn-primary">
            {mode === "login" ? "Login" : "Create Account"}
          </button>
        </form>
      </div>

      {showSignupSuccess && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0, 0, 0, 0.45)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
          }}
          onClick={() => setShowSignupSuccess(false)}
        >
          <div
            style={{
              background: "#fff",
              borderRadius: 12,
              padding: 24,
              textAlign: "center",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3>Account Created</h3>
            <p>Please login now.</p>
            <button
              className="btn btn-primary"
              onClick={() => setShowSignupSuccess(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AuthPage;

