package com.library.util;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {

    private static class PdfDocument {
        private final List<Long> offsets = new ArrayList<>();
        private final List<byte[]> objects = new ArrayList<>();
        private int objCount = 0;

        public int createObject(byte[] body) {
            objCount++;
            objects.add(body);
            return objCount;
        }

        public void write(BufferedOutputStream out) throws IOException {
            // Write Header
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            out.write("%\u00e2\u00e3\u00cf\u00d3\n".getBytes(StandardCharsets.US_ASCII)); // binary marker

            long currentOffset = 15; // length of header

            // Write Objects and track offsets
            for (int i = 0; i < objects.size(); i++) {
                offsets.add(currentOffset);
                byte[] header = ((i + 1) + " 0 obj\n").getBytes(StandardCharsets.US_ASCII);
                byte[] footer = "\nendobj\n".getBytes(StandardCharsets.US_ASCII);
                
                out.write(header);
                out.write(objects.get(i));
                out.write(footer);
                
                currentOffset += header.length + objects.get(i).length + footer.length;
            }

            // Write Xref Table
            long xrefOffset = currentOffset;
            out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
            out.write(("0 " + (objCount + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
            for (long offset : offsets) {
                String formatted = String.format("%010d 00000 n \n", offset);
                out.write(formatted.getBytes(StandardCharsets.US_ASCII));
            }

            // Write Trailer
            out.write("trailer\n".getBytes(StandardCharsets.US_ASCII));
            out.write(("<< /Size " + (objCount + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.US_ASCII));
            out.write("startxref\n".getBytes(StandardCharsets.US_ASCII));
            out.write((xrefOffset + "\n").getBytes(StandardCharsets.US_ASCII));
            out.write("%%EOF\n".getBytes(StandardCharsets.US_ASCII));
        }
    }

    public static void generateLibraryReport(String filePath, List<Book> books, List<Member> members, List<Transaction> transactions) {
        PdfDocument pdf = new PdfDocument();

        // Object 1: Catalog
        // Object 2: Pages
        // Object 3: Font Regular (Helvetica)
        // Object 4: Font Bold (Helvetica-Bold)

        // Pre-create placeholder list for kids
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

        buildPdfReport(filePath, pdf, lines);
    }

    public static void generateFinancialReport(String filePath, List<Book> books, List<Member> members, List<Transaction> transactions) {
        PdfDocument pdf = new PdfDocument();

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

        buildPdfReport(filePath, pdf, lines);
    }

    private static void buildPdfReport(String filePath, PdfDocument pdf, List<String> lines) {
        // Divide text lines into pages (max 45 lines per page for formatting)
        List<List<String>> pages = new ArrayList<>();
        List<String> curPage = new ArrayList<>();
        for (String line : lines) {
            curPage.add(line);
            if (curPage.size() >= 42) { // 42 lines per page
                pages.add(curPage);
                curPage = new ArrayList<>();
            }
        }
        if (!curPage.isEmpty()) {
            pages.add(curPage);
        }

        // Add dummy catalog objects to catalog builder
        // Obj 1: Catalog, Obj 2: Pages Parent, Obj 3: Regular Font, Obj 4: Bold Font
        // We write bodies now

        int regularFontObj = 3;
        int boldFontObj = 4;

        // Obj 1: Catalog
        pdf.createObject("/Type /Catalog /Pages 2 0 R".getBytes(StandardCharsets.US_ASCII));
        
        // Obj 2 placeholder (we will fill pages metadata next)
        // Catalog + Pages parent + Fonts = 4 objects.
        // Pages start from Obj 5.
        // We pre-compute Page Obj IDs. For page index i (0..N-1), Page is (5 + 2*i), Content is (6 + 2*i)
        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < pages.size(); i++) {
            kids.append(5 + 2 * i).append(" 0 R ");
        }
        kids.append("]");
        
        // Rewrite Obj 2 body: Pages Parent
        byte[] pagesParentBody = ("/Type /Pages /Kids " + kids.toString() + " /Count " + pages.size()).getBytes(StandardCharsets.US_ASCII);
        pdf.createObject(pagesParentBody); // Catalog is 1, this becomes 2

        // Obj 3: Font F1 (Helvetica)
        pdf.createObject("/Type /Font /Subtype /Type1 /BaseFont /Helvetica".getBytes(StandardCharsets.US_ASCII));
        
        // Obj 4: Font F2 (Helvetica-Bold)
        pdf.createObject("/Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold".getBytes(StandardCharsets.US_ASCII));

        // Create Page and Content Stream Objects
        for (int i = 0; i < pages.size(); i++) {
            List<String> pageLines = pages.get(i);
            int contentObjId = 6 + 2 * i;

            // Page Object
            String pageBody = "/Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentObjId + " 0 R";
            pdf.createObject(pageBody.getBytes(StandardCharsets.US_ASCII));

            // Content Stream Object
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 10 Tf\n"); // Regular Helvetica size 10
            stream.append("14 TL\n"); // Leading 14
            stream.append("50 780 Td\n"); // Position at margin

            for (String line : pageLines) {
                // Escape parentheses
                String escapedLine = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
                
                // If it is a header line (like title or category divider), print in bold
                if (line.startsWith("==") || line.startsWith("---")) {
                    stream.append("/F2 10 Tf\n"); // Bold
                } else {
                    stream.append("/F1 10 Tf\n"); // Regular
                }
                
                stream.append("(").append(escapedLine).append(") Tj T*\n");
            }
            stream.append("ET\n");

            byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
            
            // Build Stream Dictionary
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                bos.write(("<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
                bos.write(streamBytes);
                bos.write("\nendstream".getBytes(StandardCharsets.US_ASCII));
            } catch (IOException ignored) {}

            pdf.createObject(bos.toByteArray());
        }

        // Save PDF to file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            pdf.write(bos);
        } catch (IOException e) {
            System.err.println("Error writing PDF report: " + e.getMessage());
        }
        
        // Also copy to user's Downloads folder
        try {
            java.io.File source = new java.io.File(filePath);
            java.io.File destDir = new java.io.File("C:\\Users\\admin\\Downloads");
            if (destDir.exists() && destDir.isDirectory()) {
                java.io.File dest = new java.io.File(destDir, source.getName());
                try (java.nio.channels.FileChannel srcChannel = new java.io.FileInputStream(source).getChannel();
                     java.nio.channels.FileChannel destChannel = new java.io.FileOutputStream(dest).getChannel()) {
                    destChannel.transferFrom(srcChannel, 0, srcChannel.size());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to copy PDF to standard Downloads: " + e.getMessage());
        }
    }
}
