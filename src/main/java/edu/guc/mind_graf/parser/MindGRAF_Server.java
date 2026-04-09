package edu.guc.mind_graf.parser;

import io.javalin.Javalin;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.set.Set;

public class MindGRAF_Server {
    // Flag to track whether the MindGRAF engine has been initialized
    private static boolean engineInitialized = false;

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

            try {
                if (command.startsWith("boot-wizard")) {
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
                    System.out
                            .println("[BRIDGE] ✓ Engine initialized successfully. Attitudes: " + attitudeMap.keySet());
                    ctx.result("Attitudes Initialized Successfully");

                } else {
                    // Guard: make sure the engine wizard has been run first
                    if (!engineInitialized) {
                        System.out.println(
                                "[BRIDGE] ERROR: Engine not initialized. Send 'boot-wizard <attitudes>' first.");
                        ctx.status(400).result("Error: Engine not initialized. Call boot-wizard first.");
                        return;
                    }

                    System.out.println("[BRIDGE] Routing to Parser (Command)...");
                    MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(command));
                    parser.Command();
                    ctx.result("Command Executed Successfully");
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

                // If MindGRAF is just telling us the data is already there, treat it as a
                // success
                if (errorMessage.contains("duplicate") || errorMessage.contains("already exist")) {
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
}