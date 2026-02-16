import axios from "axios";
import axiosRetry from "axios-retry";
import Cookies from "js-cookie";

const BACKEND_API_URL = process.env.NEXT_PUBLIC_BACKEND_API_URL

if (!BACKEND_API_URL) {
  throw new Error("BACKEND_API_URL is not defined.")
}

const api = axios.create({
  baseURL: BACKEND_API_URL,
  timeout: 5000,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

axiosRetry(api, {
  retries: 3,
  retryDelay: axiosRetry.exponentialDelay,
  shouldResetTimeout: true,
});

api.interceptors.request.use((config) => {
  const csrfToken = Cookies.get("XSRF-TOKEN");
  if (csrfToken) {
    config.headers["X-XSRF-TOKEN"] = csrfToken;
  }
  return config;
});

export default api;
