package edu.guc.mind_graf.parser;

import java.io.StringReader;
import java.util.Scanner;
public class example {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter an arithmetic expression with a print command: ");
        String input = scanner.nextLine();
        
        try {
            ArithmeticParser parser = new ArithmeticParser(new StringReader(input));
            parser.Start(); // Parse and execute the command
        } catch (ParseException e) {
            System.err.println("Parsing failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
