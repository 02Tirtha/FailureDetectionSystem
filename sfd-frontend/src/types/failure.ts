export interface Failure {
  id: number;
  stepName: string;
  message: string;
  failureType: "MISSING_STEP" | "DELAYED_STEP";
  severity: "LOW" | "MEDIUM" | "HIGH";
  detectedAt: string;
  resolved: boolean;
  resolvedAt?: string;
}
