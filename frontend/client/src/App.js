/**
 * Main App Component
 * Root component with routing configuration
 */

import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";

// Import components
import Navigation from "./components/common/Navigation";
import Dashboard from "./pages/Dashboard";
import FacilitiesPage from "./pages/FacilitiesPage";
import DataEntryPage from "./pages/DataEntryPage";

function App() {
  return (
    <Router>
      <div className="App">
        {/* Navigation - shows on all pages */}
        <Navigation />

        {/* Main content area - changes based on route */}
        <main className="app-main">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/facilities" element={<FacilitiesPage />} />
            <Route path="/data-entry" element={<DataEntryPage />} />
          </Routes>
        </main>

        {/* Footer - shows on all pages */}
        <footer className="app-footer">
          <p>Health Metrics Tracker © 2026 | Built with React & Spring Boot</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
