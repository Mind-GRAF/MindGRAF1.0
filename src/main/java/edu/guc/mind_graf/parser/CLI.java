package edu.guc.mind_graf.parser;

import java.io.StringReader;
import java.util.HashMap;

import java.util.Scanner;

import edu.guc.mind_graf.context.ContextController;

public class CLI {
    private static boolean firstCommand = true;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            HashMap<String, Integer> attitudeNames = ContextController.getAttitudes().getSet();
            // attitudeNames.put(0, "belief");
            // attitudeNames.put(1, "desire");
            // attitudeNames.put(2, "fear");
            // attitudeNames.put(3, "intention");
            // attitudeNames.put(4, "regret");
            if (attitudeNames.isEmpty()) {
                System.out.println("Enter attitudes or 'N' to proceed:");
                String input = scanner.nextLine();
                if (input.trim().toLowerCase().equals("exit")) {
                    System.out.println("Session ended.");
                    break;
                }
                MindGRAF_Parser parser = new MindGRAF_Parser(
                        new StringReader(input.trim()));

                try {
                    parser.Setup();
                } catch (ParseException e) {
                    System.out.println();
                    System.out.println("Invalid Command.");
                    System.out.println();
   

                    // TODO Auto-generated catch block

                    continue;
                }
            } else {

                System.out.print("Enter a command (or 'exit' to quit):");

                String input = scanner.nextLine();

                if (input.trim().toLowerCase().equals("exit")) {
                    System.out.println("Session ended.");
                    break;
                } else {
                    MindGRAF_Parser parser = new MindGRAF_Parser(new StringReader(input));
                    try {
                        parser.Start();
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        System.out.println();
                        System.out.println("Invalid Command.");
                        System.out.println();
             

                        continue;
                    }
                    System.out.print(":");

                }
            }
        }
        scanner.close();

    }
}
