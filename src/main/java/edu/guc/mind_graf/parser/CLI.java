package edu.guc.mind_graf.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import java.util.Scanner;

import edu.guc.mind_graf.context.ContextController;

public class CLI {
    static boolean uvbr;
    static boolean consistentAttitudes;
    static boolean attitudesSet = true;

    @SuppressWarnings("unchecked")

    public static void main(String[] args) {
        // boolean hasDuplicates = false;
        // String s ="ASDFg";
        // ArrayList<ArrayList<Integer>> finalList = new ArrayList<ArrayList<Integer>>();
        // for (int i = 0; i < finalList.size(); i++) {
        //     Collections.sort(finalList.get(i)); // Sort each inner list
        // }
        // for (int i = 0; i < finalList.size(); i++) {
        //     for (int j = i + 1; j < finalList.size(); j++) {
        //         if (finalList.get(i).equals(finalList.get(j))) {
        //             hasDuplicates = true;
        //             break;
        //         }
        //     }
        //     if (hasDuplicates) {
        //         break;
        //     }
        // }
        // if (hasDuplicates)
        //     System.out.println("ArrayList contains duplicate inner ArrayLists.");
        // else
        //     System.out.println("ArrayList does not contain duplicate inner ArrayLists.");
            ArrayList<String> al = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            HashMap<String, Integer> attitudeNames = ContextController.getAttitudes().getSet();
            // attitudeNames.put(0, "belief");
            // attitudeNames.put(1, "desire");
            // attitudeNames.put(2, "fear");
            // attitudeNames.put(3, "intention");
            // attitudeNames.put(4, "regret");
            if (attitudesSet) {
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
                    consistentAttitudes = true;
                    attitudesSet=false;
                } catch (ParseException e) {
                    System.out.println();
                    // System.out.println("Wrong Command. use set-uvbr");
                    System.out.println(e.getMessage());

                    // TODO Auto-generated catch block

                    continue;
                }
            } else {
                if (consistentAttitudes) {
                    System.out.println("Enter sets of consistent attitudes");
                    String input = scanner.nextLine();
                    if (input.trim().toLowerCase().equals("exit")) {
                        System.out.println("Session ended.");
                        break;
                    }
                    MindGRAF_Parser parser = new MindGRAF_Parser(
                            new StringReader(input.trim()));
                    try {
                        parser.consistentAttitudes();
                        consistentAttitudes = false;
                        uvbr = true;
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.out.println("Wrong Command.");
                        continue;
                        
                    }
                } else {
                    if (uvbr) {
                        System.out.print("set UVBR Variable to \"true\" or \"false\"\n" + ":");
                        String input = scanner.nextLine();
                        MindGRAF_Parser parser = new MindGRAF_Parser(
                                new StringReader(input.trim()));
                        try {
                            parser.UVBR();
                            uvbr = false;
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            // e.printStackTrace();
                            System.out.print("set UVBR Variable to \"true\" or \"false\"\n" + ":");
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
                                parser.Command();
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                System.out.println();
                                System.out.println("Invalid Command.");
                                System.out.println(e);

                                continue;
                            }
                            // System.out.print(":");

                        }
                    }
                }
            }
        }
        scanner.close();

    }
}
