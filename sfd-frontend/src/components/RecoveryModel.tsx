import React, { useState } from "react";
import { triggerStep, resolveFailure } from "../api/failureActions";
import { AiOutlineCheckCircle, AiOutlineCloseCircle } from "react-icons/ai";

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
      // Trigger the event with selected timestamp
      await triggerStep(failure.stepName, workflowId, occurredAt);

      // Resolve the failure
      await resolveFailure(failure.stepName, workflowId);

      onSuccess();
      onClose();
    } catch (err) {
      console.error(err);
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-96 p-6 transform transition-transform scale-100 animate-fadeIn">
        {failure.resolved ? (
          <div className="flex flex-col items-center justify-center py-8">
            <AiOutlineCheckCircle className="text-green-500 text-6xl mb-4" />
            <h2 className="text-xl font-semibold mb-2 text-center">
              No Action Required
            </h2>
            <p className="text-gray-500 text-center">
              This step has already been resolved.
            </p>
            <button
              onClick={onClose}
              className="mt-6 px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
            >
              Close
            </button>
          </div>
        ) : (
          <>
            <div className="flex items-center mb-4">
              <AiOutlineCloseCircle className="text-red-500 text-2xl mr-2" />
              <h2 className="text-lg font-semibold">Run Recovery Action</h2>
            </div>

            <label className="text-sm block mb-1 font-medium">Step Name</label>
            <input
              value={failure.stepName}
              disabled
              className="w-full border px-3 py-2 rounded mb-4 bg-gray-100"
            />

            <label className="text-sm block mb-1 font-medium">Occurred At</label>
            <input
              type="datetime-local"
              value={occurredAt}
              onChange={(e) => setOccurredAt(e.target.value)}
              className="w-full border px-3 py-2 rounded mb-4"
            />

            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={onClose}
                className="px-4 py-2 border rounded hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? "Submitting..." : "Submit"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default RecoveryModal;
