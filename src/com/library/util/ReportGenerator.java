package com.library.util;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    public static void generateLibraryReport(String filePath, List<Book> books, List<Member> members, List<Transaction> transactions) {
        List<String> lines = new ArrayList<>();
        lines.add("==========================================================================");
        lines.add("                  LIBRARY SYSTEM GENERAL AUDIT REPORT                     ");
        lines.add("==========================================================================");
        lines.add("Date Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        lines.add("");
        
        lines.add("--- OVERVIEW SUMMARY ---");
        lines.add("Total Books in Library : " + books.size());
        long available = books.stream().filter(b -> b.getStatus().equalsIgnoreCase("Available")).count();
        long issued = books.stream().filter(b -> b.getStatus().equalsIgnoreCase("Issued")).count();
        long reserved = books.stream().filter(b -> b.getStatus().equalsIgnoreCase("Reserved")).count();
        double totalFines = transactions.stream().filter(t -> t.getType().equals("FINE")).mapToDouble(Transaction::getCost).sum();

        lines.add("Available Books        : " + available);
        lines.add("Issued Books           : " + issued);
        lines.add("Reserved Books         : " + reserved);
        lines.add("Registered Members     : " + members.size());
        lines.add("Total Fines Collected  : Rs. " + String.format("%.2f", totalFines));
        lines.add("");

        lines.add("--- REGISTERED BOOKS IN INVENTORY ---");
        lines.add(String.format("%-8s | %-25s | %-18s | %-12s | %-10s", "Book ID", "Title", "Author", "Category", "Status"));
        lines.add("--------------------------------------------------------------------------");
        for (Book b : books) {
            String title = b.getTitle().length() > 24 ? b.getTitle().substring(0, 22) + ".." : b.getTitle();
            String author = b.getAuthor().length() > 17 ? b.getAuthor().substring(0, 15) + ".." : b.getAuthor();
            lines.add(String.format("%-8s | %-25s | %-18s | %-12s | %-10s", b.getId(), title, author, b.getCategory(), b.getStatus()));
        }
        lines.add("");

        lines.add("--- SYSTEM MEMBERS ---");
        lines.add(String.format("%-8s | %-20s | %-10s | %-12s | %-12s", "Mem ID", "Name", "Level", "Wallet Balance", "Premium Status"));
        lines.add("--------------------------------------------------------------------------");
        for (Member m : members) {
            String name = m.getName().length() > 19 ? m.getName().substring(0, 17) + ".." : m.getName();
            lines.add(String.format("%-8s | %-20s | %-10s | Rs. %-9.2f | %-12s", 
                m.getId(), name, m.getRank().split(" ")[0], m.getWalletBalance(), m.isPremium() ? "Premium" : "Regular"));
        }
        lines.add("");

        lines.add("--- RECENT TRANSACTIONS LOG ---");
        lines.add(String.format("%-11s | %-8s | %-15s | %-32s", "Date", "Mem ID", "Type", "Details"));
        lines.add("--------------------------------------------------------------------------");
        int count = 0;
        for (int i = transactions.size() - 1; i >= 0 && count < 25; i--) {
            Transaction t = transactions.get(i);
            String detail = t.getDetail().length() > 30 ? t.getDetail().substring(0, 28) + ".." : t.getDetail();
            lines.add(String.format("%-11s | %-8s | %-15s | %-32s", t.getDate(), t.getMemberId(), t.getType(), detail));
            count++;
        }

        writeTextReport(filePath, lines);
    }

    public static void generateFinancialReport(String filePath, List<Book> books, List<Member> members, List<Transaction> transactions) {
        List<String> lines = new ArrayList<>();
        lines.add("==========================================================================");
        lines.add("                  LIBRARY SYSTEM REVENUE & FINANCIAL AUDIT                ");
        lines.add("==========================================================================");
        lines.add("Date Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        lines.add("");

        // Calculations
        double rentalRevenue = 0.0;
        double bookSalesRevenue = 0.0;
        double premiumRevenue = 0.0;
        double fineRevenue = 0.0;

        for (Transaction t : transactions) {
            switch (t.getType()) {
                case "BORROW":
                    rentalRevenue += t.getCost();
                    break;
                case "PURCHASE":
                    bookSalesRevenue += t.getCost();
                    break;
                case "MEMBERSHIP":
                    premiumRevenue += t.getCost();
                    break;
                case "FINE":
                    fineRevenue += t.getCost();
                    break;
            }
        }
        double totalRevenue = rentalRevenue + bookSalesRevenue + premiumRevenue + fineRevenue;

        lines.add("--- FINANCIAL REVENUE DASHBOARD ---");
        lines.add("Total Rental Revenue       : Rs. " + String.format("%.2f", rentalRevenue));
        lines.add("Total Book Sales Revenue   : Rs. " + String.format("%.2f", bookSalesRevenue));
        lines.add("Premium Membership Revenue : Rs. " + String.format("%.2f", premiumRevenue));
        lines.add("Fines Collected            : Rs. " + String.format("%.2f", fineRevenue));
        lines.add("--------------------------------------------------------------------------");
        lines.add("TOTAL ACCUMULATED REVENUE  : Rs. " + String.format("%.2f", totalRevenue));
        lines.add("");

        lines.add("--- RENTAL ANALYTICS & POPULAR BOOKS ---");
        lines.add(String.format("%-8s | %-30s | %-12s | %-12s | %-12s", "Book ID", "Book Title", "Times Issued", "Rent Count", "Sale Count"));
        lines.add("--------------------------------------------------------------------------");
        
        List<Book> sortedBooks = new ArrayList<>(books);
        sortedBooks.sort((a, b) -> Integer.compare(b.getIssueCount(), a.getIssueCount()));

        for (Book b : sortedBooks) {
            if (b.getIssueCount() > 0 || b.getRentCount() > 0 || b.getPurchaseCount() > 0) {
                String title = b.getTitle().length() > 28 ? b.getTitle().substring(0, 26) + ".." : b.getTitle();
                lines.add(String.format("%-8s | %-30s | %-12d | %-12d | %-12d", 
                    b.getId(), title, b.getIssueCount(), b.getRentCount(), b.getPurchaseCount()));
            }
        }
        lines.add("");

        lines.add("--- MEMBER SPENDINGS & BALANCES ---");
        lines.add(String.format("%-8s | %-20s | %-14s | %-12s | %-12s", "Mem ID", "Name", "Wallet Balance", "Rentals Paid", "Sales Paid"));
        lines.add("--------------------------------------------------------------------------");
        for (Member m : members) {
            double memberRent = transactions.stream()
                .filter(t -> t.getMemberId().equals(m.getId()) && t.getType().equals("BORROW"))
                .mapToDouble(Transaction::getCost).sum();
            double memberSale = transactions.stream()
                .filter(t -> t.getMemberId().equals(m.getId()) && t.getType().equals("PURCHASE"))
                .mapToDouble(Transaction::getCost).sum();

            String name = m.getName().length() > 18 ? m.getName().substring(0, 16) + ".." : m.getName();
            lines.add(String.format("%-8s | %-20s | Rs. %-10.2f | Rs. %-9.2f | Rs. %-9.2f", 
                m.getId(), name, m.getWalletBalance(), memberRent, memberSale));
        }

        writeTextReport(filePath, lines);
    }

    private static void writeTextReport(String filePath, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing report text file: " + e.getMessage());
        }

        // Copy duplicate file to standard Downloads folder
        try {
            java.io.File source = new java.io.File(filePath);
            java.io.File destDir = new java.io.File("C:\\Users\\admin\\Downloads");
            if (destDir.exists() && destDir.isDirectory()) {
                java.io.File dest = new java.io.File(destDir, source.getName());
                java.nio.file.Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("Failed to copy text report to standard Downloads folder: " + e.getMessage());
        }
    }
}
