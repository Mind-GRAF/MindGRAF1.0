package edu.guc.mind_graf.parser;

import java.util.Scanner;

public class test {
    private static Scanner sc = new Scanner(System.in); // Declare Scanner as a class variable

    public static String readInput() {
        return sc.nextLine(); // Read input using the class-level Scanner
    }

    public static void main(String[] args) {
        String input1 = readInput(); // Read the first input
        String input2 = readInput(); // Read the second input
        // System.out.println("Input 1: " + input1);
        // System.out.println("Input 2: " + input2);
    }
}
