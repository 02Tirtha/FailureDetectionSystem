import { useEffect, useState } from "react";
import { triggerStep } from "../api/failureActions";

interface Workflow {
  id: number;
  name: string;
}

const EventsTesterPage = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [workflowId, setWorkflowId] = useState<number | "">("");
  const [stepName, setStepName] = useState("");
  const [occurredAt, setOccurredAt] = useState(
    new Date().toISOString().slice(0, 16)
  );
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/workflows")
      .then((res) => res.json())
      .then(setWorkflows);
  }, []);

  const handleSubmit = async () => {
    if (!workflowId || !stepName) return;

    setLoading(true);
    await triggerStep(stepName, Number(workflowId), occurredAt);
    setLoading(false);

    alert("Event submitted successfully!");
    setStepName("");
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-semibold mb-2">Events Tester</h2>
        <p className="text-sm text-gray-500 mb-6">
          Manually trigger workflow events for testing & demo
        </p>

        {/* Workflow */}
        <label className="text-sm font-medium mb-1 block">
          Select Workflow
        </label>
        <select
          value={workflowId}
          onChange={(e) => setWorkflowId(Number(e.target.value))}
          className="w-full border px-3 py-2 rounded mb-4"
        >
          <option value="">-- Select Workflow --</option>
          {workflows.map((w) => (
            <option key={w.id} value={w.id}>
              {w.name}
            </option>
          ))}
        </select>

        {/* Step */}
        <label className="text-sm font-medium mb-1 block">
          Step Name
        </label>
        <input
          value={stepName}
          onChange={(e) => setStepName(e.target.value)}
          placeholder="e.g. PAYMENT_DONE"
          className="w-full border px-3 py-2 rounded mb-4"
        />

        {/* Timestamp */}
        <label className="text-sm font-medium mb-1 block">
          Event Time
        </label>
        <input
          type="datetime-local"
          value={occurredAt}
          onChange={(e) => setOccurredAt(e.target.value)}
          className="w-full border px-3 py-2 rounded mb-6"
        />

        {/* Action */}
        <button
          onClick={handleSubmit}
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? "Submitting..." : "Submit Event"}
        </button>
      </div>
    </div>
  );
};

export default EventsTesterPage;
