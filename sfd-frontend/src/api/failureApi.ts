import api from "./axios";
import type { Failure } from "../types/failure";

export const getFailuresByWorkflow = async (
  workflowId: number
): Promise<Failure[]> => {
  const response = await api.get(
    `/failures?workflowId=${workflowId}`
  );
  return response.data;
};

export const resolveFailure = async (
  workflowId: number,
  stepName: string
) => {
  return api.post("/failures/resolve", {
    workflowId,
    stepName,
  });
};
