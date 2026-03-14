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
    return <p className="page">Loading...</p>;

  return (
    <div className="page">
      <h2 className="page-title">Workflow Failures</h2>
      <p className="page-subtitle">
        Active failures only. Resolve to remove them from the list.
      </p>

      <table className="table">
        <thead>
          <tr>
            <th>Step</th>
            <th>Failure Type</th>
            <th>Message</th>
            <th>Status</th>
            <th>Detected At</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {failures.map((f) => (
            <tr key={f.id}>
              <td>{f.stepName}</td>
              <td>{f.failureType}</td>
              <td>{f.message}</td>
              <td>
                <span className="badge badge-danger">Open</span>
              </td>
              <td>
                {f.detectedAt ? new Date(f.detectedAt).toLocaleString() : "-"}
              </td>
              <td>
                <button
                  onClick={() =>
                    navigate(`/workflows/${workflowId}/recovery/${f.stepName}`)
                  }
                  className="btn btn-primary"
                >
                  Run Recovery
                </button>
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
