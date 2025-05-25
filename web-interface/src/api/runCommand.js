import { apiClient } from "./client";

export async function runCommand(commandText) {
  try {
    const response = await apiClient.post("/runCommand", commandText);
    return response.data; // The response text from backend
  } catch (error) {
    if (error.response) {
      return `Error: ${error.response.data}`;
    }
    return `Unexpected Error: ${error.message}`;
  }
}
