package edu.guc.mind_graf.parser;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import edu.guc.mind_graf.network.Controller;

public class CLI {
    private static boolean firstCommand = true;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ParseException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Hashtable<Integer, String> attitudeNames = Controller.getAttitudeNames();
            attitudeNames.put(0, "belief");
            attitudeNames.put(1, "desire");
            attitudeNames.put(2, "fear");
            attitudeNames.put(3, "intention");
            attitudeNames.put(4, "regret");
            if (attitudeNames.isEmpty()) {
                System.out.println("Enter attitudes or 'N' to proceed:");
                String input = scanner.nextLine();
                MindGRAF_Parser parser = new MindGRAF_Parser(
                        new StringReader(input.trim().toLowerCase() == "n" ? "" : input));
                parser.Start();
            } else {
                if (firstCommand) {
                    System.out.println("Enter a command (or 'exit' to quit):");
                    firstCommand = false;
                }
                String input = scanner.nextLine();
                
                if (input.trim().toLowerCase().equals("exit")) {
                    System.out.println("Session ended.");
                    break;
                } else {
                    MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(input));
                    parser.Start();
                    System.out.print(":");

                }
            }
        }
        scanner.close();

    }
}
