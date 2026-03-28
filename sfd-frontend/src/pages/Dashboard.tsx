import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { DashboardStats } from "../types/dashboard";

const API_URL = import.meta.env.VITE_API_URL;

const Dashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    fetch(`${API_URL}/api/dashboard/stats`)
      .then((res) => {
        if (!res.ok) {
          throw new Error(`Failed to load dashboard stats (${res.status})`);
        }
        return res.json();
      })
      .then((data) => {
        console.log(data);
        setStats(data);
      })
      .catch((err) => console.error(err));
  }, []);

  if (!stats) return <p className="page">Loading dashboard...</p>;

  return (
    <div className="page">
      <div className="hero">
        <div>
          <h1 className="page-title">Ops Overview</h1>
          <p className="page-subtitle">
            Monitor silent failures and intervene before workflows drift.
          </p>
        </div>
        <div className="hero-actions">
          <Link to="/workflows" className="btn btn-primary">
            Open Overview
          </Link>
          <Link to="/events" className="btn btn-ghost">
            Open Events Lab
          </Link>
        </div>
      </div>

      <div className="cards-grid">
        <Card title="Workflows" value={stats.totalWorkflows} />
        <Card title="Total Failures" value={stats.totalFailures} />
        <Card title="Open Failures" value={stats.unresolvedFailures} />
        <Card title="High Severity" value={stats.highSeverityFailures} />
        <Card title="Missing Steps" value={stats.missingCount} />
        <Card title="Delayed Steps" value={stats.delayedCount} />
      </div>
    </div>
  );
};

const Card = ({ title, value }: { title: string; value: number }) => (
  <div className="stat-card">
    <div className="stat-label">{title}</div>
    <div className="stat-value">{value}</div>
  </div>
);

export default Dashboard;
