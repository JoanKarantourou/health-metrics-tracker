/**
 * API Service Layer
 * Centralizes all HTTP requests to the Spring Boot backend
 */

import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

apiClient.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error("❌ Request Error:", error);
    return Promise.reject(error);
  },
);

apiClient.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.config.url}`, response.status);
    return response;
  },
  (error) => {
    if (error.response) {
      console.error(
        "❌ API Error Response:",
        error.response.status,
        error.response.data,
      );
    } else if (error.request) {
      console.error("❌ Network Error: No response received");
    } else {
      console.error("❌ Error:", error.message);
    }
    return Promise.reject(error);
  },
);

export const facilityService = {
  /**
   * Get all facilities with optional filters.
   * @param {Object} params - Optional query parameters (search, region, type, active)
   * @returns {Promise} Axios response with facility data
   */
  getAll: (params = {}) => apiClient.get("/facilities", { params }),

  getById: (id) => apiClient.get(`/facilities/${id}`),
  create: (data) => apiClient.post("/facilities", data),
  update: (id, data) => apiClient.put(`/facilities/${id}`, data),
  delete: (id) => apiClient.delete(`/facilities/${id}`),
};

export default apiClient;
