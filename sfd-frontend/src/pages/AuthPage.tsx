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

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrors({});

    const url =
      mode === "login"
        ? "http://localhost:8080/api/auth/login"
        : "http://localhost:8080/api/auth/register";

    const body =
      mode === "login"
        ? { email, password }
        : { name, email, password, role };

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    if (!response.ok) {
      if (isJson) {
        const data = await response.json();
        if (data?.errors) {
          setErrors(data.errors);
          return;
        }
        if (data?.message) {
          setErrors({ form: data.message });
          return;
        }
      }

      const fallback = await response.text();
      setErrors({ form: fallback || "Something went wrong. Please try again." });
      return;
    }

    if (isJson) {
      const data = await response.json();
      if (data?.message === "Login Successful") {
        const resolvedRole =
          typeof data?.role === "string" && data.role.length > 0
            ? data.role
            : "USER";
        localStorage.setItem("userEmail", data.email || email);
        localStorage.setItem("userRole", resolvedRole);
        if (resolvedRole === "ADMIN") {
          window.location.href = from === "/events" ? "/workflows" : (from || "/workflows");
        } else {
          window.location.href = from === "/workflows" ? "/events" : (from || "/events");
        }

        return;
      }
      if (data?.message) {
        setErrors({ form: data.message });
        return;
      }
      setErrors({ form: "Unexpected response from server." });
      return;
    }

    const text = await response.text();

    if (text === "User Registered Successfully") {
      setShowSignupSuccess(true);
      setMode("login");
    } else if (text === "Login Successful") {
      localStorage.setItem("userEmail", email);
      localStorage.setItem("userRole", "USER");
      window.location.href = from || "/events";
    } else {
      setErrors({ form: text });
    }
  };

  return (
    <div className="page">
      <div className="panel" style={{ maxWidth: 520, margin: "0 auto" }}>
        <div className="hero">
          <div>
            <h2 className="page-title">Sign in to Continue</h2>
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
              {errors.name && (
                <div style={{ color: "#dc2626", fontSize: 12, marginTop: 6 }}>
                  {errors.name}
                </div>
              )}
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
            {errors.email && (
              <div style={{ color: "#dc2626", fontSize: 12, marginTop: 6 }}>
                {errors.email}
              </div>
            )}
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
            {mode === "signup" && (
              <div className="helper">
                8-10 chars, one uppercase, one number, one special character.
              </div>
            )}
            {errors.password && (
              <div style={{ color: "#dc2626", fontSize: 12, marginTop: 6 }}>
                {errors.password}
              </div>
            )}
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
            padding: 16,
          }}
          onClick={() => setShowSignupSuccess(false)}
        >
          <div
            style={{
              background: "#fff",
              borderRadius: 12,
              maxWidth: 420,
              width: "100%",
              padding: 24,
              boxShadow:
                "0 20px 60px rgba(0, 0, 0, 0.25), 0 2px 8px rgba(0, 0, 0, 0.15)",
              textAlign: "center",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div
              style={{
                width: 64,
                height: 64,
                margin: "0 auto 12px",
                borderRadius: "50%",
                border: "3px solid #2e7d32",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#2e7d32",
                fontSize: 28,
                fontWeight: 700,
              }}
            >
              OK
            </div>
            <h3 style={{ margin: "0 0 8px" }}>Account Created</h3>
            <p style={{ margin: "0 0 20px", color: "#555" }}>
              Your account is ready. Please log in.
            </p>
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
