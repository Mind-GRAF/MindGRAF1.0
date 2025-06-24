import { apiClient } from "./client";

// Enhanced command execution with better error handling and logging
export async function runCommand(cmdText) {
  try {
    console.log(`[API] Executing command: ${cmdText}`);

    const res = await apiClient.post("/runCommand", cmdText, {
      headers: {
        "Content-Type": "text/plain",
        Accept: "text/plain",
      },
      timeout: 10000, // 10 second timeout
    });

    console.log(`[API] Response: ${res.data}`);
    return res.data; // plain text from backend
  } catch (err) {
    console.error(`[API] Error executing command "${cmdText}":`, err);

    if (err.response) {
      // Server responded with error status
      const errorMsg = `Error: ${err.response.data}`;
      console.error(`[API] Server error: ${errorMsg}`);
      return errorMsg;
    } else if (err.request) {
      // Request was made but no response received
      const errorMsg = "Error: No response from backend server";
      console.error(`[API] Network error: ${errorMsg}`);
      return errorMsg;
    } else {
      // Something else happened
      const errorMsg = `Error: ${err.message}`;
      console.error(`[API] Request error: ${errorMsg}`);
      return errorMsg;
    }
  }
}

// Specialized function for system queries that need parsing
export async function runSystemQuery(command) {
  try {
    const result = await runCommand(command);

    // Handle different types of system responses
    if (result.includes("Error:")) {
      throw new Error(result);
    }

    return result;
  } catch (error) {
    console.error(`[API] System query failed for "${command}":`, error);
    throw error;
  }
}

// Function to sync attitudes with backend during setup
export async function syncAttitudesWithBackend(attitudes) {
  try {
    console.log(`[API] Syncing attitudes with backend:`, attitudes);

    // First set the attitudes in the backend
    const attitudeList = attitudes.join(",");
    const setResult = await runCommand(`set-attitudes ${attitudeList}`);

    if (setResult.includes("Error:")) {
      throw new Error(`Failed to set attitudes: ${setResult}`);
    }

    // Verify they were set correctly
    const verifyResult = await runCommand("get-attitudes");
    console.log(`[API] Backend attitudes verification:`, verifyResult);

    return verifyResult;
  } catch (error) {
    console.error(`[API] Failed to sync attitudes:`, error);
    throw error;
  }
}

// Function to sync contexts with backend
export async function syncContextsWithBackend(contexts, currentContext) {
  try {
    console.log(`[API] Syncing contexts with backend:`, contexts);

    // Define each context in the backend
    for (const context of contexts) {
      if (context !== "default") {
        const result = await runCommand(`define-context ${context}`);
        if (result.includes("Error:")) {
          console.warn(
            `[API] Context definition warning for "${context}": ${result}`
          );
        }
      }
    }

    // Set the current context
    const setCurrentResult = await runCommand(
      `set-curr-context ${currentContext}`
    );
    if (setCurrentResult.includes("Error:")) {
      throw new Error(`Failed to set current context: ${setCurrentResult}`);
    }

    console.log(
      `[API] Successfully synced contexts and set current to: ${currentContext}`
    );
    return setCurrentResult;
  } catch (error) {
    console.error(`[API] Failed to sync contexts:`, error);
    throw error;
  }
}

// Enhanced setup synchronization function
export async function syncSetupWithBackend(setupData) {
  try {
    console.log(`[API] Starting complete setup sync with backend`);

    const {
      attitudes,
      contexts,
      currentContext,
      currentAttitude,
      consistentAttitudes,
      conjunctionAttitudes,
      consequenceAttitudes,
      telescopableAttitudes,
      uvbrEnabled,
    } = setupData;

    // 1. Sync attitudes
    await syncAttitudesWithBackend(attitudes);

    // 2. Set current attitude
    const attitudeResult = await runCommand(`set-attitude ${currentAttitude}`);
    if (attitudeResult.includes("Error:")) {
      throw new Error(`Failed to set current attitude: ${attitudeResult}`);
    }

    // 3. Sync contexts
    await syncContextsWithBackend(contexts, currentContext);

    // 4. Set additional properties if needed
    if (uvbrEnabled !== undefined) {
      const uvbrResult = await runCommand(
        `set-uvbr ${uvbrEnabled ? "on" : "off"}`
      );
      console.log(`[API] UVBR setting result:`, uvbrResult);
    }

    console.log(`[API] Complete setup sync successful`);
    return true;
  } catch (error) {
    console.error(`[API] Setup sync failed:`, error);
    throw error;
  }
}
