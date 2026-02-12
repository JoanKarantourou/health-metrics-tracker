/**
 * Health Indicator Service
 * Handles all API calls related to health indicators
 */

import apiClient from "./api";

const indicatorService = {
  getAll: () => {
    return apiClient.get("/indicators");
  },

  getById: (id) => {
    return apiClient.get(`/indicators/${id}`);
  },

  getByCategory: (category) => {
    return apiClient.get(`/indicators/category/${category}`);
  },

  create: (indicatorData) => {
    return apiClient.post("/indicators", indicatorData);
  },

  update: (id, indicatorData) => {
    return apiClient.put(`/indicators/${id}`, indicatorData);
  },

  delete: (id) => {
    return apiClient.delete(`/indicators/${id}`);
  },
};

export default indicatorService;
