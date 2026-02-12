/**
 * Dashboard Page
 * Main landing page showing overview and statistics
 * Will be enhanced with charts in the next step
 */

import React from "react";
import "./Dashboard.css";

function Dashboard() {
  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h2>📊 Dashboard</h2>
        <p className="dashboard-subtitle">Overview of health metrics</p>
      </div>

      <div className="dashboard-welcome">
        <div className="welcome-card">
          <h3>👋 Welcome to Health Metrics Tracker!</h3>
          <p>
            This system helps you track and visualize health facility data
            across Greece.
          </p>
          <div className="feature-list">
            <div className="feature-item">
              <span className="feature-icon">🏥</span>
              <div>
                <strong>Manage Facilities</strong>
                <p>View and manage health facilities across all regions</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">📝</span>
              <div>
                <strong>Submit Data</strong>
                <p>Enter health metrics data for facilities and indicators</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">📈</span>
              <div>
                <strong>Analyze Trends</strong>
                <p>
                  Visualize data with charts and aggregations (coming soon!)
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="quick-stats">
          <h4>Quick Stats</h4>
          <p className="stats-note">
            Navigate to Facilities or Data Entry to see real data!
          </p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
