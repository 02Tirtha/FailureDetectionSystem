import { useEffect, useState } from "react";
import api from "../api/axios";

export type WorkflowDashboardItem = {
  workflowId: number;
  workflowName: string;
  totalFailures: number;
  unresolvedFailures: number;
  lastFailureTime?: string | null;
};

export const useWorkflowsDashboard = () => {
  const [data, setData] = useState<WorkflowDashboardItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
      api 
      .get<WorkflowDashboardItem[]>("/workflows")
      .then(res => setData(res.data))
      .finally(()=> setLoading(false));   
});

  return { data, loading };
};