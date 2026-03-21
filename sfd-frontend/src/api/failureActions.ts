const readResponseBody = async (res: Response) => {
  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return res.json();
  }
  const text = await res.text();
  return text ? text : null;
};

export const resolveFailure = async (stepName: string, workflowId: number) => {
  const userEmail = localStorage.getItem("userEmail") || "";
   const res = await fetch(`${import.meta.env.VITE_API_URL}/api/events/resolve`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(userEmail ? { "X-User-Email": userEmail } : {})
    },
    body: JSON.stringify({ workflowId, stepName })
  });
   if (!res.ok) {
    throw new Error("Failed to resolve failure");
  }

  return readResponseBody(res);
};

export const triggerStep = async (
  stepName: string,
  workflowId: number,
  occurredAt?: string
) => {
  const userEmail = localStorage.getItem("userEmail") || "";
  const res =await fetch(`${import.meta.env.VITE_API_URL}/api/events`, {
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

   if (!res.ok) {
    throw new Error("Failed to trigger step");
  }

  return readResponseBody(res);
};
