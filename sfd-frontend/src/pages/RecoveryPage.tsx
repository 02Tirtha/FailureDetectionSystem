import { useParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { triggerStep, resolveFailure } from "../api/failureActions";

const RecoveryPage = () => {
  const { workflowId, stepName } = useParams<{
    workflowId: string;
    stepName: string;
  }>();

  const navigate = useNavigate();

  const toLocalInputValue = (date: Date) => {
    const pad = (value: number) => String(value).padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
      `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  };

  const [occurredAt, setOccurredAt] = useState(
    toLocalInputValue(new Date())
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (!workflowId || !stepName) return;

    try {
      setLoading(true);
      setError(null);

      await triggerStep(stepName, Number(workflowId), occurredAt);
      await resolveFailure(stepName, Number(workflowId));

      navigate(`/workflows/${workflowId}`);
    } catch (err) {
      console.error(err);
      setError("Failed to run recovery action. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="panel" style={{ maxWidth: 560, margin: "0 auto" }}>
        <div className="hero">
          <div>
            <h2 className="page-title">Run Recovery Action</h2>
            <p className="page-subtitle">
              Re-trigger the failed step and resolve the issue.
            </p>
          </div>
        </div>

        <div className="form-grid">
          <div>
            <label>Step Name</label>
            <input value={stepName} disabled className="input" />
          </div>

          <div>
            <label>Occurred At</label>
            <input
              type="datetime-local"
              value={occurredAt}
              onChange={(e) => setOccurredAt(e.target.value)}
              className="input"
            />
            <p className="helper">
              This timestamp will be stored as the recovery event time.
            </p>
          </div>

          {error && <div className="badge badge-danger">{error}</div>}

          <div className="hero-actions" style={{ justifyContent: "flex-end" }}>
            <button
              onClick={() => navigate(-1)}
              disabled={loading}
              className="btn btn-ghost"
            >
              Cancel
            </button>

            <button
              onClick={handleSubmit}
              disabled={loading}
              className="btn btn-primary"
            >
              {loading ? "Running..." : "Run Recovery"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecoveryPage;
