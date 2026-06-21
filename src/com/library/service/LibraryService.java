package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Review;
import com.library.model.Transaction;
import com.library.util.Database;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {
    private List<Book> books;
    private List<Member> members;
    private List<Transaction> transactions;
    private List<Review> reviews;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LibraryService() {
        Database.initialize();
        this.books = Database.loadBooks();
        this.members = Database.loadMembers();
        this.transactions = Database.loadTransactions();
        this.reviews = Database.loadReviews();
    }

    public List<Book> getBooks() { return books; }
    public List<Member> getMembers() { return members; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<Review> getReviews() { return reviews; }

    // --- Authentication ---
    public Member authenticateMember(String username, String password) {
        for (Member m : members) {
            if (m.getUsername().equalsIgnoreCase(username) && m.getPassword().equals(password)) {
                updateStreak(m);
                return m;
            }
        }
        return null;
    }

    // --- Streak Logic ---
    private void updateStreak(Member m) {
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        if (m.getLastActiveDate().isEmpty()) {
            m.setReadingStreak(1);
            m.setLongestStreak(1);
        } else {
            try {
                LocalDate lastActive = LocalDate.parse(m.getLastActiveDate(), DATE_FORMATTER);
                LocalDate today = LocalDate.now();
                long diff = ChronoUnit.DAYS.between(lastActive, today);

                if (diff == 1) {
                    // Consecutive day login
                    m.setReadingStreak(m.getReadingStreak() + 1);
                    if (m.getReadingStreak() > m.getLongestStreak()) {
                        m.setLongestStreak(m.getReadingStreak());
                    }
                } else if (diff > 1) {
                    // Streak broken
                    m.setReadingStreak(1);
                }
                // If diff == 0, same day, streak doesn't change
            } catch (Exception e) {
                m.setReadingStreak(1);
            }
        }
        m.setLastActiveDate(todayStr);
        saveMembers();
    }

    // --- Member Actions ---
    public void registerMember(String name, String username, String password) {
        String id = "M" + (101 + members.size());
        Member m = new Member(id, name, username, password);
        members.add(m);
        saveMembers();
    }

    public void updateMember(String id, String name, String password) {
        Member m = findMemberById(id);
        if (m != null) {
            if (!name.isEmpty()) m.setName(name);
            if (!password.isEmpty()) m.setPassword(password);
            saveMembers();
        }
    }

    public boolean removeMember(String id) {
        Member m = findMemberById(id);
        if (m != null) {
            members.remove(m);
            saveMembers();
            return true;
        }
        return false;
    }

    public Member findMemberById(String id) {
        return members.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public Book findBookById(String id) {
        return books.stream().filter(b -> b.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    // --- Book Actions ---
    public void addBook(String title, String author, String category, double price) {
        String id = "B" + (101 + books.size());
        Book b = new Book(id, title, author, category, price);
        books.add(b);
        saveBooks();

        // Log transaction
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        Transaction t = new Transaction(dateStr, "SYSTEM", "Admin", "ADD_BOOK", title, "System added Book: " + title, 0.0);
        transactions.add(t);
        Database.appendTransaction(t);
    }

    public boolean updateBook(String id, String title, String author, String category, double price) {
        Book b = findBookById(id);
        if (b != null) {
            if (!title.isEmpty()) b.setTitle(title);
            if (!author.isEmpty()) b.setAuthor(author);
            if (!category.isEmpty()) b.setCategory(category);
            if (price > 0) b.setPrice(price);
            saveBooks();
            return true;
        }
        return false;
    }

    public boolean deleteBook(String id) {
        Book b = findBookById(id);
        if (b != null) {
            books.remove(b);
            saveBooks();
            
            // Log transaction
            String dateStr = LocalDate.now().format(DATE_FORMATTER);
            Transaction t = new Transaction(dateStr, "SYSTEM", "Admin", "DELETE_BOOK", b.getTitle(), "System deleted Book: " + b.getTitle(), 0.0);
            transactions.add(t);
            Database.appendTransaction(t);
            return true;
        }
        return false;
    }

    // --- Smart Search ---
    public List<Book> smartSearch(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;

        return books.stream().filter(b -> 
            b.getId().toLowerCase().contains(q) ||
            b.getTitle().toLowerCase().contains(q) ||
            b.getAuthor().toLowerCase().contains(q) ||
            b.getCategory().toLowerCase().contains(q)
        ).collect(Collectors.toList());
    }

    public List<Book> searchById(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;
        return books.stream().filter(b -> b.getId().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<Book> searchByTitle(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;
        return books.stream().filter(b -> b.getTitle().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<Book> searchByAuthor(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;
        return books.stream().filter(b -> b.getAuthor().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public List<Book> searchByCategory(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) return books;
        return books.stream().filter(b -> b.getCategory().toLowerCase().contains(q)).collect(Collectors.toList());
    }

    // --- Borrow (Rent) Book ---
    public String borrowBook(String memberId, String bookId, int durationDays) {
        Member m = findMemberById(memberId);
        Book b = findBookById(bookId);

        if (m == null) return "Member not found.";
        if (b == null) return "Book not found.";

        // Checking limits
        int maxLimit = m.isPremium() ? 5 : 3;
        long currentBorrows = books.stream().filter(bk -> bk.getIssuedTo().equals(m.getId()) && bk.getStatus().equals("Issued")).count();
        if (currentBorrows >= maxLimit) {
            return "Borrow limit reached. (" + maxLimit + " books max).";
        }

        // Check availability
        if (b.getStatus().equals("Issued")) {
            return "ALREADY_ISSUED";
        }
        if (b.getStatus().equals("Reserved")) {
            // Can only borrow if they are first in queue
            if (b.getReservationQueue().isEmpty() || !b.getReservationQueue().get(0).equals(m.getId())) {
                return "This book is reserved by another member.";
            }
        }

        // Calculate Cost
        double cost = 0.0;
        if (durationDays == 7) cost = 20;
        else if (durationDays == 15) cost = 35;
        else if (durationDays == 30) cost = 60;

        if (m.isPremium()) {
            cost = cost * 0.8; // 20% discount
        }

        // Check Wallet
        if (m.getWalletBalance() < cost) {
            return "Insufficient wallet balance. Price: Rs. " + cost + ", Balance: Rs. " + m.getWalletBalance();
        }

        // Process borrow
        m.deductMoney(cost);
        b.setStatus("Issued");
        b.setIssuedTo(m.getId());
        b.incrementIssueCount();
        b.incrementRentCount();

        LocalDate today = LocalDate.now();
        b.setIssueDate(today.format(DATE_FORMATTER));
        b.setDueDate(today.plusDays(durationDays).format(DATE_FORMATTER));

        // If it was reserved by this user, remove from queue
        if (!b.getReservationQueue().isEmpty() && b.getReservationQueue().get(0).equals(m.getId())) {
            b.getReservationQueue().remove(0);
        }

        saveBooks();
        saveMembers();

        // Transaction log
        Transaction t = new Transaction(
            today.format(DATE_FORMATTER), 
            m.getId(), 
            m.getName(), 
            "BORROW", 
            b.getTitle(), 
            m.getName() + " borrowed " + b.getTitle() + " for " + durationDays + " days", 
            cost
        );
        transactions.add(t);
        Database.appendTransaction(t);

        return "SUCCESS";
    }

    // --- Return Book ---
    public String returnBook(String bookId, String returnDateSimulated) {
        Book b = findBookById(bookId);
        if (b == null) return "Book not found.";
        if (!b.getStatus().equals("Issued")) return "This book is not currently issued.";

        Member m = findMemberById(b.getIssuedTo());
        if (m == null) return "Issued member records missing.";

        LocalDate due = LocalDate.parse(b.getDueDate(), DATE_FORMATTER);
        LocalDate retDate = returnDateSimulated.isEmpty() ? LocalDate.now() : LocalDate.parse(returnDateSimulated, DATE_FORMATTER);

        long daysLate = ChronoUnit.DAYS.between(due, retDate);
        double fine = 0.0;
        if (daysLate > 0) {
            fine = daysLate * 5;
        }

        // Process fine
        if (fine > 0) {
            m.deductMoney(fine); // wallet balance can go negative
            
            // Record fine transaction
            Transaction fineTx = new Transaction(
                retDate.format(DATE_FORMATTER),
                m.getId(),
                m.getName(),
                "FINE",
                b.getTitle(),
                m.getName() + " paid fine of Rs. " + fine + " for late return of " + b.getTitle(),
                fine
            );
            transactions.add(fineTx);
            Database.appendTransaction(fineTx);
        }

        // Book returns
        b.setStatus("Available");
        b.setIssuedTo("");
        b.setIssueDate("");
        b.setDueDate("");

        // If there are reservations, set status to Reserved and keep the queue intact
        if (!b.getReservationQueue().isEmpty()) {
            b.setStatus("Reserved");
        }

        // Reward points
        m.addPoints(50);
        m.incrementChallengeProgress();
        
        saveBooks();
        saveMembers();

        // Return transaction
        Transaction t = new Transaction(
            retDate.format(DATE_FORMATTER),
            m.getId(),
            m.getName(),
            "RETURN",
            b.getTitle(),
            m.getName() + " returned " + b.getTitle(),
            0.0
        );
        transactions.add(t);
        Database.appendTransaction(t);

        if (fine > 0) {
            return "SUCCESS_WITH_FINE||" + fine + "||" + daysLate;
        }
        return "SUCCESS";
    }

    // --- Buy Book ---
    public String buyBook(String memberId, String bookId) {
        Member m = findMemberById(memberId);
        Book b = findBookById(bookId);

        if (m == null) return "Member not found.";
        if (b == null) return "Book not found.";

        if (!b.getStatus().equals("Available")) {
            return "Book is not available for purchase (Status: " + b.getStatus() + ").";
        }

        double price = b.getPrice();
        if (m.getWalletBalance() < price) {
            return "Insufficient wallet balance. Price: Rs. " + price + ", Balance: Rs. " + m.getWalletBalance();
        }

        // Process Purchase
        m.deductMoney(price);
        b.setStatus("Bought");
        b.setIssuedTo(m.getId());
        b.incrementPurchaseCount();

        saveBooks();
        saveMembers();

        // Transaction
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        Transaction t = new Transaction(
            todayStr,
            m.getId(),
            m.getName(),
            "PURCHASE",
            b.getTitle(),
            m.getName() + " purchased " + b.getTitle() + " permanently",
            price
        );
        transactions.add(t);
        Database.appendTransaction(t);

        return "SUCCESS";
    }

    // --- Book Reservation ---
    public String reserveBook(String memberId, String bookId) {
        Member m = findMemberById(memberId);
        Book b = findBookById(bookId);

        if (m == null) return "Member not found.";
        if (b == null) return "Book not found.";

        if (b.getStatus().equals("Available")) {
            return "Book is available. You can borrow it directly.";
        }

        if (b.getReservationQueue().contains(m.getId())) {
            return "You have already reserved this book.";
        }

        // Reservation fee: Premium is free, Regular is Rs. 10
        double fee = m.isPremium() ? 0.0 : 10.0;
        if (fee > 0 && m.getWalletBalance() < fee) {
            return "Insufficient balance to pay the Rs. 10 reservation fee.";
        }

        if (fee > 0) {
            m.deductMoney(fee);
        }

        b.getReservationQueue().add(m.getId());
        if (b.getStatus().equals("Available")) {
            b.setStatus("Reserved");
        }
        
        saveBooks();
        saveMembers();

        // Transaction
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        Transaction t = new Transaction(
            todayStr,
            m.getId(),
            m.getName(),
            "RESERVE",
            b.getTitle(),
            m.getName() + " reserved " + b.getTitle() + " (Fee Paid: Rs. " + fee + ")",
            fee
        );
        transactions.add(t);
        Database.appendTransaction(t);

        return "SUCCESS";
    }

    // --- Buy Premium Membership ---
    public String purchasePremium(String memberId, int months) {
        Member m = findMemberById(memberId);
        if (m == null) return "Member not found.";

        double cost = (months == 1) ? 99 : 249;
        if (m.getWalletBalance() < cost) {
            return "Insufficient wallet balance. Cost: Rs. " + cost + ", Balance: Rs. " + m.getWalletBalance();
        }

        m.deductMoney(cost);
        m.setPremium(true);
        
        LocalDate expiry = m.getPremiumExpiryDate().isEmpty() ? LocalDate.now() : LocalDate.parse(m.getPremiumExpiryDate(), DATE_FORMATTER);
        if (expiry.isBefore(LocalDate.now())) {
            expiry = LocalDate.now();
        }
        m.setPremiumExpiryDate(expiry.plusMonths(months).format(DATE_FORMATTER));

        saveMembers();

        // Transaction
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        Transaction t = new Transaction(
            todayStr,
            m.getId(),
            m.getName(),
            "MEMBERSHIP",
            "Premium Sub " + months + "m",
            m.getName() + " purchased Premium Membership for " + months + " months",
            cost
        );
        transactions.add(t);
        Database.appendTransaction(t);

        return "SUCCESS";
    }

    // --- Collect Fine (Librarian manual operation) ---
    public boolean collectFine(String memberId, double amountPaid) {
        Member m = findMemberById(memberId);
        if (m == null) return false;
        
        m.addMoney(amountPaid); // credit wallet to cover negative balance/fine
        saveMembers();

        // Log manual fine collection
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        Transaction t = new Transaction(
            todayStr,
            m.getId(),
            m.getName(),
            "DEPOSIT",
            "Fine Collected",
            "Librarian collected cash payment of Rs. " + amountPaid + " to settle fines",
            amountPaid
        );
        transactions.add(t);
        Database.appendTransaction(t);
        return true;
    }

    // --- Deposit Cash to Member Wallet ---
    public void depositWallet(String memberId, double amount) {
        Member m = findMemberById(memberId);
        if (m != null) {
            m.addMoney(amount);
            saveMembers();

            String todayStr = LocalDate.now().format(DATE_FORMATTER);
            Transaction t = new Transaction(
                todayStr,
                m.getId(),
                m.getName(),
                "DEPOSIT",
                "Wallet Load",
                m.getName() + " added Rs. " + amount + " to wallet",
                amount
            );
            transactions.add(t);
            Database.appendTransaction(t);
        }
    }

    // --- Add Reviews ---
    public void addReview(String bookId, String memberName, int stars, String comment) {
        Book b = findBookById(bookId);
        if (b != null) {
            String todayStr = LocalDate.now().format(DATE_FORMATTER);
            Review r = new Review(bookId, b.getTitle(), memberName, stars, comment, todayStr);
            reviews.add(r);
            Database.appendReview(r);
        }
    }

    // --- AI-Inspired Recommendation Engine ---
    public List<Book> getRecommendations(String memberId) {
        Member m = findMemberById(memberId);
        if (m == null) return new ArrayList<>();

        // Find books this user has ever borrowed or purchased
        Set<String> userBookIds = transactions.stream()
            .filter(t -> t.getMemberId().equals(memberId) && (t.getType().equals("BORROW") || t.getType().equals("PURCHASE")))
            .map(Transaction::getBookTitle)
            .flatMap(title -> books.stream().filter(b -> b.getTitle().equalsIgnoreCase(title)).map(Book::getId))
            .collect(Collectors.toSet());

        if (userBookIds.isEmpty()) {
            // No history. Recommend top-issued popular books
            return books.stream()
                .filter(b -> b.getStatus().equals("Available"))
                .sorted((a, b) -> Integer.compare(b.getIssueCount(), a.getIssueCount()))
                .limit(4)
                .collect(Collectors.toList());
        }

        // Recommend:
        // 1. Same category books the user has not read yet
        // 2. Same author books
        // 3. Co-borrower filter ("People who read this also read")
        
        Set<String> recommendedIds = new LinkedHashSet<>();
        
        // Fetch categories and authors of read books
        Set<String> readCategories = new HashSet<>();
        Set<String> readAuthors = new HashSet<>();
        for (String bid : userBookIds) {
            Book b = findBookById(bid);
            if (b != null) {
                readCategories.add(b.getCategory());
                readAuthors.add(b.getAuthor());
            }
        }

        // 3. Co-borrower: Find other users who read any of these books
        Set<String> coBorrowerMemberIds = transactions.stream()
            .filter(t -> (t.getType().equals("BORROW") || t.getType().equals("PURCHASE")) && !t.getMemberId().equals(memberId))
            .filter(t -> {
                String title = t.getBookTitle();
                return books.stream().anyMatch(b -> b.getTitle().equalsIgnoreCase(title) && userBookIds.contains(b.getId()));
            })
            .map(Transaction::getMemberId)
            .collect(Collectors.toSet());

        // Find what books those users borrowed that this user hasn't read
        if (!coBorrowerMemberIds.isEmpty()) {
            List<String> coReadTitles = transactions.stream()
                .filter(t -> coBorrowerMemberIds.contains(t.getMemberId()) && (t.getType().equals("BORROW") || t.getType().equals("PURCHASE")))
                .map(Transaction::getBookTitle)
                .collect(Collectors.toList());

            for (String title : coReadTitles) {
                books.stream()
                    .filter(b -> b.getTitle().equalsIgnoreCase(title) && !userBookIds.contains(b.getId()) && b.getStatus().equals("Available"))
                    .map(Book::getId)
                    .forEach(recommendedIds::add);
            }
        }

        // 1. Category-based recommendations
        for (Book b : books) {
            if (readCategories.contains(b.getCategory()) && !userBookIds.contains(b.getId()) && b.getStatus().equals("Available")) {
                recommendedIds.add(b.getId());
            }
        }

        // 2. Author-based recommendations
        for (Book b : books) {
            if (readAuthors.contains(b.getAuthor()) && !userBookIds.contains(b.getId()) && b.getStatus().equals("Available")) {
                recommendedIds.add(b.getId());
            }
        }

        // Map to books and return
        return recommendedIds.stream()
            .map(this::findBookById)
            .filter(Objects::nonNull)
            .limit(5)
            .collect(Collectors.toList());
    }

    // Get list of books borrowed by member (including due date, title, id, category, price)
    public List<Book> getActiveBorrows(String memberId) {
        return books.stream()
            .filter(b -> b.getIssuedTo().equals(memberId) && b.getStatus().equals("Issued"))
            .collect(Collectors.toList());
    }

    // Get list of books bought by member
    public List<Book> getPurchasedBooks(String memberId) {
        return books.stream()
            .filter(b -> b.getIssuedTo().equals(memberId) && b.getStatus().equals("Bought"))
            .collect(Collectors.toList());
    }

    // --- Reading History / Books Read List ---
    public List<String> getBooksReadList(String memberId) {
        // Returned books or purchased books
        return transactions.stream()
            .filter(t -> t.getMemberId().equals(memberId) && (t.getType().equals("RETURN") || t.getType().equals("PURCHASE")))
            .map(Transaction::getBookTitle)
            .distinct()
            .collect(Collectors.toList());
    }

    // --- Analytics Summary (Insights) ---
    public Map<String, String> getLibraryInsights() {
        Map<String, String> insights = new HashMap<>();
        
        insights.put("totalBooks", String.valueOf(books.size()));

        // Trending book: Book with highest issue count
        Book trending = books.stream().max(Comparator.comparingInt(Book::getIssueCount)).orElse(null);
        insights.put("trendingBook", trending != null ? trending.getTitle() : "None");

        // Most Active Member: Member with highest returns + purchases count in transactions
        Map<String, Long> activeMembers = transactions.stream()
            .filter(t -> t.getType().equals("RETURN") || t.getType().equals("PURCHASE"))
            .collect(Collectors.groupingBy(Transaction::getMemberName, Collectors.counting()));
        
        String activeMemberName = activeMembers.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        insights.put("mostActiveMember", activeMemberName);

        // Most Popular Category: Category of books with highest issues
        Map<String, Long> categoryCount = transactions.stream()
            .filter(t -> t.getType().equals("BORROW"))
            .map(t -> {
                Book b = books.stream().filter(bk -> bk.getTitle().equalsIgnoreCase(t.getBookTitle())).findFirst().orElse(null);
                return b != null ? b.getCategory() : "Unknown";
            })
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        String popularCategory = categoryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        insights.put("mostPopularCategory", popularCategory);

        // Growth count: count of borrows/purchases in the last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long growth = transactions.stream()
            .filter(t -> t.getType().equals("BORROW") || t.getType().equals("PURCHASE"))
            .filter(t -> {
                try {
                    LocalDate d = LocalDate.parse(t.getDate(), DATE_FORMATTER);
                    return d.isAfter(thirtyDaysAgo);
                } catch (Exception e) {
                    return false;
                }
            })
            .count();
        insights.put("growth", "+" + growth + " issues/purchases this month");

        return insights;
    }

    // --- Save helper calls ---
    public void saveBooks() {
        Database.saveBooks(books);
    }

    public void saveMembers() {
        Database.saveMembers(members);
    }
}
