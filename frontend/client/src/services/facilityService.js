/**
 * Facility Service
 * Handles all API calls related to facilities
 */

import apiClient from "./api";

const facilityService = {
  getAll: () => {
    return apiClient.get("/facilities");
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
