/**
 * Data Value Service
 * Handles all API calls related to data values
 */

import apiClient from "./api";

const dataValueService = {
  submit: (dataValueRequest) => {
    return apiClient.post("/data-values", dataValueRequest);
  },

  getByFacility: (facilityId) => {
    return apiClient.get(`/data-values/facility/${facilityId}`);
  },

  getByFacilityAndPeriod: (facilityId, startDate, endDate) => {
    return apiClient.get(`/data-values/facility/${facilityId}/period`, {
      params: { startDate, endDate },
    });
  },

  getByIndicator: (indicatorId) => {
    return apiClient.get(`/data-values/indicator/${indicatorId}`);
  },

  aggregateByRegion: (indicatorId, region, startDate, endDate) => {
    const params = {
      indicatorId,
      startDate,
      endDate,
    };

    if (region) {
      params.region = region;
    }

    return apiClient.get("/data-values/aggregate/region", { params });
  },

  getTotal: (indicatorId, startDate, endDate) => {
    return apiClient.get("/data-values/aggregate/total", {
      params: { indicatorId, startDate, endDate },
    });
  },

  getAverage: (indicatorId, startDate, endDate) => {
    return apiClient.get("/data-values/aggregate/average", {
      params: { indicatorId, startDate, endDate },
    });
  },
};

export default dataValueService;
