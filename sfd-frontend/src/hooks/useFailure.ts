import { useEffect, useState } from "react";
import { getFailuresByWorkflow } from "../api/failureApi";
import type { Failure } from "../types/failure";

export const useFailures = (workflowId: number) => {
  const [failures, setFailures] = useState<Failure[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getFailuresByWorkflow(workflowId)
      .then(setFailures)
      .finally(() => setLoading(false));
  }, [workflowId]);

  return { failures, loading };
};
