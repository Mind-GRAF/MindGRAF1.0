package edu.guc.mind_graf.parser;

import java.io.StringReader;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.HashMap;
import java.util.Collection;
import java.util.Scanner;

import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;

//import edu.guc.mind_graf.context.ContextController;

public class CLI {
    private static boolean uvbr;
    private static boolean consistentAttitudes;
    private static boolean attitudesSet = true;
    private static boolean conjunctionAttitudes;
    private static boolean consequentAttitudes;
    private static boolean telescopable;
    public static boolean loop = true;
    // static boolean definingSemanticType = false;
    private static Scanner sc = new Scanner(System.in);

    @SuppressWarnings("unchecked")
    public static String readInput() {
        String s = sc.nextLine();
        return s;
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void trial() {
        Collection<Pair<PropositionNodeSet, PropositionNodeSet>[]> x = ContextController.getContext("hogwarts")
                .getHypotheses()
                .values();
        for (Pair<PropositionNodeSet, PropositionNodeSet>[] pairArray : x) {
            for (Pair<PropositionNodeSet, PropositionNodeSet> p : pairArray) {
                Collection<Node> f = p.getFirst().getNodes();
                Collection<Node> s = p.getSecond().getNodes();
                CLI.print("ummmmmm OKKAYY??????");
                for (Node n : f)
                    CLI.print(n.toString());
                for (Node n : s)
                    CLI.print(n.toString());

            }
        }
    }

    public static void main(String[] args) {

        while (loop) {

            if (attitudesSet) {
                CLI.print("");
                System.out.println("Enter Attitudes or 'N' to Proceed");
                CLI.print("");
                System.out.print(": ");
                // String input = scanner.nextLine();
                String s = readInput();
                if (s.trim().toLowerCase().equals("exit")) {
                    CLI.print("Exiting MindGRAF...");
                    break;
                }
                MindGRAF_Parser parser = new MindGRAF_Parser(
                        new StringReader(s));

                try {
                    parser.setAttitudes();
                    CLI.print("");
                    CLI.print("------------------------------------------------------------");
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
                    CLI.print("");
                    CLI.print("Enter Sets of Consistent Attitudes");
                    CLI.print("");
                    System.out.print(": ");
                    // String input = scanner.nextLine();
                    String s = readInput();
                    if (s.trim().toLowerCase().equals("exit")) {
                        CLI.print("Exiting MindGRAF...");
                        break;
                    }
                    MindGRAF_Parser parser = new MindGRAF_Parser(
                            new StringReader(s.trim()));
                    try {
                        parser.consistentAttitudes();
                        consistentAttitudes = false;
                        conjunctionAttitudes = true;
                        CLI.print("");
                        CLI.print("------------------------------------------------------------");
                        // uvbr = true;
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        // e.printStackTrace();
                        System.out.println(e.getMessage());
                        continue;

                    }
                } else {
                    if (conjunctionAttitudes) {
                        CLI.print("Enter Attitudes Closed Under Conjunction");
                        CLI.print("");
                        System.out.print(": ");
                        String input = readInput();
                        if (input.trim().toLowerCase().equals("exit")) {
                            CLI.print("Exiting MindGRAF...");
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
                            System.out.println("Enter Attitudes Closed Under Consequence");
                            CLI.print("");
                            System.out.print(": ");
                            String input = readInput();
                            if (input.trim().toLowerCase().equals("exit")) {
                                CLI.print("Exiting MindGRAF...");
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
                                // e.printStackTrace();
                                System.out.println(e.getMessage());
                                continue;

                            }
                        } else

                        {
                            if (telescopable) {
                                CLI.print("Enter Sets of Telescopable Attitudes");
                                CLI.print("");
                                System.out.print(": ");
                                String input = readInput();
                                if (input.trim().toLowerCase().equals("exit")) {
                                    CLI.print("Exiting MindGRAF...");
                                    break;
                                }
                                MindGRAF_Parser parser = new MindGRAF_Parser(
                                        new StringReader(input.trim()));
                                try {
                                    parser.TelescopableAttitudes();
                                    telescopable = false;
                                    uvbr = true;
                                } catch (ParseException e) {
                                    // TODO Auto-generated catch block
                                    // e.printStackTrace();
                                    System.out.println(e.getMessage());
                                    continue;

                                }
                            } else {
                                if (uvbr) {
                                   // CLI.print("");
                                    CLI.print("Enable UVBR?");
                                    CLI.print("");
                                    System.out.print(": ");
                                    String input = readInput();
                                    MindGRAF_Parser parser = new MindGRAF_Parser(
                                            new StringReader(input.trim()));
                                    if (input.trim().toLowerCase().equals("exit")) {
                                        CLI.print("Exiting MindGRAF...");
                                        break;
                                    }
                                    try {
                                        parser.UVBR();
                                        uvbr = false;
                                    } catch (ParseException e) {
                                        // TODO Auto-generated catch block
                                        // e.printStackTrace();
                                        print("");
                                        CLI.print("Wrong Command");
                                        print("");
                                        continue;
                                    }
                                } else {
                                    CLI.print("");
                                    CLI.print("---------------------------------------------------------------");
                                    CLI.print("Enter a command (or 'exit' to quit)");
                                    CLI.print("");
                                    System.out.print(": ");
                                    String input = readInput();

                                    if (input.trim().toLowerCase().equals("exit")) {
                                        CLI.print("Exiting MindGRAF...");
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
