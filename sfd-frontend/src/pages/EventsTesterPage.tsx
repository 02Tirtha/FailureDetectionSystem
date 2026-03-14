import { useEffect, useState } from "react";
import { triggerStep } from "../api/failureActions";

interface Workflow {
  id: number;
  name: string;
}

interface WorkflowStep {
  id: number;
  stepName: string;
}

const EventsTesterPage = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [workflowId, setWorkflowId] = useState<number | "">("");
  const [steps, setSteps] = useState<WorkflowStep[]>([]);
  const [stepName, setStepName] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/workflows/all")
      .then((res) => res.json())
      .then((data: Workflow[]) => {
        const filtered = data.filter(
          (w) => w.name === "Order Flow" || w.name === "User Registration"
        );
        setWorkflows(filtered.length > 0 ? filtered : data);
      });
  }, []);

  useEffect(() => {
    if (!workflowId) {
      setSteps([]);
      setStepName("");
      return;
    }

    fetch(`http://localhost:8080/api/workflow-steps?workflowId=${workflowId}`)
      .then((res) => res.json())
      .then((data: WorkflowStep[]) => {
        setSteps(data);
        setStepName(data[0]?.stepName ?? "");
      });
  }, [workflowId]);

  const handleSubmit = async () => {
    if (!workflowId || !stepName) return;

    setLoading(true);
    await triggerStep(stepName, Number(workflowId));
    setLoading(false);

    alert("Event submitted successfully!");
  };

  return (
    <div className="page">
      <div className="panel" style={{ maxWidth: 520, margin: "0 auto" }}>
        <h2 className="page-title">Events Lab</h2>
        <p className="page-subtitle">
          Manually trigger workflow events for testing and demos.
        </p>

        <div className="form-grid">
          <div>
            <label>Select Workflow</label>
            <select
              value={workflowId}
              onChange={(e) => setWorkflowId(Number(e.target.value))}
              className="select"
            >
              <option value="">-- Select Workflow --</option>
              {workflows.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label>Step Name</label>
            <select
              value={stepName}
              onChange={(e) => setStepName(e.target.value)}
              className="select"
              disabled={steps.length === 0}
            >
              {steps.length === 0 && (
                <option value="">-- Select Workflow First --</option>
              )}
              {steps.map((step) => (
                <option key={step.id} value={step.stepName}>
                  {step.stepName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label>Event Time</label>
            <div className="helper">Uses current time when you submit.</div>
          </div>

          <button
            onClick={handleSubmit}
            disabled={loading || !workflowId || !stepName}
            className="btn btn-primary"
          >
            {loading ? "Submitting..." : "Submit Event"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EventsTesterPage;
