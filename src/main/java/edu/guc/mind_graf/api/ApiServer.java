package edu.guc.mind_graf.api;

import io.javalin.Javalin;
import edu.guc.mind_graf.parser.MindGRAF_Parser;
import java.io.StringReader;

public class ApiServer {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7070);

        app.post("/runCommand", ctx -> {
            String command = ctx.body();
            try {
                MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(command));
                parser.Command(); // Capital 'C' here to match your JJ grammar
                ctx.result("Command executed successfully.");
            } catch (Exception e) {
                ctx.status(500).result("Error: " + e.getMessage());
            }
        });

        app.get("/", ctx -> ctx.result("Mind GRAF backend is running."));
    }
}
