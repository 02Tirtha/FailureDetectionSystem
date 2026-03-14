import { useWorkflowsDashboard } from "../hooks/userWorkflowsDashboard";
import { Link } from "react-router-dom";

const Workflows = () => {
  const { data: workflows, loading } = useWorkflowsDashboard();

  if (loading) return <p className="page">Loading...</p>;

  return (
    <div className="page">
      <h2 className="page-title">Workflows Dashboard</h2>
      <p className="page-subtitle">
        Track health, failures, and last incident time by workflow.
      </p>

      <table className="table">
        <thead>
          <tr>
            <th>Workflow</th>
            <th>Total Failures</th>
            <th>Unresolved</th>
            <th>Last Failure</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {workflows.map((w) => {
            const healthy = w.unresolvedFailures === 0;

            return (
              <tr key={w.workflowId}>
                <td className="font-medium">{w.workflowName}</td>

                <td className="text-center">{w.totalFailures}</td>

                <td className="text-center">{w.unresolvedFailures}</td>

                <td>
                  {w.lastFailureTime
                    ? new Date(w.lastFailureTime).toLocaleString()
                    : "—"}
                </td>

                <td className="text-center">
                  <span
                    className={`badge ${
                      healthy ? "badge-success" : "badge-danger"
                    }`}
                  >
                    {healthy ? "Healthy" : "Needs Attention"}
                  </span>
                </td>

                <td className="text-center">
                  <Link to={`/workflows/${w.workflowId}`} className="btn btn-ghost">
                    View details
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default Workflows;
