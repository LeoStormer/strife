import axios from "axios";
import Cookies from "js-cookie";

const BACKEND_BASE_URL = process.env.REACT_APP_BACKEND_API_URL

if (!BACKEND_BASE_URL) {
  throw new Error("BACKEND_BASE_URL is not defined.")
}

const api = axios.create({
  baseURL: BACKEND_BASE_URL,
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
