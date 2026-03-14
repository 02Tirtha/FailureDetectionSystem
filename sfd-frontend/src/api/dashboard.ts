import api from "./axios";
import type {DashboardStats} from "../types/dashboard";

//A Promise is an object that represents a value that will be available in the future.
export const getDashboardStats = async (): Promise<DashboardStats> => {
    const res = await api.get("/dashboard/stats");
    return res.data;

};