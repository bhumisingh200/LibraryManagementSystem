package com.library.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Member {
    private String id;
    private String name;
    private String username;
    private String password;
    private double walletBalance;
    private int points;
    private int readingStreak;
    private int longestStreak;
    private String lastActiveDate; // yyyy-MM-dd
    private int challengeGoal; // monthly challenge target
    private int challengeProgress; // books read this month
    private boolean isPremium;
    private String premiumExpiryDate; // yyyy-MM-dd

    public Member(String id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.walletBalance = 0.0;
        this.points = 0;
        this.readingStreak = 0;
        this.longestStreak = 0;
        this.lastActiveDate = "";
        this.challengeGoal = 10; // Default monthly challenge
        this.challengeProgress = 0;
        this.isPremium = false;
        this.premiumExpiryDate = "";
    }

    public Member() {
    }

    // Dynamic Rank calculation
    public String getRank() {
        if (points < 100) return "Bronze Reader";
        if (points < 300) return "Silver Reader";
        if (points < 600) return "Gold Reader";
        return "Library Legend";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
    public void addMoney(double amount) { this.walletBalance += amount; }
    public boolean deductMoney(double amount) {
        if (this.walletBalance >= amount) {
            this.walletBalance -= amount;
            return true;
        }
        return false;
    }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public void addPoints(int pts) { this.points += pts; }

    public int getReadingStreak() { return readingStreak; }
    public void setReadingStreak(int readingStreak) { this.readingStreak = readingStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public String getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(String lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public int getChallengeGoal() { return challengeGoal; }
    public void setChallengeGoal(int challengeGoal) { this.challengeGoal = challengeGoal; }

    public int getChallengeProgress() { return challengeProgress; }
    public void setChallengeProgress(int challengeProgress) { this.challengeProgress = challengeProgress; }
    public void incrementChallengeProgress() { this.challengeProgress++; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public String getPremiumExpiryDate() { return premiumExpiryDate; }
    public void setPremiumExpiryDate(String premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

    // Serialization helper
    public String serialize() {
        return String.join("||",
            id,
            name,
            username,
            password,
            String.valueOf(walletBalance),
            String.valueOf(points),
            String.valueOf(readingStreak),
            String.valueOf(longestStreak),
            lastActiveDate.isEmpty() ? "NONE" : lastActiveDate,
            String.valueOf(challengeGoal),
            String.valueOf(challengeProgress),
            String.valueOf(isPremium),
            premiumExpiryDate.isEmpty() ? "NONE" : premiumExpiryDate
        );
    }

    public static Member deserialize(String line) {
        String[] parts = line.split("\\|\\|");
        if (parts.length < 13) return null;

        Member m = new Member();
        m.setId(parts[0]);
        m.setName(parts[1]);
        m.setUsername(parts[2]);
        m.setPassword(parts[3]);
        m.setWalletBalance(Double.parseDouble(parts[4]));
        m.setPoints(Integer.parseInt(parts[5]));
        m.setReadingStreak(Integer.parseInt(parts[6]));
        m.setLongestStreak(Integer.parseInt(parts[7]));
        m.setLastActiveDate(parts[8].equals("NONE") ? "" : parts[8]);
        m.setChallengeGoal(Integer.parseInt(parts[9]));
        m.setChallengeProgress(Integer.parseInt(parts[10]));
        m.setPremium(Boolean.parseBoolean(parts[11]));
        m.setPremiumExpiryDate(parts[12].equals("NONE") ? "" : parts[12]);
        return m;
    }
}
