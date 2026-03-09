import { useWorkflowsDashboard } from "../hooks/userWorkflowsDashboard";
import { Link } from "react-router-dom";

const Workflows = () => {
  const { data: workflows, loading } = useWorkflowsDashboard();

  if (loading) return <p>Loading...</p>;

  return (
    <div className="p-6">
      <h2 className="text-2xl font-semibold mb-4">Workflows Dashboard</h2>

      <table className="w-full border border-gray-200 rounded-lg">
        <thead className="bg-gray-100">
          <tr>
            <th className="px-4 py-3 text-left">Workflow</th>
            <th className="px-4 py-3 text-center">Total Failures</th>
            <th className="px-4 py-3 text-center">Unresolved</th>
            <th className="px-4 py-3 text-left">Last Failure</th>
            <th className="px-4 py-3 text-center">Status</th>
            <th className="px-4 py-3 text-center">Action</th>
          </tr>
        </thead>

        <tbody>
          {workflows.map(w => {
            const healthy = w.unresolvedFailures === 0;

            return (
              <tr key={w.workflowId} className="border-t hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">
                  {w.workflowName}
                </td>

                <td className="px-4 py-3 text-center">
                  {w.totalFailures}
                </td>

                <td className="px-4 py-3 text-center">
                  {w.unresolvedFailures}
                </td>

                <td className="px-4 py-3">
                  {w.lastFailureTime
                    ? new Date(w.lastFailureTime).toLocaleString()
                    : "—"}
                </td>

                <td className="px-4 py-3 text-center">
                  <span
                    className={`px-2 py-1 rounded text-xs font-semibold ${
                      healthy
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {healthy ? "🟢 Healthy" : "🔴 Issues"}
                  </span>
                </td>

                <td className="px-4 py-3 text-center">
                  <Link
                    to={`/workflows/${w.workflowId}`}
                    className="text-blue-600 hover:underline"
                  >
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
