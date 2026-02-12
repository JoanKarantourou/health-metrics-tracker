import React, { useEffect, useState } from "react";
import { facilityService } from "../../services/api";
import "./FacilityList.css";

/**
 * FacilityList Component
 * Displays a filterable, searchable, paginated list of health facilities.
 *
 * Features:
 * - Search by facility name or code
 * - Filter by region, type, and active status
 * - Pagination with configurable page size
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

  // State for pagination
  const [pagination, setPagination] = useState({
    currentPage: 0, // Current page number (0-indexed)
    pageSize: 10, // Items per page
    totalPages: 0, // Total number of pages
    totalElements: 0, // Total number of items
    sort: "name", // Sort field
    direction: "asc", // Sort direction
  });

  // Unique values for dropdown options
  const [regions, setRegions] = useState([]);
  const [types, setTypes] = useState([]);

  /**
   * Fetch facilities when component mounts or filters/pagination change.
   */
  useEffect(() => {
    fetchFacilities();
  }, [
    filters,
    pagination.currentPage,
    pagination.pageSize,
    pagination.sort,
    pagination.direction,
  ]);

  /**
   * Fetch facilities from API with current filters and pagination.
   */
  const fetchFacilities = async () => {
    try {
      setLoading(true);

      // Build query parameters
      const params = {
        page: pagination.currentPage,
        size: pagination.pageSize,
        sort: pagination.sort,
        direction: pagination.direction,
      };

      // Add filters (only if not empty)
      if (filters.search) params.search = filters.search;
      if (filters.region) params.region = filters.region;
      if (filters.type) params.type = filters.type;
      if (filters.active) params.active = filters.active === "true";

      // Make API call
      const response = await facilityService.getAll(params);

      // Response.data is now a Page object with pagination metadata
      setFacilities(response.data.content);

      // Update pagination state with metadata from server
      setPagination((prev) => ({
        ...prev,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements,
      }));

      // Extract unique regions and types for dropdowns (only on first load)
      if (regions.length === 0 && response.data.content.length > 0) {
        // Fetch all facilities without pagination to get unique values
        const allFacilitiesResponse = await facilityService.getAll({
          size: 1000,
        });
        const allFacilities =
          allFacilitiesResponse.data.content || allFacilitiesResponse.data;

        const uniqueRegions = [
          ...new Set(allFacilities.map((f) => f.region)),
        ].sort();
        const uniqueTypes = [
          ...new Set(allFacilities.map((f) => f.type)),
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
   * Reset to page 0 when filters change.
   */
  const handleFilterChange = (filterName, value) => {
    setFilters((prevFilters) => ({
      ...prevFilters,
      [filterName]: value,
    }));

    // Reset to first page when filters change
    setPagination((prev) => ({ ...prev, currentPage: 0 }));
  };

  /**
   * Clear all filters and reset to page 0.
   */
  const handleClearFilters = () => {
    setFilters({
      search: "",
      region: "",
      type: "",
      active: "",
    });
    setPagination((prev) => ({ ...prev, currentPage: 0 }));
  };

  /**
   * Change page size (items per page).
   */
  const handlePageSizeChange = (newSize) => {
    setPagination((prev) => ({
      ...prev,
      pageSize: parseInt(newSize),
      currentPage: 0, // Reset to first page
    }));
  };

  /**
   * Navigate to a specific page.
   */
  const handlePageChange = (newPage) => {
    setPagination((prev) => ({
      ...prev,
      currentPage: newPage,
    }));

    // Scroll to top when changing pages
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  /**
   * Check if any filters are currently active.
   */
  const hasActiveFilters = () => {
    return filters.search || filters.region || filters.type || filters.active;
  };

  /**
   * Generate array of page numbers for pagination buttons.
   * Shows max 7 page buttons with ellipsis for large page counts.
   */
  const getPageNumbers = () => {
    const { currentPage, totalPages } = pagination;
    const pageNumbers = [];

    if (totalPages <= 7) {
      // Show all pages if 7 or fewer
      for (let i = 0; i < totalPages; i++) {
        pageNumbers.push(i);
      }
    } else {
      // Show first page, last page, current page and 2 on each side
      pageNumbers.push(0); // First page

      if (currentPage > 3) {
        pageNumbers.push("...");
      }

      // Pages around current
      for (
        let i = Math.max(1, currentPage - 2);
        i <= Math.min(totalPages - 2, currentPage + 2);
        i++
      ) {
        pageNumbers.push(i);
      }

      if (currentPage < totalPages - 4) {
        pageNumbers.push("...");
      }

      pageNumbers.push(totalPages - 1); // Last page
    }

    return pageNumbers;
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

      {/* Results Info and Page Size Selector */}
      <div className="results-header">
        <div className="results-info">
          {loading ? (
            <span>Updating...</span>
          ) : (
            <span>
              Showing {pagination.currentPage * pagination.pageSize + 1} -{" "}
              {Math.min(
                (pagination.currentPage + 1) * pagination.pageSize,
                pagination.totalElements,
              )}{" "}
              of {pagination.totalElements}{" "}
              {pagination.totalElements === 1 ? "facility" : "facilities"}
              {hasActiveFilters() && " (filtered)"}
            </span>
          )}
        </div>

        {/* Page Size Selector */}
        <div className="page-size-selector">
          <label htmlFor="pageSize">Show:</label>
          <select
            id="pageSize"
            value={pagination.pageSize}
            onChange={(e) => handlePageSizeChange(e.target.value)}
            className="page-size-select"
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
          </select>
          <span>per page</span>
        </div>
      </div>

      {/* Facilities Table */}
      {facilities.length === 0 ? (
        <div className="no-results">
          No facilities found matching your criteria.
        </div>
      ) : (
        <>
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

          {/* Pagination Controls */}
          {pagination.totalPages > 1 && (
            <div className="pagination-container">
              {/* Previous Button */}
              <button
                className="pagination-btn"
                onClick={() => handlePageChange(pagination.currentPage - 1)}
                disabled={pagination.currentPage === 0}
              >
                « Previous
              </button>

              {/* Page Numbers */}
              <div className="page-numbers">
                {getPageNumbers().map((pageNum, index) =>
                  pageNum === "..." ? (
                    <span
                      key={`ellipsis-${index}`}
                      className="pagination-ellipsis"
                    >
                      ...
                    </span>
                  ) : (
                    <button
                      key={pageNum}
                      className={`page-number-btn ${pagination.currentPage === pageNum ? "active" : ""}`}
                      onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum + 1}
                    </button>
                  ),
                )}
              </div>

              {/* Next Button */}
              <button
                className="pagination-btn"
                onClick={() => handlePageChange(pagination.currentPage + 1)}
                disabled={pagination.currentPage === pagination.totalPages - 1}
              >
                Next »
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default FacilityList;
