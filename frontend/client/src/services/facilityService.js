/**
 * Facility Service
 * Handles all API calls related to facilities
 */

import apiClient from "./api";

const facilityService = {
  // Accepts optional params for filtering and pagination
  // params can include: page, size, sort, direction, search, region, type, active
  getAll: (params = {}) => {
    return apiClient.get("/facilities", { params });
  },

  getById: (id) => {
    return apiClient.get(`/facilities/${id}`);
  },

  getByRegion: (region) => {
    return apiClient.get(`/facilities/region/${region}`);
  },

  create: (facilityData) => {
    return apiClient.post("/facilities", facilityData);
  },

  update: (id, facilityData) => {
    return apiClient.put(`/facilities/${id}`, facilityData);
  },

  delete: (id) => {
    return apiClient.delete(`/facilities/${id}`);
  },
};

export default facilityService;
