package edu.guc.mind_graf.api;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import edu.guc.mind_graf.parser.MindGRAF_Parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class ApiServer {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost(); // allow all origins
                });
            });
        }).start(7070);

        app.post("/runCommand", ctx -> {
            String command = ctx.body();             // CLI text sent from React

    // 1. Capture whatever Farah’s code prints
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        boolean ok = true;
        try {
            MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(command));
            parser.Command();                    // this writes to System.out
        } catch (Exception e) {
         ok = false;
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.flush();
            System.setOut(oldOut);               // ALWAYS restore stdout
        }

        // 2. Send the captured text back
        String output = buffer.toString(StandardCharsets.UTF_8);
        if (output.isBlank()) output = ok ? "Done." : "Unknown error.";

        ctx.result(output)                       // plain-text response
        .status(ok ? 200 : 500);
        });


        app.get("/", ctx -> ctx.result("Mind GRAF backend is running."));
    }
}
