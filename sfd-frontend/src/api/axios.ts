import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
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
