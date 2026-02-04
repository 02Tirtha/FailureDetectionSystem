import { useEffect, useState } from "react";
import { getDashboardStats } from "../api/dashboard";
import type { DashboardStats } from "../types/dashboard";

const Dashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(()=> {
    getDashboardStats()
    .then(setStats)
    .catch(console.error);

  }, []);

  if(!stats) return <p> Loading dashboard...</p>

  return (
    <div style={{ padding: "24px" }}>
      <h1>Dashboard</h1>

      <div style={{ display: "flex", gap: "16px", marginTop: "20px" }}>
        <Card title="Workflows" value={stats.totalWorkflows} />
        <Card title="Total Failures" value={stats.totalFailures} />
        <Card title="Unresolved" value={stats.unresolvedFailures} />
        <Card title="High Severity" value={stats.highSeverityFailures} />
        <Card title="Missing" value={stats.missingCount}/>
        <Card title="Delayed" value={stats.delayedCount}/>
        <Card title="ML Anomaly" value={stats.mlAnomalycount}/>
        
      </div>
    </div>
  );

};

  const Card =({title, value}:{title:string; value:number}) => (
    <div  style={{
      border: "1px solid #ddd",
      padding: "16px",
      borderRadius: "8px",
      width: "200px",
    }}

  >
     <h3>{title}</h3>
    <p style={{ fontSize: "24px", fontWeight: "bold" }}>{value}</p>
  </div>
  );

  export default Dashboard;