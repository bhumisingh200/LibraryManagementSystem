package com.library.model;

public class Review {
    private String bookId;
    private String bookTitle;
    private String memberName;
    private int stars; // 1 to 5
    private String comment;
    private String date; // yyyy-MM-dd

    public Review(String bookId, String bookTitle, String memberName, int stars, String comment, String date) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberName = memberName;
        this.stars = stars;
        this.comment = comment;
        this.date = date;
    }

    public Review() {
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStarsStr() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < stars) sb.append("★");
            else sb.append("☆");
        }
        return sb.toString();
    }

    // Serialization helper
    public String serialize() {
        return String.join("||",
            bookId,
            bookTitle,
            memberName,
            String.valueOf(stars),
            comment.replace("\n", " "), // keep single-line
            date
        );
    }

    public static Review deserialize(String line) {
        String[] parts = line.split("\\|\\|");
        if (parts.length < 6) return null;

        Review r = new Review();
        r.setBookId(parts[0]);
        r.setBookTitle(parts[1]);
        r.setMemberName(parts[2]);
        r.setStars(Integer.parseInt(parts[3]));
        r.setComment(parts[4]);
        r.setDate(parts[5]);
        return r;
    }
}
