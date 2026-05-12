package edu.guc.mind_graf.parser;

import io.javalin.Javalin;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.Set;

public class MindGRAF_Server {
    // Flag to track whether the MindGRAF engine has been initialized
    private static boolean engineInitialized = false;

    // ---------------------------------------------------------------
    // Command type classification for Group 4 (Inference & Reasoning)
    // ---------------------------------------------------------------
    private enum CommandType {
        BOOT_WIZARD,
        ASK_IF_TRUE,
        FORWARD_INFER,
        BACK_INFER,
        PERFORM_ACT,
        CLEAR_INFER,
        GENERIC
    }

    /**
     * Classifies the incoming command string into a CommandType.
     * This determines what post-execution result capture logic to apply.
     */
    private static CommandType classifyCommand(String command) {
        if (command.startsWith("boot-wizard"))    return CommandType.BOOT_WIZARD;
        if (command.startsWith("ask-if-true"))    return CommandType.ASK_IF_TRUE;
        if (command.startsWith("forward-infer"))  return CommandType.FORWARD_INFER;
        if (command.startsWith("back-infer"))     return CommandType.BACK_INFER;
        if (command.startsWith("perform-act"))    return CommandType.PERFORM_ACT;
        if (command.startsWith("clear-infer"))    return CommandType.CLEAR_INFER;
        return CommandType.GENERIC;
    }

