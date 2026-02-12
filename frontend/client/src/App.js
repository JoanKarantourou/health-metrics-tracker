/**
 * Main App Component
 * Root component of the application
 */
import React from "react";
import "./App.css";
import FacilityList from "./components/facilities/FacilityList";
import DataEntryForm from "./components/dataentry/DataEntryForm";

function App() {
  return (
    <div className="App">
      {/* Header */}
      <header className="app-header">
        <h1>🏥 Health Metrics Tracker</h1>
        <p className="subtitle">Track and visualize health facility data</p>
      </header>

      {/* Main content */}
      <main className="app-main">
        {/* Show Data Entry Form */}
        <DataEntryForm />

        {/* Separator */}
        <div style={{ margin: "40px 0", borderTop: "2px solid #e0e0e0" }}></div>

        {/* Show Facility List below the form */}
        <FacilityList />
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>Health Metrics Tracker © 2026 | Built with React & Spring Boot</p>
      </footer>
    </div>
  );
}

export default App;
