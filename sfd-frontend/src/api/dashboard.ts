import api from "./axios";
import type {DashboardStats} from "../types/dashboard";

export const getDashboardStats = async (): Promise<DashboardStats> => {
    const res = await api.get("/dashboard/stats");
    return res.data;

};