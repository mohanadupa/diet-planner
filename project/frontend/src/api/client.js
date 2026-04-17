const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export async function apiRequest(path, method = "GET", body) {
  const token = localStorage.getItem("authToken");
  const options = {
    method,
    headers: { "Content-Type": "application/json" }
  };
  if (token) {
    options.headers.Authorization = `Bearer ${token}`;
  }
  if (body) {
    options.body = JSON.stringify(body);
  }
  const response = await fetch(`${BASE_URL}${path}`, options);
  let payload = {};
  try {
    payload = await response.json();
  } catch (error) {
    payload = {};
  }
  if (!response.ok) {
    throw new Error(payload.error || `Request failed (${response.status})`);
  }
  return payload;
}
