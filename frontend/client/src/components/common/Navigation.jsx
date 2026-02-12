/**
 * Navigation Component
 * Main navigation menu for the application
 * Similar to a _Layout.cshtml navbar in ASP.NET MVC
 */

import React from "react";
import { Link, useLocation } from "react-router-dom";
import "./Navigation.css";

function Navigation() {
  // Get current location to highlight active link
  const location = useLocation();

  /**
   * Check if a route is active
   */
  const isActive = (path) => {
    return location.pathname === path;
  };

  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <span className="nav-logo">🏥</span>
          <span className="nav-title">Health Metrics Tracker</span>
        </div>

        <ul className="nav-menu">
          <li className="nav-item">
            <Link
              to="/"
              className={`nav-link ${isActive("/") ? "active" : ""}`}
            >
              <span className="nav-icon">📊</span>
              Dashboard
            </Link>
          </li>
          <li className="nav-item">
            <Link
              to="/facilities"
              className={`nav-link ${isActive("/facilities") ? "active" : ""}`}
            >
              <span className="nav-icon">🏥</span>
              Facilities
            </Link>
          </li>
          <li className="nav-item">
            <Link
              to="/data-entry"
              className={`nav-link ${isActive("/data-entry") ? "active" : ""}`}
            >
              <span className="nav-icon">📝</span>
              Data Entry
            </Link>
          </li>
        </ul>
      </div>
    </nav>
  );
}

export default Navigation;
