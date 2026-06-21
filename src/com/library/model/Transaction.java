package com.library.model;

public class Transaction {
    private String date; // yyyy-MM-dd
    private String memberId;
    private String memberName;
    private String type; // BORROW, RETURN, PURCHASE, MEMBERSHIP, DEPOSIT, FINE
    private String bookTitle;
    private String detail; // e.g. "Bhumi issued Java Programming"
    private double cost;

    public Transaction(String date, String memberId, String memberName, String type, String bookTitle, String detail, double cost) {
        this.date = date;
        this.memberId = memberId;
        this.memberName = memberName;
        this.type = type;
        this.bookTitle = bookTitle;
        this.detail = detail;
        this.cost = cost;
    }

    public Transaction() {
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    // Serialization helper
    public String serialize() {
        return String.join("||",
            date,
            memberId,
            memberName,
            type,
            bookTitle.isEmpty() ? "N/A" : bookTitle,
            detail,
            String.valueOf(cost)
        );
    }

    public static Transaction deserialize(String line) {
        String[] parts = line.split("\\|\\|");
        if (parts.length < 7) return null;

        Transaction tx = new Transaction();
        tx.setDate(parts[0]);
        tx.setMemberId(parts[1]);
        tx.setMemberName(parts[2]);
        tx.setType(parts[3]);
        tx.setBookTitle(parts[4].equals("N/A") ? "" : parts[4]);
        tx.setDetail(parts[5]);
        tx.setCost(Double.parseDouble(parts[6]));
        return tx;
    }
}
