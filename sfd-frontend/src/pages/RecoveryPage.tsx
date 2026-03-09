import { useParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { triggerStep, resolveFailure } from "../api/failureActions";

const RecoveryPage = () => {
  const { workflowId, stepName } = useParams<{
    workflowId: string;
    stepName: string;
  }>();

  const navigate = useNavigate();

  const [occurredAt, setOccurredAt] = useState(
    new Date().toISOString().slice(0, 16)
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
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-lg p-6">
        {/* Header */}
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 rounded-full bg-blue-100">
          </div>
          <div>
            <h2 className="text-xl font-semibold">Run Recovery Action</h2>
            <p className="text-sm text-gray-500">
              Re-trigger the failed step and resolve the issue
            </p>
          </div>
        </div>

        {/* Step info */}
        <div className="mb-4">
          <label className="text-sm font-medium block mb-1">Step Name</label>
          <input
            value={stepName}
            disabled
            className="w-full border px-3 py-2 rounded-lg bg-gray-100 text-gray-700"
          />
        </div>

        {/* Timestamp */}
        <div className="mb-4">
          <label className="text-sm font-medium block mb-1">
            Occurred At
          </label>
          <input
            type="datetime-local"
            value={occurredAt}
            onChange={(e) => setOccurredAt(e.target.value)}
            className="w-full border px-3 py-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="text-xs text-gray-500 mt-1">
            This timestamp will be stored as the recovery event time.
          </p>
        </div>

        {/* Error */}
        {error && (
          <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-2">
            {error}
          </div>
        )}

        {/* Actions */}
        <div className="flex justify-end gap-3 mt-6">
          <button
            onClick={() => navigate(-1)}
            disabled={loading}
            className="px-4 py-2 border rounded-lg hover:bg-gray-100 disabled:opacity-50"
          >
            Cancel
          </button>

          <button
            onClick={handleSubmit}
            disabled={loading}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
          >
            {loading && (
              <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            )}
            {loading ? "Running..." : "Run Recovery"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default RecoveryPage;
