import { useEffect, useState } from "react";
import api from "../api/axios";

export type Failure = {
  id: number;
  stepName: string;
  failureType: string;
  resolved: boolean;
  delayed: boolean;
  detectedAt:string;
};

export const useFailures = (workflowId: number | null) => {
  const [failures, setFailures] = useState<Failure[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!workflowId) return;

    setLoading(true);
    api
      .get<Failure[]>(`/dashboard/failures/workflow/${workflowId}`)
      .then(res => setFailures(res.data))
      .finally(() => setLoading(false));
  }, [workflowId]);

  return { failures, loading };
};
