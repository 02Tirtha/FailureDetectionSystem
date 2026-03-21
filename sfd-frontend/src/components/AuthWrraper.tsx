import { useLocation, Navigate } from "react-router-dom";
import AuthPage from "../pages/AuthPage";
const getUserRole = () => localStorage.getItem("userRole");
const isLoggedIn = () => Boolean(localStorage.getItem("userEmail"));
const isAdmin = () => getUserRole() === "ADMIN";
const AuthWrapper = () => {
  const location = useLocation();
  const forceLogin = Boolean(location.state?.forceLogin);

  if (isLoggedIn() && !forceLogin) {
    const from = location.state?.from;

    return <Navigate to={from || (isAdmin() ? "/workflows" : "/events")} replace />;
  }

  return <AuthPage />;
};

export default AuthWrapper;
