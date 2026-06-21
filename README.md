# 📚 Library Management System (CLI)

An interactive, high-fidelity command-line interface (CLI) application built using **Java** to manage library operations. This system employs robust **Object-Oriented Design (OOD)** patterns, persistent file storage, gamified reading achievements, personalized recommendation engines, and direct dependency-free PDF report writers.

---

## 🌟 Core Features

### 1. Multi-Role Interactive Dashboards
The application supports three roles, each equipped with visual menus and validation feedback:
*   **👨💼 Admin Dashboard**: Full CRUD book inventory management (Add, Update, Delete, View All Books), view active members, audit logs, inspect business intelligence metrics, and compile financial reports. Books are listed automatically prior to deletion or update prompts.
*   **👨🔧 Librarian Dashboard**: Oversee day-to-day books issuing/returning, advanced search, simulate return dates for testing fine periods, and settle pending member dues via cash payments.
*   **🎓 Member Dashboard**: Browse catalog listings, rent books under customizable duration plans, purchase books permanently, reload virtual wallet balances, rate/review books, track reading milestones, and upgrade to premium membership tiers. Books are listed automatically when selecting to rent, buy, or reserve.

### 2. Advanced & Smart Search Engine
Supports multiple search filters case-insensitively with partial matching, or viewing the entire catalog:
*   **Smart Search**: Global query matching on ID, Title, Author, or Category.
*   **Field Filtering**: Target searches by specific Book ID, Title, Author, or Category.
*   **Catalog View**: List all current books, their status (Available, Issued, Reserved, Bought), and pricing.
*   *Available*: Marked in green.
*   *Issued*: Marked in red, showing the borrower Member ID and calculated due dates.

### 3. Gamification & Reading Achievements
*   **Reader Level Ranks**: Earn points by returning borrowed books (+50 pts per book). Ranks scale from **Bronze Reader** and **Silver Reader** to **Gold Reader** and **Library Legend**.
*   **Reading Streak Tracker**: Tracks consecutive login days to log active and lifetime longest streaks.
*   **Monthly Reading Challenge**: Progress tracker presenting active goals (e.g. `7 / 10` books read).
*   **Monthly Leaderboard**: Ranks members by accumulated points.

### 4. Recommendation Engine
Uses multiple heuristics to recommend books:
*   **Same Category**: Finds unread books in categories matching the member's borrowing history.
*   **Same Author**: Recommends other works written by authors the member has previously read.
*   **"People Who Read This Also Read"**: Co-borrower filter checking what other titles were read by members who borrowed the same books.

### 5. Dependency-Free PDF Generators
The application contains a raw byte-stream assembler compiling compliant PDF objects (catalog tables, document pages, Helvetica standard fonts, and cross-reference tables) in pure Java. Generates:
*   `library_report.pdf`: Summarizes inventory status, members, and transaction history.
*   `financial_report.pdf`: Tallies revenue from rental fees, book sales, and premium memberships.
*   *Downloads Folder copy*: A copy of each PDF is automatically saved directly to the user's standard Downloads folder (`C:\Users\admin\Downloads\`) for immediate access.

### 6. Persistent File Storage
All database variables are written directly to `data/*.txt` using double-pipe separators (`||`), allowing safe modification, backup, and manual audit inspection.

---

## 📁 Project Structure

```
LibraryManagementSystem/
│
├── bin/                       # Compiled target .class binaries
├── data/                      # Persistent database files
│   ├── books.txt              # Book inventories
│   ├── members.txt            # Member profiles & streaks
│   ├── reviews.txt            # Book reviews
│   └── transactions.txt       # Unified transaction ledger
│
├── src/                       # Source package code
│   └── com/library/
│       ├── model/             # Domain data models (Book, Member, Transaction, Review)
│       ├── ui/                # ANSI framing & terminal boxes (ConsoleUI)
│       ├── util/              # Database load/saves & PDF generator utilities
│       ├── service/           # Business transactions & recommendation algorithms
│       └── Main.java          # Controller CLI entry loop
│
├── library_report.pdf         # Generated general audit report (PDF)
├── financial_report.pdf       # Generated revenue report (PDF)
└── run.bat                    # Windows launch script
```

---

## 🚀 How to Run the Project

### Option A: Double-Click (Windows)
Double-click the [run.bat](file:///c:/Users/admin/Downloads/LibraryManagementSystem/run.bat) script in the workspace root directory.

### Option B: PowerShell / CMD Terminal
Navigate to the project root directory and execute:
```powershell
# Compile all source files into bin/ target folder
javac -d bin src/com/library/model/*.java src/com/library/util/*.java src/com/library/ui/*.java src/com/library/service/*.java src/com/library/Main.java

# Run the Main class executable
java -cp bin com.library.Main
```

---

## 🔑 Pre-Seeded Accounts for Testing

The system automatically initializes default records on its first execution. Log in using the credentials below:

| Role | Username | Password | Notes / Seeded Attributes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin` | Full administrative privileges, revenue dashboards, and PDF exports. |
| **Librarian** | `lib` | `lib` | Daily check-outs, returns, simulated due dates, cash collections. |
| **Member** | `bhumi` | `password` | **Bhumi Singh** (150 pts, Silver Reader, active streak, ₹1000 wallet). |
| **Member** | `amit` | `password` | **Amit Patel** (350 pts, Gold Reader, ₹2000 wallet). |
| **Member** | `rahul` | `password` | **Rahul Sharma** (50 pts, Bronze Reader, ₹500 wallet). |
