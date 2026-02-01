import api from "./axios";
import type { Workflow } from "../types/workflow";

export const getWorkflows = async (): Promise<Workflow[]> => {
  const response = await api.get("/workflows");
  return response.data;
};

export const getWorkflowById = async (
  id: number
): Promise<Workflow> => {
  const response = await api.get(`/workflows/${id}`);
  return response.data;
};