    public static void main(String[] args) {
        // Start the server on port 8080
        var app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost()); // Allows your Next.js UI to talk to this server
            });
        }).start(8080);

        System.out.println("--- MindGRAF Bridge Live on http://localhost:8080 ---");

        // The endpoint that will receive commands from Claude/Next.js
        app.post("/execute", ctx -> {
            String command = ctx.body().trim();
            System.out.println("\n[BRIDGE] Incoming Command: " + command);
            CommandType cmdType = classifyCommand(command);

            try {
                if (cmdType == CommandType.BOOT_WIZARD) {
                    handleBootWizard(command);
                    ctx.result("Attitudes Initialized Successfully");
                    return;
                }

                // Guard: make sure the engine wizard has been run first
                if (!engineInitialized) {
                    System.out.println(
                            "[BRIDGE] ERROR: Engine not initialized. Send 'boot-wizard <attitudes>' first.");
                    ctx.status(400).result("Error: Engine not initialized. Call boot-wizard first.");
                    return;
                }

                // ---------------------------------------------------------------
                // Route to the JavaCC parser — this handles ALL command parsing
                // and execution. The grammar already contains productions for:
                //   forward-infer → forwardInference() → fInference() → propNode.add()
                //   back-infer    → backwardInference() → bInference() → propNode.deduce()
                //   perform-act   → performAct()        → actNode.perform()
                //   clear-infer   → clearInfer()        → clears all channels
                // ---------------------------------------------------------------
                System.out.println("[BRIDGE] Routing to Parser (Command) [type=" + cmdType + "]...");
                MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(command));
                parser.Command();

                // ---------------------------------------------------------------
                // POST-EXECUTION: Capture and return meaningful results
                // based on the command type.
                // ---------------------------------------------------------------
                switch (cmdType) {
                    case ASK_IF_TRUE:
                        // If we reach here, Ask() printed "ASK_IF_TRUE_RESULT: true"
                        // and didn't throw — answer is true.
                        ctx.result("true");
                        break;

                    case FORWARD_INFER:
                        ctx.result(captureForwardInferenceResults());
                        break;

                    case BACK_INFER:
                        ctx.result(captureBackwardInferenceResults());
                        break;

                    case PERFORM_ACT:
                        ctx.result("PERFORM_ACT_RESULT: Act executed successfully.");
                        break;

                    case CLEAR_INFER:
                        ctx.result("CLEAR_INFER_RESULT: All inference channels cleared.");
                        break;

                    default:
                        ctx.result("Command Executed Successfully");
                        break;
                }

            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";

                // ask-if-true: Ask() throws a ParseException with this sentinel when
                // the node is NOT supported in the current context.
                if (errorMessage.startsWith("ASK_IF_TRUE_RESULT: false")) {
                    System.out.println("[BRIDGE] ask-if-true → false  (" + errorMessage + ")");
                    ctx.result("false");
                    return;
                }

                // If MindGRAF is just telling us the data is already there, treat it as a
                // success
                String lower = errorMessage.toLowerCase();
                if (lower.contains("duplicate") || lower.contains("already exist")) {
                    System.out.println("[BRIDGE] Notice: State already exists. Proceeding safely.");
                    ctx.result("State Already Exists (Success)");
                } else {
                    // Otherwise, it's a real crash
                    System.out.println("[BRIDGE] PARSER CRASH: " + e.getMessage());
                    e.printStackTrace();
                    ctx.status(400).result("Error: " + e.getMessage());
                }
            }
        });

        // Simple health check to see if the server is up
        app.get("/health", ctx -> ctx.result("Mind is active."));
    }

    // ===================================================================
    // Boot Wizard — programmatic engine initialization
    // ===================================================================
    private static void handleBootWizard(String command) {
        if (engineInitialized) {
            System.out.println("[BRIDGE] Engine already initialized. Skipping redundant boot to preserve state.");
            return;
        }

        System.out.println("[BRIDGE] Programmatic Engine Init — bypassing interactive CLI wizard...");
        String attArg = command.replace("boot-wizard", "").trim();

        // ---------------------------------------------------------------
        // STEP 1: Parse the attitude names from the command.
        // The frontend sends: "boot-wizard intention"
        // or for multiple: "boot-wizard intention, obligation"
        //
        // "belief" is always auto-added as attitude 0 by the grammar.
        // ---------------------------------------------------------------
        HashMap<String, Integer> attitudeMap = new HashMap<>();
        attitudeMap.put("belief", 0); // belief is always attitude 0

        int nextId = 1;
        if (!attArg.isEmpty()) {
            String[] attitudes = attArg.split(",");
            for (String att : attitudes) {
                String name = att.trim().toLowerCase();
                if (!name.isEmpty() && !attitudeMap.containsKey(name)) {
                    attitudeMap.put(name, nextId++);
                }
            }
        }

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.setSet(attitudeMap);

        System.out.println();
        System.out.println("Attitudes Defined:");
        System.out.println();
        for (java.util.Map.Entry<String, Integer> entry : attitudeMap.entrySet()) {
            System.out.println("--> " + entry.getKey() + " ID: " + entry.getValue());
        }
        System.out.println();

        // ---------------------------------------------------------------
        // STEP 2: Call NetworkController.setUp() DIRECTLY.
        //
        // This is THE critical call that the interactive CLI wizard
        // eventually reaches (inside MindGRAF_Parser.java line ~3659)
        // after prompting for UVBR, automatic handling, cache, and
        // merge function.
        //
        // It calls ContextController.setup() which populates the
        // static 'attitudes' field. Without this, Context objects
        // are created with 0-length hypothesis arrays, causing the
        // ArrayIndexOutOfBoundsException.
        //
        // Parameters:
        // attitudeNames — the attitude set (belief + user-defined)
        // consistentAttitudes — empty list (no consistency groups)
        // uvbrEnabled — true (enable UVBR)
        // automaticHandling — true (auto-handle contradictions)
        // cacheEnabled — false (no cache)
        // mergeFunctionNumber — 1 (default: Math.max)
        // ---------------------------------------------------------------
        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        NetworkController.setUp(
                attitudeNames,
                consistentAttitudes,
                /* uvbrEnabled */ true,
                /* automaticHandling */ true,
                /* cacheEnabled */ false,
                /* mergeFunctionNumber */ 1);

        engineInitialized = true;
        System.out.println("[BRIDGE] ✓ Engine initialized successfully. Attitudes: " + attitudeMap.keySet());
    }

    // ===================================================================
    // GROUP 4 — Result Capture Methods
    // ===================================================================

    /**
     * Captures the results of a forward inference run.
     *
     * After parser.Command() completes the forwardInference() production,
     * the parser calls fInference() → propNode.add() which:
     *   1. Initializes the Scheduler
     *   2. Sends reports to antecedent rules, matching nodes, and WhenDo rules
     *   3. Runs the Scheduler to completion
     *   4. Populates Scheduler.getForwardAssertedNodes() with newly inferred nodes
     *
     * This method reads that result table and formats a human-readable response.
     */
    private static String captureForwardInferenceResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("FORWARD_INFER_RESULT: ");

        try {
            Hashtable<Report, PropositionNode> forwardAsserted = Scheduler.getForwardAssertedNodes();

            if (forwardAsserted == null || forwardAsserted.isEmpty()) {
                sb.append("No new knowledge was inferred during forward inference.");
                System.out.println("[BRIDGE] forward-infer → No new inferences.");
            } else {
                sb.append("Inferred ").append(forwardAsserted.size()).append(" new node(s): ");
                ArrayList<String> nodeDescriptions = new ArrayList<>();
                for (PropositionNode node : forwardAsserted.values()) {
                    nodeDescriptions.add(node.getName() + " (id=" + node.getId() + ")");
                }
                sb.append(String.join(", ", nodeDescriptions));
                sb.append(". Details: ");
                for (PropositionNode node : forwardAsserted.values()) {
                    sb.append("[").append(node.toString()).append("] ");
                }
                System.out.println("[BRIDGE] forward-infer → " + forwardAsserted.size() + " new node(s) inferred.");
            }
        } catch (Exception e) {
            sb.append("Forward inference completed but result capture failed: ").append(e.getMessage());
            System.out.println("[BRIDGE] forward-infer result capture error: " + e.getMessage());
        }

        return sb.toString();
    }

    /**
     * Captures the results of a backward inference run.
     *
     * After parser.Command() completes the backwardInference() production,
     * the parser calls bInference() → propNode.deduce() which:
     *   1. Initializes the Scheduler
     *   2. Checks known instances (positive and negative)
     *   3. Sends requests to dominating rule nodes, matching nodes, and DoIf rules
     *   4. Runs the Scheduler to completion
     *   5. Populates Scheduler.getBackwardAssertedReplyNodes() with inferred replies
     *
     * This method reads that result table and formats a human-readable response.
     */
    private static String captureBackwardInferenceResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("BACK_INFER_RESULT: ");

        try {
            Hashtable<Report, PropositionNode> backwardReplies = Scheduler.getBackwardAssertedReplyNodes();

            if (backwardReplies == null || backwardReplies.isEmpty()) {
                sb.append("No derivations found during backward inference.");
                System.out.println("[BRIDGE] back-infer → No derivations found.");
            } else {
                sb.append("Derived ").append(backwardReplies.size()).append(" reply node(s): ");
                ArrayList<String> nodeDescriptions = new ArrayList<>();
                for (PropositionNode node : backwardReplies.values()) {
                    nodeDescriptions.add(node.getName() + " (id=" + node.getId() + ")");
                }
                sb.append(String.join(", ", nodeDescriptions));
                sb.append(". Details: ");
                for (PropositionNode node : backwardReplies.values()) {
                    sb.append("[").append(node.toString()).append("] ");
                }
                System.out.println("[BRIDGE] back-infer → " + backwardReplies.size() + " reply node(s) derived.");
            }
        } catch (Exception e) {
            sb.append("Backward inference completed but result capture failed: ").append(e.getMessage());
            System.out.println("[BRIDGE] back-infer result capture error: " + e.getMessage());
        }

        return sb.toString();
    }
}