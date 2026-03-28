import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL;

const api = axios.create({
  baseURL: `${API_URL}/api`,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const userEmail = localStorage.getItem("userEmail");
  if (userEmail) {
    config.headers = config.headers || {};
    config.headers["X-User-Email"] = userEmail;
  }
  return config;
});

export default api;
