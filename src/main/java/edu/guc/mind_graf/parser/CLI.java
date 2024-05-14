package edu.guc.mind_graf.parser;

import java.io.StringReader;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.HashMap;

import java.util.Scanner;

//import edu.guc.mind_graf.context.ContextController;

public class CLI {
    private static boolean uvbr;
    private static boolean consistentAttitudes;
    private static boolean attitudesSet = true;
    private static boolean conjunctionAttitudes;
    private static boolean consequentAttitudes;
    private static boolean telescopable;
    //static boolean definingSemanticType = false;
    private static Scanner sc = new Scanner(System.in);

    @SuppressWarnings("unchecked")
    public static String readInput() {
        String s = sc.nextLine();
        return s;
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {

        while (true) {

            if (attitudesSet) {
                System.out.println("Enter attitudes or 'N' to proceed:");
                // String input = scanner.nextLine();
                String s = readInput();
                if (s.trim().toLowerCase().equals("exit")) {
                    System.out.println("Session ended.");
                    break;
                }
                MindGRAF_Parser parser = new MindGRAF_Parser(
                        new StringReader(s));

                try {
                    parser.setAttitudes();
                    consistentAttitudes = true;
                    attitudesSet = false;
                } catch (ParseException e) {
                    System.out.println();
                    System.out.println(e.getMessage());

                    // TODO Auto-generated catch block

                    continue;
                }
            } else {
                if (consistentAttitudes) {
                    System.out.println("Enter sets of consistent attitudes");
                    // String input = scanner.nextLine();
                    String s = readInput();
                    if (s.trim().toLowerCase().equals("exit")) {
                        System.out.println("Session ended.");
                        break;
                    }
                    MindGRAF_Parser parser = new MindGRAF_Parser(
                            new StringReader(s.trim()));
                    try {
                        parser.consistentAttitudes();
                        consistentAttitudes = false;
                        conjunctionAttitudes = true;
                        // uvbr = true;
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        System.out.println(e.getMessage());
                        continue;

                    }
                } else {
                    if (conjunctionAttitudes) {
                        System.out.println("Enter sets of attitudes closed under conjunction");
                        String input = readInput();
                        if (input.trim().toLowerCase().equals("exit")) {
                            System.out.println("Session ended.");
                            break;
                        }
                        MindGRAF_Parser parser = new MindGRAF_Parser(
                                new StringReader(input.trim()));
                        try {
                            parser.underConjunctionAttitudes();
                            conjunctionAttitudes = false;
                            consequentAttitudes = true;
                            // uvbr = true;
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                           // e.printStackTrace();
                            System.out.println(e.getMessage());
                            continue;

                        }
                    } else {
                        if (consequentAttitudes) {
                            System.out.println("Enter sets of attitudes closed under consequence");
                            String input = readInput();
                            if (input.trim().toLowerCase().equals("exit")) {
                                System.out.println("Session ended.");
                                break;
                            }
                            MindGRAF_Parser parser = new MindGRAF_Parser(
                                    new StringReader(input.trim()));
                            try {
                                parser.underConsequenceAttitudes();
                                consequentAttitudes = false;
                                telescopable = true;
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                //e.printStackTrace();
                                System.out.println(e.getMessage());
                                continue;

                            }
                        } else

                        {
                            if (telescopable) {
                                System.out.println("Enter sets of Telescopable Attitudes");
                                String input = readInput();
                                if (input.trim().toLowerCase().equals("exit")) {
                                    System.out.println("Session ended.");
                                    break;
                                }
                                MindGRAF_Parser parser = new MindGRAF_Parser(
                                        new StringReader(input.trim()));
                                try {
                                    parser.TelescopableAttitudes();
                                    telescopable = false;
                                    //uvbr = true;
                                } catch (ParseException e) {
                                    // TODO Auto-generated catch block
                                    //e.printStackTrace();
                                    System.out.println(e.getMessage());
                                    continue;

                                }
                            } else {
                                if (uvbr) {
                                    System.out.print("set UVBR Variable to \"true\" or \"false\"\n" + ":");
                                    String input = readInput();
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

                                    String input = readInput();

                                    if (input.trim().toLowerCase().equals("exit")) {
                                        System.out.println("Session ended.");
                                        break;
                                    } else {
                                        MindGRAF_Parser parser = new MindGRAF_Parser(
                                                new StringReader(input.trim().toLowerCase()));
                                        try {
                                            parser.Command();
                                        } catch (ParseException e) {
                                            // TODO Auto-generated catch block
                                            System.out.println();
                                            // System.out.println("Invalid Command.");
                                            System.out.println(e.getMessage());

                                            continue;
                                        }
                                        // System.out.print(":");

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
