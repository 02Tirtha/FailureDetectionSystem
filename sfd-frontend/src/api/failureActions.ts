export const resolveFailure = async (stepName: string, workflowId: number) => {
  const userEmail = localStorage.getItem("userEmail") || "";
  await fetch("http://localhost:8080/api/events/resolve", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(userEmail ? { "X-User-Email": userEmail } : {})
    },
    body: JSON.stringify({ workflowId, stepName })
  });
};

export const triggerStep = async (
  stepName: string,
  workflowId: number,
  occurredAt?: string
) => {
  const userEmail = localStorage.getItem("userEmail") || "";
  await fetch("http://localhost:8080/api/events", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(userEmail ? { "X-User-Email": userEmail } : {})
    },
    body: JSON.stringify({
      stepName,
      occurredAt: occurredAt || new Date().toISOString(),
      workflow: { id: workflowId }
    })
  });
};
