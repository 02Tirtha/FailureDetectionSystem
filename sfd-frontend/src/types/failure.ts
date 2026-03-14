export interface Failure {
  id: number;
  stepName: string;
  message: string;
  failureType: "MISSING_STEP" | "DELAYED_STEP" | "ML_ANOMALY";
  severity: "LOW" | "MEDIUM" | "HIGH";
  detectedAt: string;
}
