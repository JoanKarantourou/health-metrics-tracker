/**
 * Dashboard Page
 * Main landing page with statistics and data visualizations
 *
 * Features:
 * - Summary statistics cards
 * - Line chart showing data trends
 * - Bar chart comparing regions
 * - Real data from backend API
 */

import React, { useEffect, useState } from "react";
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import {
  facilityService,
  indicatorService,
  dataValueService,
} from "../services";
import "./Dashboard.css";

function Dashboard() {
  // State for summary statistics
  const [stats, setStats] = useState({
    totalFacilities: 0,
    activeFacilities: 0,
    totalIndicators: 0,
    totalDataValues: 0,
  });

  // State for chart data
  const [indicators, setIndicators] = useState([]);
  const [selectedIndicator, setSelectedIndicator] = useState(null);
  const [timeSeriesData, setTimeSeriesData] = useState([]);
  const [regionalData, setRegionalData] = useState([]);

  // State for UI
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Load all dashboard data when component mounts
   */
  useEffect(() => {
    fetchDashboardData();
  }, []);

  /**
   * Fetch data for selected indicator
   */
  useEffect(() => {
    if (selectedIndicator) {
      fetchIndicatorData(selectedIndicator);
    }
  }, [selectedIndicator]);

  /**
   * Fetch initial dashboard data
   */
  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch facilities and indicators in parallel
      // facilityService.getAll() returns a Page object, so we use size=1000 to get all
      const [facilitiesResponse, indicatorsResponse] = await Promise.all([
        facilityService.getAll({ size: 1000 }),
        indicatorService.getAll(),
      ]);

      // facilityService returns a Page object with .content array
      const facilities =
        facilitiesResponse.data.content || facilitiesResponse.data;
      const indicators = indicatorsResponse.data;

      // Calculate statistics
      setStats({
        totalFacilities: facilities.length,
        activeFacilities: facilities.filter((f) => f.active).length,
        totalIndicators: indicators.filter((i) => i.active).length,
        totalDataValues: 0,
      });

      // Set indicators for dropdown
      setIndicators(indicators.filter((i) => i.active));

      // Auto-select first indicator
      if (indicators.length > 0) {
        setSelectedIndicator(indicators[0].id);
      }

      console.log("✅ Dashboard data loaded");
    } catch (err) {
      console.error("❌ Error loading dashboard data:", err);
      setError(
        "Failed to load dashboard data. Please ensure the backend is running.",
      );
    } finally {
      setLoading(false);
    }
  };

  /**
   * Fetch data for a specific indicator
   */
  const fetchIndicatorData = async (indicatorId) => {
    try {
      // Fetch all data values for this indicator
      const response = await dataValueService.getByIndicator(indicatorId);
      const dataValues = response.data;

      // Process data for time series chart
      const timeData = processTimeSeriesData(dataValues);
      setTimeSeriesData(timeData);

      // Process data for regional comparison
      const regionData = processRegionalData(dataValues);
      setRegionalData(regionData);

      console.log("✅ Indicator data loaded:", dataValues.length, "records");
    } catch (err) {
      console.error("❌ Error loading indicator data:", err);
      setTimeSeriesData([]);
      setRegionalData([]);
    }
  };

  /**
   * Process data values into time series format for line chart
   */
  const processTimeSeriesData = (dataValues) => {
    // Group by month and sum values
    const monthlyData = {};

    dataValues.forEach((dv) => {
      const date = new Date(dv.periodStart);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;

      if (!monthlyData[monthKey]) {
        monthlyData[monthKey] = {
          month: monthKey,
          value: 0,
          count: 0,
        };
      }

      monthlyData[monthKey].value += parseFloat(dv.value);
      monthlyData[monthKey].count += 1;
    });

    // Convert to array and sort by month
    return Object.values(monthlyData)
      .sort((a, b) => a.month.localeCompare(b.month))
      .map((item) => ({
        month: item.month,
        value: Math.round(item.value),
      }));
  };

  /**
   * Process data values into regional comparison format for bar chart
   */
  const processRegionalData = (dataValues) => {
    // Group by region and sum values
    const regionalTotals = {};

    dataValues.forEach((dv) => {
      const region = dv.facilityRegion || "Unknown";

      if (!regionalTotals[region]) {
        regionalTotals[region] = {
          region: region,
          total: 0,
          count: 0,
        };
      }

      regionalTotals[region].total += parseFloat(dv.value);
      regionalTotals[region].count += 1;
    });

    // Convert to array and sort by total
    return Object.values(regionalTotals)
      .sort((a, b) => b.total - a.total)
      .map((item) => ({
        region: item.region,
        value: Math.round(item.total),
      }));
  };

  /**
   * Handle indicator selection change
   */
  const handleIndicatorChange = (e) => {
    setSelectedIndicator(Number(e.target.value));
  };

  /**
   * Render loading state
   */
  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading dashboard...</p>
        </div>
      </div>
    );
  }

  /**
   * Render error state
   */
  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-message">
          <h3>⚠️ Error</h3>
          <p>{error}</p>
          <button onClick={fetchDashboardData} className="btn-retry">
            🔄 Retry
          </button>
        </div>
      </div>
    );
  }

  /**
   * Get selected indicator details
   */
  const selectedIndicatorObj = indicators.find(
    (i) => i.id === selectedIndicator,
  );

  /**
   * Render main dashboard
   */
  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header">
        <h2>📊 Dashboard</h2>
        <p className="dashboard-subtitle">
          Overview of health metrics across facilities
        </p>
      </div>

      {/* Summary Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">🏥</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalFacilities}</div>
            <div className="stat-label">Total Facilities</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <div className="stat-value">{stats.activeFacilities}</div>
            <div className="stat-label">Active Facilities</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📋</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalIndicators}</div>
            <div className="stat-label">Health Indicators</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📈</div>
          <div className="stat-content">
            <div className="stat-value">{timeSeriesData.length}</div>
            <div className="stat-label">Data Points (Selected)</div>
          </div>
        </div>
      </div>

      {/* Indicator Selection */}
      <div className="chart-controls">
        <label htmlFor="indicator-select">
          <strong>Select Health Indicator:</strong>
        </label>
        <select
          id="indicator-select"
          value={selectedIndicator || ""}
          onChange={handleIndicatorChange}
          className="indicator-select"
        >
          {indicators.map((indicator) => (
            <option key={indicator.id} value={indicator.id}>
              {indicator.name} ({indicator.unit})
            </option>
          ))}
        </select>
      </div>

      {/* Charts Section */}
      <div className="charts-grid">
        {/* Time Series Line Chart */}
        <div className="chart-card">
          <h3 className="chart-title">
            📈 Trend Over Time
            {selectedIndicatorObj && (
              <span className="chart-subtitle">
                {selectedIndicatorObj.name}
              </span>
            )}
          </h3>

          {timeSeriesData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={timeSeriesData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#667eea"
                  strokeWidth={2}
                  name={selectedIndicatorObj?.unit || "Value"}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="no-data">
              <p>No time series data available for this indicator</p>
            </div>
          )}
        </div>

        {/* Regional Comparison Bar Chart */}
        <div className="chart-card">
          <h3 className="chart-title">
            📊 Regional Comparison
            {selectedIndicatorObj && (
              <span className="chart-subtitle">
                {selectedIndicatorObj.name}
              </span>
            )}
          </h3>

          {regionalData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={regionalData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="region"
                  tick={{ fontSize: 12 }}
                  angle={-45}
                  textAnchor="end"
                  height={80}
                />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Legend />
                <Bar
                  dataKey="value"
                  fill="#764ba2"
                  name={selectedIndicatorObj?.unit || "Value"}
                />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="no-data">
              <p>No regional data available for this indicator</p>
            </div>
          )}
        </div>
      </div>

      {/* Info Section */}
      <div className="dashboard-info">
        <div className="info-card">
          <h4>💡 Quick Tips</h4>
          <ul>
            <li>
              Select different indicators to see their trends and regional
              distribution
            </li>
            <li>
              Navigate to <strong>Facilities</strong> to manage health
              facilities
            </li>
            <li>
              Use <strong>Data Entry</strong> to submit new health metrics
            </li>
            <li>Charts update automatically based on available data</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
