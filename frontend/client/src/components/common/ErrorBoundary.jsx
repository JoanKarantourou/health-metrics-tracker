import React from "react";

/**
 * Error Boundary Component
 * Catches JavaScript errors in child components and displays a fallback UI.
 * Prevents the entire app from crashing due to a single component error.
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught an error:", error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={styles.container}>
          <div style={styles.card}>
            <h2 style={styles.title}>Something went wrong</h2>
            <p style={styles.message}>
              An unexpected error occurred. Please try refreshing the page.
            </p>
            {this.state.error && (
              <details style={styles.details}>
                <summary>Error details</summary>
                <pre style={styles.pre}>{this.state.error.toString()}</pre>
              </details>
            )}
            <div style={styles.actions}>
              <button onClick={this.handleReset} style={styles.button}>
                Try Again
              </button>
              <button onClick={() => window.location.reload()} style={styles.buttonSecondary}>
                Refresh Page
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "60vh",
    padding: "2rem",
  },
  card: {
    background: "#fff",
    borderRadius: "8px",
    boxShadow: "0 2px 12px rgba(0,0,0,0.1)",
    padding: "2rem",
    maxWidth: "500px",
    width: "100%",
    textAlign: "center",
    border: "1px solid #fee2e2",
  },
  title: {
    color: "#dc2626",
    marginBottom: "0.5rem",
  },
  message: {
    color: "#6b7280",
    marginBottom: "1.5rem",
  },
  details: {
    textAlign: "left",
    marginBottom: "1.5rem",
    padding: "0.75rem",
    background: "#fef2f2",
    borderRadius: "4px",
    fontSize: "0.85rem",
  },
  pre: {
    whiteSpace: "pre-wrap",
    wordBreak: "break-word",
    margin: "0.5rem 0 0",
    color: "#991b1b",
  },
  actions: {
    display: "flex",
    gap: "0.75rem",
    justifyContent: "center",
  },
  button: {
    padding: "0.5rem 1.5rem",
    background: "#667eea",
    color: "#fff",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
    fontSize: "0.9rem",
  },
  buttonSecondary: {
    padding: "0.5rem 1.5rem",
    background: "#e5e7eb",
    color: "#374151",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
    fontSize: "0.9rem",
  },
};

export default ErrorBoundary;
