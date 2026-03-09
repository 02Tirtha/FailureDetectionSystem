export const resolveFailure = async (stepName: string, workflowId: number) => {
  await fetch("http://localhost:8080/api/events/resolve", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ workflowId, stepName })
  });
};

export const triggerStep = async (
  stepName: string,
  workflowId: number,
  occurredAt?: string
) => {
  await fetch("http://localhost:8080/api/events", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      stepName,
      occurredAt: occurredAt || new Date().toISOString(),
      workflow: { id: workflowId }
    })
  });
};
