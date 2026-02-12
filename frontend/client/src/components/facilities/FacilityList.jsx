import React, { useEffect, useState } from "react";
import { facilityService } from "../../services/api";
import "./FacilityList.css";

/**
 * FacilityList Component
 * Displays a filterable, searchable list of health facilities.
 *
 * Features:
 * - Search by facility name or code
 * - Filter by region, type, and active status
 * - Real-time filtering as user types/selects
 */
function FacilityList() {
  // State for facilities data
  const [facilities, setFacilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // State for filters
  const [filters, setFilters] = useState({
    search: "",
    region: "",
    type: "",
    active: "",
  });

  // Unique values for dropdown options (extracted from facilities)
  const [regions, setRegions] = useState([]);
  const [types, setTypes] = useState([]);

  /**
   * Fetch facilities when component mounts or filters change.
   * useEffect = runs side effects
   */
  useEffect(() => {
    fetchFacilities();
  }, [filters]); // Re-run when filters change

  /**
   * Fetch facilities from API with current filters.
   * Builds query string from non-empty filter values.
   */
  const fetchFacilities = async () => {
    try {
      setLoading(true);

      // Build query parameters (only include non-empty filters)
      const params = {};
      if (filters.search) params.search = filters.search;
      if (filters.region) params.region = filters.region;
      if (filters.type) params.type = filters.type;
      if (filters.active) params.active = filters.active === "true"; // Convert string to boolean

      // Make API call with filters
      const response = await facilityService.getAll(params);
      setFacilities(response.data);

      // Extract unique regions and types for dropdowns (only on first load)
      if (regions.length === 0) {
        const uniqueRegions = [
          ...new Set(response.data.map((f) => f.region)),
        ].sort();
        const uniqueTypes = [
          ...new Set(response.data.map((f) => f.type)),
        ].sort();
        setRegions(uniqueRegions);
        setTypes(uniqueTypes);
      }

      setError(null);
    } catch (err) {
      console.error("Error fetching facilities:", err);
      setError("Failed to load facilities. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle filter input changes.
   * Updates the filters state, which triggers useEffect to re-fetch data.
   */
  const handleFilterChange = (filterName, value) => {
    setFilters((prevFilters) => ({
      ...prevFilters,
      [filterName]: value,
    }));
  };

  /**
   * Clear all filters and reset to show all facilities.
   */
  const handleClearFilters = () => {
    setFilters({
      search: "",
      region: "",
      type: "",
      active: "",
    });
  };

  /**
   * Check if any filters are currently active.
   */
  const hasActiveFilters = () => {
    return filters.search || filters.region || filters.type || filters.active;
  };

  // Loading state
  if (loading && facilities.length === 0) {
    return (
      <div className="facility-list-container">
        <div className="loading">Loading facilities...</div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="facility-list-container">
        <div className="error">{error}</div>
      </div>
    );
  }

  return (
    <div className="facility-list-container">
      <h2>Health Facilities</h2>

      {/* Filter Section */}
      <div className="filters-section">
        {/* Search Box */}
        <div className="filter-group">
          <label htmlFor="search">Search:</label>
          <input
            id="search"
            type="text"
            placeholder="Search by name or code..."
            value={filters.search}
            onChange={(e) => handleFilterChange("search", e.target.value)}
            className="filter-input"
          />
        </div>

        {/* Region Filter */}
        <div className="filter-group">
          <label htmlFor="region">Region:</label>
          <select
            id="region"
            value={filters.region}
            onChange={(e) => handleFilterChange("region", e.target.value)}
            className="filter-select"
          >
            <option value="">All Regions</option>
            {regions.map((region) => (
              <option key={region} value={region}>
                {region}
              </option>
            ))}
          </select>
        </div>

        {/* Type Filter */}
        <div className="filter-group">
          <label htmlFor="type">Type:</label>
          <select
            id="type"
            value={filters.type}
            onChange={(e) => handleFilterChange("type", e.target.value)}
            className="filter-select"
          >
            <option value="">All Types</option>
            {types.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>

        {/* Active Status Filter */}
        <div className="filter-group">
          <label htmlFor="active">Status:</label>
          <select
            id="active"
            value={filters.active}
            onChange={(e) => handleFilterChange("active", e.target.value)}
            className="filter-select"
          >
            <option value="">All Statuses</option>
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>
        </div>

        {/* Clear Filters Button */}
        {hasActiveFilters() && (
          <button onClick={handleClearFilters} className="clear-filters-btn">
            Clear Filters
          </button>
        )}
      </div>

      {/* Results Count */}
      <div className="results-info">
        {loading ? (
          <span>Updating...</span>
        ) : (
          <span>
            Showing {facilities.length}{" "}
            {facilities.length === 1 ? "facility" : "facilities"}
            {hasActiveFilters() && " (filtered)"}
          </span>
        )}
      </div>

      {/* Facilities Table */}
      {facilities.length === 0 ? (
        <div className="no-results">
          No facilities found matching your criteria.
        </div>
      ) : (
        <div className="table-container">
          <table className="facilities-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Type</th>
                <th>Region</th>
                <th>District</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {facilities.map((facility) => (
                <tr key={facility.id}>
                  <td className="code-cell">{facility.code}</td>
                  <td className="name-cell">{facility.name}</td>
                  <td>{facility.type}</td>
                  <td>{facility.region}</td>
                  <td>{facility.district}</td>
                  <td>
                    <span
                      className={`status-badge ${facility.active ? "active" : "inactive"}`}
                    >
                      {facility.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <button className="btn-view">View</button>
                    <button className="btn-edit">Edit</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default FacilityList;
