package com.library.util;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Review;
import com.library.model.Transaction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DATA_DIR = "data";
    private static final String BOOKS_FILE = DATA_DIR + "/books.txt";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.txt";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.txt";
    private static final String REVIEWS_FILE = DATA_DIR + "/reviews.txt";

    public static void initialize() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            
            // Seed Books if file doesn't exist
            File bFile = new File(BOOKS_FILE);
            if (!bFile.exists() || bFile.length() == 0) {
                List<Book> defaultBooks = new ArrayList<>();
                defaultBooks.add(new Book("B101", "Java Complete Reference", "Herbert Schildt", "Programming", 599.0));
                defaultBooks.add(new Book("B102", "Clean Code", "Robert C. Martin", "Programming", 499.0));
                defaultBooks.add(new Book("B103", "Effective Java", "Joshua Bloch", "Programming", 699.0));
                defaultBooks.add(new Book("B104", "Head First Design Patterns", "Eric Freeman", "Programming", 799.0));
                defaultBooks.add(new Book("B105", "A Brief History of Time", "Stephen Hawking", "Science", 399.0));
                defaultBooks.add(new Book("B106", "Sapiens", "Yuval Noah Harari", "History", 450.0));
                defaultBooks.add(new Book("B107", "Atomic Habits", "James Clear", "Self-Help", 350.0));
                defaultBooks.add(new Book("B108", "Deep Work", "Cal Newport", "Self-Help", 399.0));
                defaultBooks.add(new Book("B109", "The Hobbit", "J.R.R. Tolkien", "Fiction", 299.0));
                saveBooks(defaultBooks);
            }

            // Seed Members if file doesn't exist
            File mFile = new File(MEMBERS_FILE);
            if (!mFile.exists() || mFile.length() == 0) {
                List<Member> defaultMembers = new ArrayList<>();
                
                Member bhumi = new Member("M101", "Bhumi Singh", "bhumi", "password");
                bhumi.addPoints(150); // Silver Reader
                bhumi.addMoney(1000);
                bhumi.setReadingStreak(15);
                bhumi.setLongestStreak(42);
                bhumi.setLastActiveDate("2026-06-20");
                bhumi.setChallengeGoal(10);
                bhumi.setChallengeProgress(7);
                defaultMembers.add(bhumi);

                Member rahul = new Member("M102", "Rahul Sharma", "rahul", "password");
                rahul.addPoints(50); // Bronze Reader
                rahul.addMoney(500);
                rahul.setReadingStreak(5);
                rahul.setLongestStreak(10);
                rahul.setLastActiveDate("2026-06-21");
                defaultMembers.add(rahul);

                Member amit = new Member("M103", "Amit Patel", "amit", "password");
                amit.addPoints(350); // Gold Reader
                amit.addMoney(2000);
                amit.setReadingStreak(25);
                amit.setLongestStreak(30);
                amit.setLastActiveDate("2026-06-19");
                defaultMembers.add(amit);

                saveMembers(defaultMembers);
            }

            // Seed initial Transaction and Reviews files if not present
            File tFile = new File(TRANSACTIONS_FILE);
            if (!tFile.exists()) {
                tFile.createNewFile();
            }

            File rFile = new File(REVIEWS_FILE);
            if (!rFile.exists()) {
                rFile.createNewFile();
                List<Review> defaultReviews = new ArrayList<>();
                defaultReviews.add(new Review("B102", "Clean Code", "Bhumi Singh", 5, "Excellent book for learning software craftsmanship.", "2026-06-15"));
                saveReviews(defaultReviews);
            }
        } catch (IOException e) {
            System.err.println("Error initializing database files: " + e.getMessage());
        }
    }

    // --- BOOKS ---
    public static synchronized List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Book book = Book.deserialize(line);
                if (book != null) {
                    books.add(book);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
        return books;
    }

    public static synchronized void saveBooks(List<Book> books) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                bw.write(book.serialize());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }

    // --- MEMBERS ---
    public static synchronized List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MEMBERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Member member = Member.deserialize(line);
                if (member != null) {
                    members.add(member);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading members: " + e.getMessage());
        }
        return members;
    }

    public static synchronized void saveMembers(List<Member> members) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : members) {
                bw.write(member.serialize());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving members: " + e.getMessage());
        }
    }

    // --- TRANSACTIONS ---
    public static synchronized List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Transaction tx = Transaction.deserialize(line);
                if (tx != null) {
                    transactions.add(tx);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        return transactions;
    }

    public static synchronized void saveTransactions(List<Transaction> transactions) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction tx : transactions) {
                bw.write(tx.serialize());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    public static synchronized void appendTransaction(Transaction tx) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            bw.write(tx.serialize());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending transaction: " + e.getMessage());
        }
    }

    // --- REVIEWS ---
    public static synchronized List<Review> loadReviews() {
        List<Review> reviews = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(REVIEWS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Review r = Review.deserialize(line);
                if (r != null) {
                    reviews.add(r);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading reviews: " + e.getMessage());
        }
        return reviews;
    }

    public static synchronized void saveReviews(List<Review> reviews) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REVIEWS_FILE))) {
            for (Review r : reviews) {
                bw.write(r.serialize());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving reviews: " + e.getMessage());
        }
    }

    public static synchronized void appendReview(Review r) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REVIEWS_FILE, true))) {
            bw.write(r.serialize());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending review: " + e.getMessage());
        }
    }
}
