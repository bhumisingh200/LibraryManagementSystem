package com.library;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Review;
import com.library.model.Transaction;
import com.library.service.LibraryService;
import com.library.ui.ConsoleUI;
import com.library.util.PdfGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final LibraryService service = new LibraryService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        boolean exit = false;
        while (!exit) {
            ConsoleUI.clearScreen();
            System.out.println(ConsoleUI.BOLD + ConsoleUI.CYAN + "====================================================" + ConsoleUI.RESET);
            System.out.println(ConsoleUI.BOLD + ConsoleUI.YELLOW + "            📚 LIBRARY MANAGEMENT SYSTEM            " + ConsoleUI.RESET);
            System.out.println(ConsoleUI.BOLD + ConsoleUI.CYAN + "====================================================" + ConsoleUI.RESET);
            System.out.println("1. Login");
            System.out.println("2. Register as Member");
            System.out.println("3. Exit");
            System.out.println("────────────────────────────────────────────────────");
            int choice = ConsoleUI.promptInt("Enter Choice", 1, 3);

            switch (choice) {
                case 1:
                    loginFlow();
                    break;
                case 2:
                    registerFlow();
                    break;
                case 3:
                    ConsoleUI.printInfo("Thank you for using the Library Management System. Goodbye!");
                    exit = true;
                    break;
            }
        }
    }

    private static void loginFlow() {
        ConsoleUI.printSubHeader("SYSTEM LOGIN");
        String username = ConsoleUI.promptString("Username", true);
        String password = ConsoleUI.promptPassword("Password");

        // Admin hardcoded login
        if (username.equals("admin") && password.equals("admin")) {
            adminDashboard();
            return;
        }

        // Librarian hardcoded login
        if (username.equals("lib") && password.equals("lib")) {
            librarianDashboard();
            return;
        }

        // Member DB login
        Member member = service.authenticateMember(username, password);
        if (member != null) {
            memberDashboard(member);
        } else {
            ConsoleUI.printError("Invalid Username or Password.");
            ConsoleUI.pause();
        }
    }

    private static void registerFlow() {
        ConsoleUI.printSubHeader("MEMBER REGISTRATION");
        String name = ConsoleUI.promptString("Full Name", true);
        
        // Ensure username is unique
        String username;
        while (true) {
            username = ConsoleUI.promptString("Desired Username", true);
            String finalUsername = username;
            boolean exists = service.getMembers().stream()
                .anyMatch(m -> m.getUsername().equalsIgnoreCase(finalUsername));
            if (exists) {
                ConsoleUI.printError("Username already taken. Please choose another.");
            } else {
                break;
            }
        }

        String password = ConsoleUI.promptString("Password", true);
        service.registerMember(name, username, password);
        ConsoleUI.printSuccess("Registration Successful! You can now login with your username.");
        ConsoleUI.pause();
    }

    // ==========================================
    // ADMIN DASHBOARD
    // ==========================================
    private static void adminDashboard() {
        boolean logout = false;
        while (!logout) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("👨💼 ADMIN DASHBOARD");
            System.out.println("1. Add Book (Create)");
            System.out.println("2. Update Book (Update)");
            System.out.println("3. Delete Book (Delete)");
            System.out.println("4. View All Books (Read)");
            System.out.println("5. Manage Members");
            System.out.println("6. Reports Dashboard (CLI)");
            System.out.println("7. Financial Analytics & Revenue");
            System.out.println("8. Library Insights");
            System.out.println("9. Logout");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 9);
            switch (choice) {
                case 1:
                    adminAddBook();
                    break;
                case 2:
                    adminUpdateBook();
                    break;
                case 3:
                    adminDeleteBook();
                    break;
                case 4:
                    adminViewBooks();
                    break;
                case 5:
                    adminManageMembers();
                    break;
                case 6:
                    adminReportsDashboard();
                    break;
                case 7:
                    adminFinancialAnalytics();
                    break;
                case 8:
                    adminLibraryInsights();
                    break;
                case 9:
                    logout = true;
                    break;
            }
        }
    }

    private static void adminAddBook() {
        ConsoleUI.printSubHeader("ADD NEW BOOK");
        String title = ConsoleUI.promptString("Title", true);
        String author = ConsoleUI.promptString("Author", true);
        
        System.out.println("\nCategories: Programming, Science, History, Fiction, Self-Help");
        String category = ConsoleUI.promptString("Category", true);
        double price = ConsoleUI.promptDouble("Purchase Price (Rs.)", 10.0, 10000.0);

        service.addBook(title, author, category, price);
        ConsoleUI.printSuccess("Book added successfully!");
        ConsoleUI.pause();
    }

    private static void adminViewBooks() {
        ConsoleUI.printSubHeader("ALL BOOKS IN LIBRARY");
        printAllBooks();
        ConsoleUI.pause();
    }

    private static void adminUpdateBook() {
        ConsoleUI.printSubHeader("UPDATE BOOK DETAILS");
        printAllBooks();
        System.out.println();
        String id = ConsoleUI.promptString("Enter Book ID to Update", true);
        Book b = service.findBookById(id);
        if (b == null) {
            ConsoleUI.printError("Book not found.");
            ConsoleUI.pause();
            return;
        }

        System.out.println("Current details: " + b.getTitle() + " | " + b.getAuthor() + " | " + b.getCategory() + " | Rs. " + b.getPrice());
        String title = ConsoleUI.promptString("New Title (leave blank to keep current)", false);
        String author = ConsoleUI.promptString("New Author (leave blank to keep current)", false);
        String category = ConsoleUI.promptString("New Category (leave blank to keep current)", false);
        
        String priceStr = ConsoleUI.promptString("New Price (leave blank to keep current)", false);
        double price = -1;
        if (!priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}
        }

        if (service.updateBook(id, title, author, category, price)) {
            ConsoleUI.printSuccess("Book details updated successfully!");
        } else {
            ConsoleUI.printError("Update failed.");
        }
        ConsoleUI.pause();
    }

    private static void adminDeleteBook() {
        ConsoleUI.printSubHeader("DELETE BOOK");
        printAllBooks();
        System.out.println();
        String id = ConsoleUI.promptString("Enter Book ID to Delete", true);
        if (service.deleteBook(id)) {
            ConsoleUI.printSuccess("Book deleted from inventory.");
        } else {
            ConsoleUI.printError("Book not found.");
        }
        ConsoleUI.pause();
    }

    private static void printAllBooks() {
        List<Book> books = service.getBooks();
        if (books.isEmpty()) {
            ConsoleUI.printWarning("No books currently in the library inventory.");
            return;
        }
        String[] headers = {"Book ID", "Title", "Author", "Category", "Status", "Price (Buy)", "Issued To"};
        List<String[]> rows = books.stream()
            .map(b -> new String[]{
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getCategory(),
                b.getStatus().equals("Available") ? ConsoleUI.GREEN + b.getStatus() + ConsoleUI.RESET : 
                b.getStatus().equals("Bought") ? ConsoleUI.PURPLE + b.getStatus() + ConsoleUI.RESET : ConsoleUI.RED + b.getStatus() + ConsoleUI.RESET,
                "Rs. " + b.getPrice(),
                b.getIssuedTo().isEmpty() ? "None" : b.getIssuedTo()
            })
            .collect(Collectors.toList());
        ConsoleUI.drawTable(headers, rows);
    }

    private static void adminManageMembers() {
        boolean back = false;
        while (!back) {
            ConsoleUI.clearScreen();
            ConsoleUI.printSubHeader("MANAGE MEMBERS");
            System.out.println("1. Register Member");
            System.out.println("2. Update Member Details");
            System.out.println("3. Remove Member");
            System.out.println("4. View Member Profile & History");
            System.out.println("5. Back to Main Menu");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 5);
            switch (choice) {
                case 1:
                    registerFlow();
                    break;
                case 2:
                    adminUpdateMember();
                    break;
                case 3:
                    adminRemoveMember();
                    break;
                case 4:
                    adminViewMemberProfile();
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    private static void adminUpdateMember() {
        ConsoleUI.printSubHeader("UPDATE MEMBER DETAILS");
        String id = ConsoleUI.promptString("Member ID", true);
        Member m = service.findMemberById(id);
        if (m == null) {
            ConsoleUI.printError("Member not found.");
            ConsoleUI.pause();
            return;
        }

        System.out.println("Current Profile: " + m.getName() + " (Username: " + m.getUsername() + ")");
        String name = ConsoleUI.promptString("New Name (leave blank to keep current)", false);
        String password = ConsoleUI.promptString("New Password (leave blank to keep current)", false);

        service.updateMember(id, name, password);
        ConsoleUI.printSuccess("Member details updated!");
        ConsoleUI.pause();
    }

    private static void adminRemoveMember() {
        ConsoleUI.printSubHeader("REMOVE MEMBER");
        String id = ConsoleUI.promptString("Member ID", true);
        if (service.removeMember(id)) {
            ConsoleUI.printSuccess("Member removed successfully.");
        } else {
            ConsoleUI.printError("Member not found.");
        }
        ConsoleUI.pause();
    }

    private static void adminViewMemberProfile() {
        ConsoleUI.printSubHeader("MEMBER PROFILE & HISTORY");
        String id = ConsoleUI.promptString("Member ID", true);
        Member m = service.findMemberById(id);
        if (m == null) {
            ConsoleUI.printError("Member not found.");
            ConsoleUI.pause();
            return;
        }

        ConsoleUI.clearScreen();
        String[] profileInfo = {
            "ID: " + m.getId(),
            "Name: " + m.getName(),
            "Username: " + m.getUsername(),
            "Tier: " + (m.isPremium() ? ConsoleUI.BOLD + ConsoleUI.YELLOW + "PREMIUM (Expires: " + m.getPremiumExpiryDate() + ")" + ConsoleUI.RESET : "REGULAR"),
            "Wallet Balance: Rs. " + String.format("%.2f", m.getWalletBalance()),
            "Earned Points: " + m.getPoints() + " (" + m.getRank() + ")",
            "Reading Streak: " + m.getReadingStreak() + " days (Longest: " + m.getLongestStreak() + " days)",
            "Monthly Challenge: " + m.getChallengeProgress() + " / " + m.getChallengeGoal() + " books read"
        };
        ConsoleUI.drawBox("MEMBER PROFILE", profileInfo, ConsoleUI.BLUE);

        // Books Issued
        System.out.println(ConsoleUI.BOLD + ConsoleUI.YELLOW + "\n📖 Active Borrows (Issued Books):" + ConsoleUI.RESET);
        List<Book> activeBorrows = service.getActiveBorrows(m.getId());
        if (activeBorrows.isEmpty()) {
            System.out.println("  No active borrows.");
        } else {
            String[] headers = {"Book ID", "Title", "Category", "Issue Date", "Due Date"};
            List<String[]> rows = activeBorrows.stream()
                .map(b -> new String[]{b.getId(), b.getTitle(), b.getCategory(), b.getIssueDate(), b.getDueDate()})
                .collect(Collectors.toList());
            ConsoleUI.drawTable(headers, rows);
        }

        // Books Purchased
        System.out.println(ConsoleUI.BOLD + ConsoleUI.YELLOW + "\n🛍️ Purchased Books:" + ConsoleUI.RESET);
        List<Book> purchased = service.getPurchasedBooks(m.getId());
        if (purchased.isEmpty()) {
            System.out.println("  No purchased books.");
        } else {
            for (Book b : purchased) {
                System.out.println("  ✓ [" + b.getId() + "] " + b.getTitle() + " (" + b.getCategory() + ")");
            }
        }

        // Complete Reading History
        System.out.println(ConsoleUI.BOLD + ConsoleUI.YELLOW + "\n📜 Complete Reading History:" + ConsoleUI.RESET);
        List<String> readList = service.getBooksReadList(m.getId());
        if (readList.isEmpty()) {
            System.out.println("  No books read yet.");
        } else {
            for (String title : readList) {
                System.out.println("  ✓ " + title);
            }
        }

        ConsoleUI.pause();
    }

    private static void adminReportsDashboard() {
        ConsoleUI.printSubHeader("REPORTS DASHBOARD");

        long total = service.getBooks().size();
        long available = service.getBooks().stream().filter(b -> b.getStatus().equals("Available")).count();
        long issued = service.getBooks().stream().filter(b -> b.getStatus().equals("Issued")).count();
        long members = service.getMembers().size();
        
        double totalFines = service.getTransactions().stream()
            .filter(t -> t.getType().equals("FINE"))
            .mapToDouble(Transaction::getCost)
            .sum();

        String[] reportData = {
            "Total Books: " + total,
            "Available Books: " + available,
            "Issued Books: " + issued,
            "Total Members: " + members,
            "Total Fines Collected: Rs. " + String.format("%.2f", totalFines)
        };
        ConsoleUI.drawBox("SYSTEM STATS SUMMARY", reportData, ConsoleUI.GREEN);
        ConsoleUI.pause();
    }

    private static void adminFinancialAnalytics() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("📊 REVENUE DASHBOARD");

        double rentalRevenue = 0.0;
        double bookSalesRevenue = 0.0;
        double premiumRevenue = 0.0;
        double fineRevenue = 0.0;

        for (Transaction t : service.getTransactions()) {
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

        String[] revSummary = {
            "Total Rental Revenue: Rs. " + String.format("%.2f", rentalRevenue),
            "Total Book Sales: Rs. " + String.format("%.2f", bookSalesRevenue),
            "Premium Membership Revenue: Rs. " + String.format("%.2f", premiumRevenue),
            "Fines Collected: Rs. " + String.format("%.2f", fineRevenue),
            "---------------------------------------",
            "TOTAL ACCUMULATED REVENUE: Rs. " + String.format("%.2f", totalRevenue)
        };
        ConsoleUI.drawBox("FINANCIALS", revSummary, ConsoleUI.YELLOW);

        // Rental Analytics
        System.out.println(ConsoleUI.BOLD + ConsoleUI.CYAN + "\n📈 Top Issued/Rented Books:" + ConsoleUI.RESET);
        List<Book> sortedByIssues = service.getBooks().stream()
            .sorted((a, b) -> Integer.compare(b.getIssueCount(), a.getIssueCount()))
            .limit(3)
            .collect(Collectors.toList());
        int rank = 1;
        for (Book b : sortedByIssues) {
            System.out.println("  " + rank + ". " + b.getTitle() + " (" + b.getIssueCount() + " times)");
            rank++;
        }

        System.out.println(ConsoleUI.BOLD + ConsoleUI.CYAN + "\n🛍️ Top Purchased Books:" + ConsoleUI.RESET);
        List<Book> sortedByPurchases = service.getBooks().stream()
            .sorted((a, b) -> Integer.compare(b.getPurchaseCount(), a.getPurchaseCount()))
            .limit(3)
            .collect(Collectors.toList());
        rank = 1;
        for (Book b : sortedByPurchases) {
            System.out.println("  " + rank + ". " + b.getTitle() + " (" + b.getPurchaseCount() + " purchases)");
            rank++;
        }

        System.out.println("\n────────────────────────────────────────────────────");
        boolean export = ConsoleUI.promptBoolean("Would you like to export complete PDF reports? (library_report.pdf & financial_report.pdf)");
        if (export) {
            PdfGenerator.generateLibraryReport("library_report.pdf", service.getBooks(), service.getMembers(), service.getTransactions());
            PdfGenerator.generateFinancialReport("financial_report.pdf", service.getBooks(), service.getMembers(), service.getTransactions());
            ConsoleUI.printSuccess("Reports exported successfully to project folder!");
        }
        ConsoleUI.pause();
    }

    private static void adminLibraryInsights() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("💡 LIBRARY INSIGHTS & BUSINESS INTELLIGENCE");

        Map<String, String> insights = service.getLibraryInsights();
        String[] insText = {
            "📚 Total Books: " + insights.get("totalBooks"),
            "",
            "🔥 Trending Book: " + insights.get("trendingBook"),
            "",
            "👤 Most Active Member: " + insights.get("mostActiveMember"),
            "",
            "📖 Most Popular Category: " + insights.get("mostPopularCategory"),
            "",
            "📈 Monthly Growth: " + insights.get("growth")
        };
        ConsoleUI.drawBox("BUSINESS INSIGHTS", insText, ConsoleUI.PURPLE);
        ConsoleUI.pause();
    }


    // ==========================================
    // LIBRARIAN DASHBOARD
    // ==========================================
    private static void librarianDashboard() {
        boolean logout = false;
        while (!logout) {
            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("👨🔧 LIBRARIAN DASHBOARD");
            System.out.println("1. Smart Search Books");
            System.out.println("2. Issue Book to Member");
            System.out.println("3. Return Book (Receive)");
            System.out.println("4. View All Books In Inventory");
            System.out.println("5. Collect Fine (Manual Cash Payment)");
            System.out.println("6. Logout");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 6);
            switch (choice) {
                case 1:
                    librarianSearch();
                    break;
                case 2:
                    librarianIssue();
                    break;
                case 3:
                    librarianReturn();
                    break;
                case 4:
                    librarianViewBooks();
                    break;
                case 5:
                    librarianCollectFine();
                    break;
                case 6:
                    logout = true;
                    break;
            }
        }
    }

    private static void librarianSearch() {
        searchBooksMenu("LIBRARIAN");
    }

    private static void searchBooksMenu(String roleHeader) {
        boolean back = false;
        while (!back) {
            ConsoleUI.clearScreen();
            ConsoleUI.printSubHeader(roleHeader + " - SEARCH BOOKS");
            System.out.println("1. Smart Search (Global Query)");
            System.out.println("2. Search by Book ID");
            System.out.println("3. Search by Title");
            System.out.println("4. Search by Author");
            System.out.println("5. Search by Category");
            System.out.println("6. View All Books");
            System.out.println("7. Back");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 7);
            if (choice == 7) {
                back = true;
                continue;
            }

            List<Book> results = new ArrayList<>();
            String promptText = "";
            switch (choice) {
                case 1:
                    promptText = "Enter search term (ID, Title, Author, Category)";
                    break;
                case 2:
                    promptText = "Enter Book ID query";
                    break;
                case 3:
                    promptText = "Enter Title query";
                    break;
                case 4:
                    promptText = "Enter Author query";
                    break;
                case 5:
                    promptText = "Enter Category query";
                    break;
                case 6:
                    results = service.getBooks();
                    break;
            }

            if (choice != 6) {
                String query = ConsoleUI.promptString(promptText, false);
                switch (choice) {
                    case 1: results = service.smartSearch(query); break;
                    case 2: results = service.searchById(query); break;
                    case 3: results = service.searchByTitle(query); break;
                    case 4: results = service.searchByAuthor(query); break;
                    case 5: results = service.searchByCategory(query); break;
                }
            }

            if (results.isEmpty()) {
                ConsoleUI.printWarning("No books matched the search criteria.");
            } else {
                String[] headers = {"Book ID", "Title", "Author", "Category", "Status", "Price (Buy)", "Issued To"};
                List<String[]> rows = results.stream()
                    .map(b -> new String[]{
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getCategory(),
                        b.getStatus().equals("Available") ? ConsoleUI.GREEN + b.getStatus() + ConsoleUI.RESET : 
                        b.getStatus().equals("Bought") ? ConsoleUI.PURPLE + b.getStatus() + ConsoleUI.RESET : ConsoleUI.RED + b.getStatus() + ConsoleUI.RESET,
                        "Rs. " + b.getPrice(),
                        b.getIssuedTo().isEmpty() ? "None" : b.getIssuedTo()
                    })
                    .collect(Collectors.toList());
                ConsoleUI.drawTable(headers, rows);
            }
            ConsoleUI.pause();
        }
    }

    private static void librarianIssue() {
        ConsoleUI.printSubHeader("ISSUE BOOK");
        String bookId = ConsoleUI.promptString("Book ID", true);
        String memberId = ConsoleUI.promptString("Member ID", true);

        Book b = service.findBookById(bookId);
        Member m = service.findMemberById(memberId);

        if (b == null) {
            ConsoleUI.printError("Book not found.");
            ConsoleUI.pause();
            return;
        }
        if (m == null) {
            ConsoleUI.printError("Member not found.");
            ConsoleUI.pause();
            return;
        }

        System.out.println("\nBorrow Durations:");
        System.out.println("1. 7 Days  - Rs. 20 (Premium: Rs. 16)");
        System.out.println("2. 15 Days - Rs. 35 (Premium: Rs. 28)");
        System.out.println("3. 30 Days - Rs. 60 (Premium: Rs. 48)");
        int durChoice = ConsoleUI.promptInt("Select Plan", 1, 3);
        int days = (durChoice == 1) ? 7 : (durChoice == 2) ? 15 : 30;

        String res = service.borrowBook(memberId, bookId, days);
        if (res.equals("SUCCESS")) {
            ConsoleUI.printSuccess("Book successfully issued to " + m.getName() + "!");
        } else if (res.equals("ALREADY_ISSUED")) {
            ConsoleUI.printWarning("Book already issued. Would you like to reserve it for this member?");
            boolean reserve = ConsoleUI.promptBoolean("Reserve");
            if (reserve) {
                String reserveRes = service.reserveBook(memberId, bookId);
                if (reserveRes.equals("SUCCESS")) {
                    ConsoleUI.printSuccess("Book reserved successfully in queue!");
                } else {
                    ConsoleUI.printError(reserveRes);
                }
            }
        } else {
            ConsoleUI.printError(res);
        }
        ConsoleUI.pause();
    }

    private static void librarianReturn() {
        ConsoleUI.printSubHeader("RETURN BOOK");
        String bookId = ConsoleUI.promptString("Book ID", true);

        Book b = service.findBookById(bookId);
        if (b == null || !b.getStatus().equals("Issued")) {
            ConsoleUI.printError("Book is not currently issued.");
            ConsoleUI.pause();
            return;
        }

        System.out.println("Due date of this book was: " + b.getDueDate());
        String retSim = ConsoleUI.promptString("Enter Return Date (yyyy-MM-dd) [Leave empty for Today]", false);

        String res = service.returnBook(bookId, retSim);
        if (res.startsWith("SUCCESS_WITH_FINE")) {
            String[] parts = res.split("\\|\\|");
            ConsoleUI.printWarning("Book returned late by " + parts[2] + " days. Fine of Rs. " + parts[1] + " was deducted from Member's wallet.");
        } else if (res.equals("SUCCESS")) {
            ConsoleUI.printSuccess("Book successfully returned and added back to inventory.");
        } else {
            ConsoleUI.printError(res);
        }
        ConsoleUI.pause();
    }

    private static void librarianViewBooks() {
        ConsoleUI.printSubHeader("ALL BOOKS IN LIBRARY");
        List<Book> books = service.getBooks();

        String[] headers = {"Book ID", "Title", "Author", "Category", "Status", "Price"};
        List<String[]> rows = books.stream()
            .map(b -> new String[]{
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getCategory(),
                b.getStatus().equals("Available") ? ConsoleUI.GREEN + b.getStatus() + ConsoleUI.RESET : ConsoleUI.RED + b.getStatus() + ConsoleUI.RESET,
                "Rs. " + b.getPrice()
            })
            .collect(Collectors.toList());

        ConsoleUI.drawTable(headers, rows);
        ConsoleUI.pause();
    }

    private static void librarianCollectFine() {
        ConsoleUI.printSubHeader("COLLECT FINE PAYMENT (CASH)");
        String memberId = ConsoleUI.promptString("Member ID", true);
        Member m = service.findMemberById(memberId);
        if (m == null) {
            ConsoleUI.printError("Member not found.");
            ConsoleUI.pause();
            return;
        }

        double balance = m.getWalletBalance();
        System.out.println("Member Name: " + m.getName());
        System.out.println("Current Wallet Balance: Rs. " + String.format("%.2f", balance));
        if (balance >= 0) {
            ConsoleUI.printInfo("Member has no active fine (balance is positive).");
        } else {
            ConsoleUI.printWarning("Member is in negative balance by Rs. " + String.format("%.2f", Math.abs(balance)) + " due to late return fines.");
        }

        double cashPaid = ConsoleUI.promptDouble("Cash Collected Amount (Rs.)", 1.0, 10000.0);
        if (service.collectFine(memberId, cashPaid)) {
            ConsoleUI.printSuccess("Payment recorded! Wallet updated. New Balance: Rs. " + String.format("%.2f", m.getWalletBalance()));
        } else {
            ConsoleUI.printError("Transaction failed.");
        }
        ConsoleUI.pause();
    }


    // ==========================================
    // MEMBER DASHBOARD
    // ==========================================
    private static void memberDashboard(Member member) {
        boolean logout = false;
        while (!logout) {
            // Reload member data in case changes happened
            Member m = service.findMemberById(member.getId());
            if (m == null) {
                logout = true;
                break;
            }

            ConsoleUI.clearScreen();
            ConsoleUI.printHeader("🎓 MEMBER DASHBOARD - Welcome " + m.getName() + "!");

            // 14. Overdue Notifications on Login Dashboard
            int overdueCount = 0;
            double finePending = 0.0;
            List<Book> activeBorrows = service.getActiveBorrows(m.getId());
            for (Book b : activeBorrows) {
                if (!b.getDueDate().isEmpty()) {
                    LocalDate due = LocalDate.parse(b.getDueDate(), DATE_FORMATTER);
                    if (due.isBefore(LocalDate.now())) {
                        overdueCount++;
                        long daysLate = ChronoUnit.DAYS.between(due, LocalDate.now());
                        finePending += daysLate * 5;
                    }
                }
            }

            if (m.getWalletBalance() < 0) {
                finePending += Math.abs(m.getWalletBalance());
            }

            if (overdueCount > 0 || finePending > 0) {
                System.out.println(ConsoleUI.BOLD + ConsoleUI.RED + "╔════════════════════════ SYSTEM NOTIFICATIONS ════════════════════════╗" + ConsoleUI.RESET);
                if (overdueCount > 0) {
                    System.out.println(ConsoleUI.BOLD + ConsoleUI.RED + "  ⚠️ You have " + overdueCount + " book(s) overdue!" + ConsoleUI.RESET);
                }
                if (finePending > 0) {
                    System.out.println(ConsoleUI.BOLD + ConsoleUI.RED + "  ⚠️ Fine pending/account deficit: Rs. " + String.format("%.2f", finePending) + ConsoleUI.RESET);
                }
                System.out.println(ConsoleUI.BOLD + ConsoleUI.RED + "╚══════════════════════════════════════════════════════════════════════╝" + ConsoleUI.RESET);
                System.out.println();
            }

            // Quick Stats
            System.out.println(ConsoleUI.BOLD + ConsoleUI.CYAN + "Rank: " + ConsoleUI.YELLOW + m.getRank() + ConsoleUI.CYAN + " | Wallet: " + ConsoleUI.GREEN + "Rs. " + String.format("%.2f", m.getWalletBalance()) + ConsoleUI.CYAN + " | Streak: " + ConsoleUI.PURPLE + m.getReadingStreak() + " days" + ConsoleUI.RESET);
            System.out.println();

            System.out.println("1. Browse & Search Books");
            System.out.println("2. Borrow (Rent) Book");
            System.out.println("3. Buy Book (Permanent Purchase)");
            System.out.println("4. Wallet Actions");
            System.out.println("5. My Rentals & Overdue Dates");
            System.out.println("6. Purchase History");
            System.out.println("7. Recommendations Engine");
            System.out.println("8. Reviews & Ratings");
            System.out.println("9. Achievements & Challenges");
            System.out.println("10. Top Leaderboard");
            System.out.println("11. Reserve Book (Join queue)");
            System.out.println("12. Premium Membership Tier");
            System.out.println("13. Logout");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 13);
            switch (choice) {
                case 1:
                    memberSearch();
                    break;
                case 2:
                    memberBorrow(m);
                    break;
                case 3:
                    memberBuy(m);
                    break;
                case 4:
                    memberWallet(m);
                    break;
                case 5:
                    memberRentals(m);
                    break;
                case 6:
                    memberPurchases(m);
                    break;
                case 7:
                    memberRecommendations(m);
                    break;
                case 8:
                    memberReviews();
                    break;
                case 9:
                    memberAchievements(m);
                    break;
                case 10:
                    memberLeaderboard();
                    break;
                case 11:
                    memberReserve(m);
                    break;
                case 12:
                    memberPremium(m);
                    break;
                case 13:
                    logout = true;
                    break;
            }
        }
    }

    private static void memberSearch() {
        searchBooksMenu("MEMBER");
    }

    private static void memberBorrow(Member m) {
        ConsoleUI.printSubHeader("BORROW (RENT) A BOOK");
        printAllBooks();
        System.out.println();
        String id = ConsoleUI.promptString("Enter Book ID to borrow", true);
        
        System.out.println("\nBorrow Durations:");
        System.out.println("1. 7 Days  - Rs. 20 (Premium: Rs. 16)");
        System.out.println("2. 15 Days - Rs. 35 (Premium: Rs. 28)");
        System.out.println("3. 30 Days - Rs. 60 (Premium: Rs. 48)");
        int plan = ConsoleUI.promptInt("Select Plan", 1, 3);
        int days = (plan == 1) ? 7 : (plan == 2) ? 15 : 30;

        String res = service.borrowBook(m.getId(), id, days);
        if (res.equals("SUCCESS")) {
            ConsoleUI.printSuccess("Book borrowed successfully! Track due dates in 'My Rentals'.");
        } else if (res.equals("ALREADY_ISSUED")) {
            ConsoleUI.printWarning("Book already issued to someone else. Would you like to reserve it?");
            boolean r = ConsoleUI.promptBoolean("Reserve");
            if (r) {
                String reserveRes = service.reserveBook(m.getId(), id);
                if (reserveRes.equals("SUCCESS")) {
                    ConsoleUI.printSuccess("Book reserved successfully! You will be in the queue.");
                } else {
                    ConsoleUI.printError(reserveRes);
                }
            }
        } else {
            ConsoleUI.printError(res);
        }
        ConsoleUI.pause();
    }

    private static void memberBuy(Member m) {
        ConsoleUI.printSubHeader("BUY BOOK (PERMANENT PURCHASE)");
        printAllBooks();
        System.out.println();
        String id = ConsoleUI.promptString("Enter Book ID to purchase", true);
        Book b = service.findBookById(id);
        if (b == null) {
            ConsoleUI.printError("Book not found.");
            ConsoleUI.pause();
            return;
        }

        System.out.println("Book: " + b.getTitle() + " by " + b.getAuthor());
        System.out.println("Price: Rs. " + b.getPrice());
        boolean confirm = ConsoleUI.promptBoolean("Confirm permanent purchase?");
        if (confirm) {
            String res = service.buyBook(m.getId(), id);
            if (res.equals("SUCCESS")) {
                ConsoleUI.printSuccess("Book purchased successfully! It is now permanently yours.");
            } else {
                ConsoleUI.printError(res);
            }
        }
        ConsoleUI.pause();
    }

    private static void memberWallet(Member m) {
        boolean back = false;
        while (!back) {
            ConsoleUI.clearScreen();
            ConsoleUI.printSubHeader("MEMBER WALLET SYSTEM");
            System.out.println("Current Wallet Balance: Rs. " + String.format("%.2f", m.getWalletBalance()));
            System.out.println();
            System.out.println("1. Deposit Money");
            System.out.println("2. View Wallet Transaction History");
            System.out.println("3. Back");
            System.out.println("────────────────────────────────────────────────────");

            int choice = ConsoleUI.promptInt("Enter Choice", 1, 3);
            switch (choice) {
                case 1:
                    double deposit = ConsoleUI.promptDouble("Enter amount to deposit (Rs.)", 10.0, 5000.0);
                    service.depositWallet(m.getId(), deposit);
                    ConsoleUI.printSuccess("Deposit successful! New Balance: Rs. " + String.format("%.2f", m.getWalletBalance()));
                    ConsoleUI.pause();
                    break;
                case 2:
                    ConsoleUI.printSubHeader("WALLET TRANSACTION HISTORY");
                    List<Transaction> txHistory = service.getTransactions().stream()
                        .filter(t -> t.getMemberId().equals(m.getId()))
                        .collect(Collectors.toList());

                    if (txHistory.isEmpty()) {
                        System.out.println("No wallet transactions recorded.");
                    } else {
                        String[] headers = {"Date", "Type", "Details", "Amount"};
                        List<String[]> rows = txHistory.stream()
                            .map(t -> new String[]{
                                t.getDate(),
                                t.getType(),
                                t.getDetail(),
                                t.getCost() > 0 ? "Rs. " + t.getCost() : "Rs. 0.00"
                            })
                            .collect(Collectors.toList());
                        ConsoleUI.drawTable(headers, rows);
                    }
                    ConsoleUI.pause();
                    break;
                case 3:
                    back = true;
                    break;
            }
        }
    }

    private static void memberRentals(Member m) {
        ConsoleUI.printSubHeader("MY ACTIVE RENTALS & DUE DATES");
        List<Book> active = service.getActiveBorrows(m.getId());

        if (active.isEmpty()) {
            ConsoleUI.printInfo("You have no active rented books.");
        } else {
            String[] headers = {"Book ID", "Title", "Issue Date", "Due Date", "Status"};
            List<String[]> rows = active.stream()
                .map(b -> {
                    LocalDate due = LocalDate.parse(b.getDueDate(), DATE_FORMATTER);
                    String stat = ConsoleUI.GREEN + "Active" + ConsoleUI.RESET;
                    if (due.isBefore(LocalDate.now())) {
                        long days = ChronoUnit.DAYS.between(due, LocalDate.now());
                        stat = ConsoleUI.RED + "OVERDUE (" + days + " days late, Fine: Rs. " + (days * 5) + ")" + ConsoleUI.RESET;
                    }
                    return new String[]{b.getId(), b.getTitle(), b.getIssueDate(), b.getDueDate(), stat};
                })
                .collect(Collectors.toList());
            ConsoleUI.drawTable(headers, rows);
        }
        ConsoleUI.pause();
    }

    private static void memberPurchases(Member m) {
        ConsoleUI.printSubHeader("MY PURCHASE HISTORY");
        List<Book> bought = service.getPurchasedBooks(m.getId());
        if (bought.isEmpty()) {
            ConsoleUI.printInfo("You have not purchased any books yet.");
        } else {
            for (Book b : bought) {
                System.out.println("  ✓ [" + b.getId() + "] " + b.getTitle() + " by " + b.getAuthor() + " (" + b.getCategory() + ")");
            }
        }
        ConsoleUI.pause();
    }

    private static void memberRecommendations(Member m) {
        ConsoleUI.printSubHeader("RECOMMENDED BOOKS");
        List<Book> recommendations = service.getRecommendations(m.getId());

        if (recommendations.isEmpty()) {
            ConsoleUI.printInfo("No active recommendation matches found right now.");
        } else {
            String[] headers = {"Book ID", "Title", "Author", "Category", "Status"};
            List<String[]> rows = recommendations.stream()
                .map(b -> new String[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getStatus()
                })
                .collect(Collectors.toList());
            ConsoleUI.drawTable(headers, rows);
        }
        ConsoleUI.pause();
    }

    private static void memberReviews() {
        ConsoleUI.printSubHeader("BOOK REVIEWS & RATINGS");
        System.out.println("1. Write a Book Review");
        System.out.println("2. Read Book Reviews");
        System.out.println("3. Back");
        int choice = ConsoleUI.promptInt("Choose", 1, 3);
        if (choice == 3) return;

        if (choice == 1) {
            String id = ConsoleUI.promptString("Book ID to review", true);
            Book b = service.findBookById(id);
            if (b == null) {
                ConsoleUI.printError("Book not found.");
                ConsoleUI.pause();
                return;
            }
            int rating = ConsoleUI.promptInt("Rating (1 to 5 Stars)", 1, 5);
            String comment = ConsoleUI.promptString("Enter your review comment", true);

            service.addReview(id, "Member", rating, comment);
            ConsoleUI.printSuccess("Review submitted! Thank you.");
        } else {
            String id = ConsoleUI.promptString("Book ID to view reviews", true);
            List<Review> bookReviews = service.getReviews().stream()
                .filter(r -> r.getBookId().equalsIgnoreCase(id))
                .collect(Collectors.toList());

            if (bookReviews.isEmpty()) {
                ConsoleUI.printInfo("No reviews submitted for this book yet.");
            } else {
                for (Review r : bookReviews) {
                    System.out.println("\nReviewer: " + r.getMemberName() + " | Date: " + r.getDate());
                    System.out.println("Rating: " + ConsoleUI.YELLOW + r.getStarsStr() + ConsoleUI.RESET);
                    System.out.println("\"" + r.getComment() + "\"");
                    System.out.println("────────────────────────────────────");
                }
            }
        }
        ConsoleUI.pause();
    }

    private static void memberAchievements(Member m) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("🏆 ACHIEVEMENTS & GAMIFICATION");

        String[] info = {
            "Member Name: " + m.getName(),
            "Reader Rank: " + ConsoleUI.BOLD + ConsoleUI.YELLOW + m.getRank() + ConsoleUI.RESET,
            "Total Reader Points: " + m.getPoints() + " pts (Earn +50 pts per book returned)",
            "Current Reading Streak: " + m.getReadingStreak() + " days",
            "Longest Streak Achieved: " + m.getLongestStreak() + " days",
            "",
            "🎯 MONTHLY READING CHALLENGE:",
            "Goal: Read " + m.getChallengeGoal() + " books this month.",
            "Progress: " + m.getChallengeProgress() + " / " + m.getChallengeGoal() + " books read.",
            "Status: " + (m.getChallengeProgress() >= m.getChallengeGoal() ? ConsoleUI.GREEN + "COMPLETED! 🎉" + ConsoleUI.RESET : ConsoleUI.CYAN + "IN PROGRESS" + ConsoleUI.RESET)
        };
        ConsoleUI.drawBox("GAMIFICATION DASHBOARD", info, ConsoleUI.PURPLE);

        // Books read list
        System.out.println(ConsoleUI.BOLD + ConsoleUI.YELLOW + "\n📖 Completed Books Checklist:" + ConsoleUI.RESET);
        List<String> readList = service.getBooksReadList(m.getId());
        if (readList.isEmpty()) {
            System.out.println("  No books read yet. Start borrowing!");
        } else {
            for (String title : readList) {
                System.out.println("  ✓ " + title);
            }
        }
        ConsoleUI.pause();
    }

    private static void memberLeaderboard() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("🏆 MONTHLY LEADERBOARD");

        List<Member> leaderboard = service.getMembers().stream()
            .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
            .collect(Collectors.toList());

        String[] headers = {"Rank", "Reader Name", "Points Earned", "Level Rank"};
        List<String[]> rows = new ArrayList<>();
        int rank = 1;
        for (Member m : leaderboard) {
            rows.add(new String[]{
                String.valueOf(rank),
                m.getName(),
                m.getPoints() + " pts",
                m.getRank()
            });
            rank++;
        }

        ConsoleUI.drawTable(headers, rows);
        ConsoleUI.pause();
    }

    private static void memberReserve(Member m) {
        ConsoleUI.printSubHeader("RESERVE A BOOK");
        printAllBooks();
        System.out.println();
        String id = ConsoleUI.promptString("Book ID to reserve", true);
        String res = service.reserveBook(m.getId(), id);
        if (res.equals("SUCCESS")) {
            ConsoleUI.printSuccess("Book reserved successfully!");
        } else {
            ConsoleUI.printError(res);
        }
        ConsoleUI.pause();
    }

    private static void memberPremium(Member m) {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("⭐ PREMIUM MEMBERSHIP DETAILS");
        
        System.out.println("Current Tier: " + (m.isPremium() ? ConsoleUI.BOLD + ConsoleUI.YELLOW + "PREMIUM (Expiry: " + m.getPremiumExpiryDate() + ")" + ConsoleUI.RESET : "REGULAR"));
        System.out.println("\nPremium Membership Benefits:");
        System.out.println("1. Reduced rental charges (20% Off all rentals)");
        System.out.println("2. Higher borrowing limit (Up to 5 books concurrently, regular: 3)");
        System.out.println("3. Zero reservation fees (regular: Rs. 10 per reservation)");
        System.out.println("────────────────────────────────────────────────────");
        System.out.println("Premium Plans Available:");
        System.out.println("1. 1 Month Premium  - Rs. 99");
        System.out.println("2. 3 Months Premium - Rs. 249");
        System.out.println("3. Back");
        System.out.println("────────────────────────────────────────────────────");

        int plan = ConsoleUI.promptInt("Select Plan", 1, 3);
        if (plan == 3) return;

        int months = (plan == 1) ? 1 : 3;
        String res = service.purchasePremium(m.getId(), months);
        if (res.equals("SUCCESS")) {
            ConsoleUI.printSuccess("Congratulations! You are now a Premium Member.");
        } else {
            ConsoleUI.printError(res);
        }
        ConsoleUI.pause();
    }
}
