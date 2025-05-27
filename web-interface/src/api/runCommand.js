import { apiClient } from "./client";

export async function runCommand(cmdText) {
  try {
    const res = await apiClient.post("/runCommand", cmdText, {
      headers: { "Content-Type": "text/plain" },
    });
    return res.data; // plain text from backend
  } catch (err) {
    return err.response ? `Error: ${err.response.data}` : err.message;
  }
}
