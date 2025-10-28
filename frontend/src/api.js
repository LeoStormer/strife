import axios from "axios";
import Cookies from "js-cookie";

const api = axios.create({
  baseURL: process.env.REACT_APP_BACKEND_BASE_URL || "http://localhost:8080",
  timeout: 5000,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const csrfToken = Cookies.get("XSRF-TOKEN");
  if (csrfToken) {
    config.headers["X-XSRF-TOKEN"] = csrfToken;
  }
  return config;
});

export default api;
