import { useParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useFailures } from "../hooks/useFailure";
import RecoveryModal from "../components/RecoveryModel";

const WorkflowDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const workflowId = id ? Number(id) : null;

  const { failures, loading } = useFailures(workflowId);
  const [selectedFailure, setSelectedFailure] = useState<any>(null);

  if (loading || workflowId === null)
    return <p className="p-4">Loading...</p>;

  return (
    <div className="p-6">
      <h2 className="text-xl font-semibold mb-4">Workflow Failures</h2>

      <table className="w-full border border-gray-200 rounded-lg">
        <thead className="bg-gray-100">
          <tr>
            <th>Step</th>
            <th>Failure Type</th>
            <th>Status</th>
            <th>Detected At</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {failures.map(f => (
            <tr key={f.id} className="border-t">
              <td>{f.stepName}</td>
              <td>{f.failureType}</td>
              <td>{f.resolved ? "🟢 Healthy" : "🔴 Issues"}</td>
              <td>
                {f.detectedAt
                  ? new Date(f.detectedAt).toLocaleString()
                  : "-"}
              </td>
               <td>
                {f.resolved ? (
                  <span className="text-gray-500 text-xl font-semibold mb-2 text-center">
                        ✅ No Action Required
                      </span>
                ) : (
                  <button
                    onClick={() =>
                      navigate(`/workflows/${workflowId}/recovery/${f.stepName}`)
                    }
                    className="px-3 py-1 text-xs bg-blue-600 text-white rounded"
                  >
                    🔁 Run Recovery Action
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedFailure && (
        <RecoveryModal
          failure={selectedFailure}
          workflowId={workflowId}
          onClose={() => setSelectedFailure(null)}
          onSuccess={() => window.location.reload()}
        />
      )}
    </div>
  );
};

export default WorkflowDetails;
