/**
 * Main App Component
 * Root component with routing configuration and lazy loading
 */

import React, { Suspense, lazy } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";

// Import components
import Navigation from "./components/common/Navigation";
import ErrorBoundary from "./components/common/ErrorBoundary";

// Lazy load pages for better initial load performance
const Dashboard = lazy(() => import("./pages/Dashboard"));
const FacilitiesPage = lazy(() => import("./pages/FacilitiesPage"));
const DataEntryPage = lazy(() => import("./pages/DataEntryPage"));

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <div className="App">
          {/* Navigation - shows on all pages */}
          <Navigation />

          {/* Main content area - changes based on route */}
          <main className="app-main">
            <Suspense
            fallback={
              <div className="loading-container">
                <div className="spinner"></div>
                <p>Loading...</p>
              </div>
            }
          >
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/facilities" element={<FacilitiesPage />} />
              <Route path="/data-entry" element={<DataEntryPage />} />
            </Routes>
            </Suspense>
          </main>

          {/* Footer - shows on all pages */}
          <footer className="app-footer">
            <p>Health Metrics Tracker © 2026 | Built with React & Spring Boot</p>
          </footer>
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
