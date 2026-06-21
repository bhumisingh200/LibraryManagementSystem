package com.library.ui;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final Scanner scanner = new Scanner(System.in);

    // ANSI Colors
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright Colors
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        int width = 76;
        System.out.println(BOLD + BRIGHT_CYAN + "╔" + "═".repeat(width - 2) + "╗" + RESET);
        
        int padding = (width - 2 - title.length()) / 2;
        String leftPad = " ".repeat(padding);
        String rightPad = " ".repeat(width - 2 - title.length() - padding);
        
        System.out.println(BOLD + BRIGHT_CYAN + "║" + RESET + BOLD + BRIGHT_YELLOW + leftPad + title + rightPad + RESET + BOLD + BRIGHT_CYAN + "║" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "╚" + "═".repeat(width - 2) + "╝" + RESET);
    }

    public static void printSubHeader(String text) {
        System.out.println(BOLD + BLUE + "─── " + YELLOW + text + BLUE + " " + "─".repeat(Math.max(1, 70 - text.length() - 5)) + RESET);
    }

    public static void drawBox(String title, String[] lines, String color) {
        int width = 60;
        for (String line : lines) {
            width = Math.max(width, line.length() + 6);
        }

        System.out.println(color + "┌" + "─".repeat(width - 2) + "┐" + RESET);
        if (!title.isEmpty()) {
            int pad = (width - 2 - title.length()) / 2;
            System.out.println(color + "│" + RESET + " ".repeat(pad) + BOLD + YELLOW + title + RESET + " ".repeat(width - 2 - title.length() - pad) + color + "│" + RESET);
            System.out.println(color + "├" + "─".repeat(width - 2) + "┤" + RESET);
        }

        for (String line : lines) {
            int padRight = width - 4 - line.length();
            System.out.println(color + "│ " + RESET + line + " ".repeat(padRight) + color + " │" + RESET);
        }
        System.out.println(color + "└" + "─".repeat(width - 2) + "┘" + RESET);
    }

    public static String promptString(String prompt, boolean required) {
        while (true) {
            System.out.print(BOLD + WHITE + prompt + RESET + ": ");
            String input = scanner.nextLine().trim();
            if (required && input.isEmpty()) {
                printError("This field is required. Please try again.");
                continue;
            }
            return input;
        }
    }

    public static String promptPassword(String prompt) {
        System.out.print(BOLD + WHITE + prompt + RESET + ": ");
        // In console application, normal scanner reads password visibly, which is fine for CLI project.
        return scanner.nextLine().trim();
    }

    public static int promptInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(BOLD + WHITE + prompt + RESET + " (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                printError("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please try again.");
            }
        }
    }

    public static double promptDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(BOLD + WHITE + prompt + RESET + " (Min: " + min + ", Max: " + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                printError("Please enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please try again.");
            }
        }
    }

    public static boolean promptBoolean(String prompt) {
        while (true) {
            System.out.print(BOLD + WHITE + prompt + RESET + " (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y") || input.equals("YES")) {
                return true;
            }
            if (input.equals("N") || input.equals("NO")) {
                return false;
            }
            printError("Please enter Y or N.");
        }
    }

    public static void printError(String message) {
        System.out.println(BOLD + RED + " ✘ [ERROR] " + message + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(BOLD + GREEN + " ✔ [SUCCESS] " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(BOLD + YELLOW + " ⚠ [WARNING] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BOLD + CYAN + " ℹ [INFO] " + message + RESET);
    }

    public static void drawTable(String[] headers, List<String[]> rows) {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i < widths.length) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }
            }
        }

        // Draw top line
        StringBuilder sb = new StringBuilder();
        sb.append(CYAN).append("┌");
        for (int i = 0; i < widths.length; i++) {
            sb.append("─".repeat(widths[i] + 2));
            if (i < widths.length - 1) sb.append("┬");
        }
        sb.append("┐").append(RESET);
        System.out.println(sb);

        // Draw headers
        sb = new StringBuilder();
        sb.append(CYAN).append("│").append(RESET);
        for (int i = 0; i < headers.length; i++) {
            String title = headers[i];
            int pad = widths[i] - title.length();
            sb.append(" ").append(BOLD).append(YELLOW).append(title).append(RESET).append(" ".repeat(pad + 1));
            sb.append(CYAN).append("│").append(RESET);
        }
        System.out.println(sb);

        // Draw separator
        sb = new StringBuilder();
        sb.append(CYAN).append("├");
        for (int i = 0; i < widths.length; i++) {
            sb.append("─".repeat(widths[i] + 2));
            if (i < widths.length - 1) sb.append("┼");
        }
        sb.append("┤").append(RESET);
        System.out.println(sb);

        // Draw rows
        for (String[] row : rows) {
            sb = new StringBuilder();
            sb.append(CYAN).append("│").append(RESET);
            for (int i = 0; i < headers.length; i++) {
                String val = (i < row.length) ? row[i] : "";
                int pad = widths[i] - val.length();
                sb.append(" ").append(val).append(" ".repeat(pad + 1));
                sb.append(CYAN).append("│").append(RESET);
            }
            System.out.println(sb);
        }

        // Draw bottom line
        sb = new StringBuilder();
        sb.append(CYAN).append("└");
        for (int i = 0; i < widths.length; i++) {
            sb.append("─".repeat(widths[i] + 2));
            if (i < widths.length - 1) sb.append("┴");
        }
        sb.append("┘").append(RESET);
        System.out.println(sb);
    }

    public static void pause() {
        System.out.println(BOLD + YELLOW + "\nPress Enter to continue..." + RESET);
        scanner.nextLine();
    }
}
