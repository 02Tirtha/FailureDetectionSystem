import { useEffect, useState } from "react";
import { getWorkflows } from "../api/workflowApi";
import type { Workflow } from "../types/workflow";

export const useWorkflows = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getWorkflows()
      .then(setWorkflows)
      .finally(() => setLoading(false));
  }, []);

  return { workflows, loading };
};
