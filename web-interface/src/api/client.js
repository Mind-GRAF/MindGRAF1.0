import axios from "axios";

const BASE_URL = "http://localhost:7070";

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "text/plain",
  },
});
