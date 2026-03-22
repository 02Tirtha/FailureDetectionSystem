const readResponseBody = async (res: Response) => {
  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    try {
      return await res.json();
    } catch {
      return null;
    }
  }
  const text = await res.text();
  return text ? text : null;
};

const fetchWithTimeout = async (input: RequestInfo, init: RequestInit, timeoutMs = 45000) => {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

  try {
    return await fetch(input, { ...init, signal: controller.signal });
  } finally {
    window.clearTimeout(timeoutId);
  }
};

const toLocalIsoString = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
};

export const resolveFailure = async (stepName: string, workflowId: number) => {
  const userEmail = localStorage.getItem("userEmail") || "";
   const res = await fetchWithTimeout(`${import.meta.env.VITE_API_URL}/api/events/resolve`, {
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
  const res = await fetchWithTimeout(`${import.meta.env.VITE_API_URL}/api/events`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(userEmail ? { "X-User-Email": userEmail } : {})
    },
    body: JSON.stringify({
      stepName,
      occurredAt: occurredAt || toLocalIsoString(new Date()),
      workflow: { id: workflowId }
    })
  });

   if (!res.ok) {
    throw new Error("Failed to trigger step");
  }

  return readResponseBody(res);
};
