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

  const formatDetectedAtTime = (value?: string | null) => {
    if (!value) return "-";
    const isoBase = value.includes("T") ? value : value.replace(" ", "T");
    // Normalize fractional seconds to 3 digits (JS Date supports milliseconds).
    const normalized = isoBase.replace(/\.(\d{3})\d+$/, ".$1");
    const date = new Date(normalized + "Z");
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleTimeString("en-IN", {
      timeZone: "Asia/Kolkata",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    });
  };

  if (loading || workflowId === null)
    return <p className="page">Loading...</p>;

  return (
    <div className="page">
      <h2 className="page-title">Workflow Failures</h2>
      <p className="page-subtitle">
        Active failures only. Resolve to remove them from the list.
      </p>
      {failures?.[0] && console.log("sample failure", failures[0])}

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
                {formatDetectedAtTime(f.detectedAt)}
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
