import { useState } from "react";
import { useNavigate } from "react-router-dom";

const AuthPage = () => {
  const navigate = useNavigate();
  const [mode, setMode] = useState<"login" | "signup">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

 const handleSubmit = async (event: React.FormEvent) => {
  event.preventDefault();

  const url =
    mode === "login"
      ? "http://localhost:8080/api/auth/login"
      : "http://localhost:8080/api/auth/register";

  const body =
    mode === "login"
      ? { email, password }
      : { name, email, password };

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  const data = await response.text();

  if (data === "Login Successful") {
    localStorage.setItem("sfd_admin", "1");
    navigate("/workflows");
  } else if (data === "User Registered Successfully") {
    alert("Account created. Please login.");
    setMode("login");
  } else {
    alert(data);
  }
};

  return (
    <div className="page">
      <div className="panel" style={{ maxWidth: 520, margin: "0 auto" }}>
        <div className="hero">
          <div>
            <h2 className="page-title">Admin Access</h2>
            <p className="page-subtitle">
              Only admins can resolve failures. Create an admin account or sign in.
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
              <label>Admin Name</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Jane Admin"
                className="input"
                required
              />
            </div>
          )}

          <div>
            <label>Work Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@company.com"
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
              placeholder="••••••••"
              className="input"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary">
            {mode === "login" ? "Login as Admin" : "Create Admin Account"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AuthPage;
