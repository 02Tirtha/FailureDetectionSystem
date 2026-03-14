import React, { useState } from "react";
import { triggerStep, resolveFailure } from "../api/failureActions";

interface Props {
  failure: any;
  workflowId: number;
  onClose: () => void;
  onSuccess: () => void;
}

const RecoveryModal: React.FC<Props> = ({
  failure,
  workflowId,
  onClose,
  onSuccess
}) => {
  const [occurredAt, setOccurredAt] = useState(
    new Date().toISOString().slice(0, 16)
  );
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await triggerStep(failure.stepName, workflowId, occurredAt);
      await resolveFailure(failure.stepName, workflowId);

      onSuccess();
      onClose();
    } catch (err) {
      console.error(err);
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="panel modal-card">
        <h2 className="page-title">Run Recovery Action</h2>
        <p className="page-subtitle">
          Re-trigger the failed step and resolve the issue.
        </p>

        <div className="form-grid">
          <div>
            <label>Step Name</label>
            <input value={failure.stepName} disabled className="input" />
          </div>

          <div>
            <label>Occurred At</label>
            <input
              type="datetime-local"
              value={occurredAt}
              onChange={(e) => setOccurredAt(e.target.value)}
              className="input"
            />
          </div>

          <div className="hero-actions" style={{ justifyContent: "flex-end" }}>
            <button onClick={onClose} className="btn btn-ghost">
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="btn btn-primary"
            >
              {loading ? "Submitting..." : "Submit"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecoveryModal;
