package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String id;
    private String title;
    private String author;
    private String category;
    private String status; // "Available", "Issued", "Reserved"
    private double price; // Purchase price
    private String issuedTo; // Member ID if issued
    private String dueDate; // yyyy-MM-dd
    private String issueDate; // yyyy-MM-dd
    private int issueCount; // total times checked out/borrowed
    private int rentCount; // total rental count
    private int purchaseCount; // total purchase count
    private List<String> reservationQueue; // Member IDs queue

    public Book(String id, String title, String author, String category, double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = "Available";
        this.price = price;
        this.issuedTo = "";
        this.dueDate = "";
        this.issueDate = "";
        this.issueCount = 0;
        this.rentCount = 0;
        this.purchaseCount = 0;
        this.reservationQueue = new ArrayList<>();
    }

    // Default constructor for loading
    public Book() {
        this.reservationQueue = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getIssuedTo() { return issuedTo; }
    public void setIssuedTo(String issuedTo) { this.issuedTo = issuedTo; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public int getIssueCount() { return issueCount; }
    public void setIssueCount(int issueCount) { this.issueCount = issueCount; }

    public int getRentCount() { return rentCount; }
    public void setRentCount(int rentCount) { this.rentCount = rentCount; }

    public int getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(int purchaseCount) { this.purchaseCount = purchaseCount; }

    public List<String> getReservationQueue() { return reservationQueue; }
    public void setReservationQueue(List<String> reservationQueue) { this.reservationQueue = reservationQueue; }

    public void incrementIssueCount() { this.issueCount++; }
    public void incrementRentCount() { this.rentCount++; }
    public void incrementPurchaseCount() { this.purchaseCount++; }

    // Serialization helper
    public String serialize() {
        String queueStr = String.join(",", reservationQueue);
        if (queueStr.isEmpty()) queueStr = "EMPTY";
        
        return String.join("||", 
            id, 
            title, 
            author, 
            category, 
            status, 
            String.valueOf(price), 
            issuedTo.isEmpty() ? "NONE" : issuedTo, 
            dueDate.isEmpty() ? "NONE" : dueDate, 
            issueDate.isEmpty() ? "NONE" : issueDate, 
            String.valueOf(issueCount), 
            String.valueOf(rentCount), 
            String.valueOf(purchaseCount), 
            queueStr
        );
    }

    public static Book deserialize(String line) {
        String[] parts = line.split("\\|\\|");
        if (parts.length < 13) return null;
        
        Book book = new Book();
        book.setId(parts[0]);
        book.setTitle(parts[1]);
        book.setAuthor(parts[2]);
        book.setCategory(parts[3]);
        book.setStatus(parts[4]);
        book.setPrice(Double.parseDouble(parts[5]));
        book.setIssuedTo(parts[6].equals("NONE") ? "" : parts[6]);
        book.setDueDate(parts[7].equals("NONE") ? "" : parts[7]);
        book.setIssueDate(parts[8].equals("NONE") ? "" : parts[8]);
        book.setIssueCount(Integer.parseInt(parts[9]));
        book.setRentCount(Integer.parseInt(parts[10]));
        book.setPurchaseCount(Integer.parseInt(parts[11]));
        
        String queueStr = parts[12];
        if (!queueStr.equals("EMPTY")) {
            String[] ids = queueStr.split(",");
            for (String qId : ids) {
                if (!qId.trim().isEmpty()) {
                    book.getReservationQueue().add(qId.trim());
                }
            }
        }
        return book;
    }
}
