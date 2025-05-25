package edu.guc.mind_graf.api;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import edu.guc.mind_graf.parser.MindGRAF_Parser;

import java.io.StringReader;

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
            String command = ctx.body();
            try {
                MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(command));
                parser.Command(); // ensure this is the correct method
                ctx.result("Command executed successfully.");
            } catch (Exception e) {
                ctx.status(500).result("Error: " + e.getMessage());
            }
        });

        app.get("/", ctx -> ctx.result("Mind GRAF backend is running."));
    }
}
