<div align="center">
  <h1>HOMY</h1>
  <p>Software_Engineering_2026 - CEID University of Patras</p>
  <img width="400" height="400" alt="logoHOMY" src="https://github.com/user-attachments/assets/d4087a6f-b04b-451c-a9f8-5397dcf9a4ce" />
  <p>HOMY is a comprehensive roommate management platform designed to eliminate daily frictions and ensure a harmonious co-living experience.</p>
</div>

---

## Team Members
* ΔΗΜΗΤΡΙΟΥ-ΜΑΣΓΑΛΑΣ ΙΩΑΝΝΗΣ/ idimitrioumasgalas (ΑΜ: 1108382)
* ΕΥΘΥΜΙΟΥ ΓΕΩΡΓΙΟΣ/ gefth (ΑΜ: 1108319)
* ΚΑΝΑΚΗΣ ΣΠΥΡΙΔΩΝ/ animemios (ΑΜ: 1108318)
* ΜΠΑΛΑΤΣΟΥΡΑΣ ΟΔΥΣΣΕΑΣ-ΜΑΡΙΟΣ/ OdysseasBal (ΑΜ: 1112107)
* ΧΑΡΑΛΑΜΠΟΥΣ ΠΑΝΑΓΙΩΤΗΣ/ PCharalampous (ΑΜ: 1103475)

---

## Technologies
* Java Development Kit (JDK)
* JavaFX (GUI)
* MySQL (Application Database)
* Hosting Service For Database (clever.cloud)
* JDBC (Java Database Connectivity)

---

## Features
* Authentication & Account Management
* Room Search
* Room Management
* Expense Management & Bill Splitting
* Chores & Fair Distribution System
* In-App Rewards & Points System
* Shopping List & Bill Splitting
* Home Issue Reporting & Maintenance
* Shared Calendar
* Notifications System
* User Profiles & Application Management

---

## Supported Platforms
* Windows

---

## Running The Code

### Requirements
* Java 21 or higher ([Download Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html))
* JavaFX 21 or higher ([Download JavaFX 21](https://www.oracle.com/java/technologies/downloads/javafx/#javafx21))

### Installation

1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd <your-repository-folder>
2. **Add the database JAR connector:**
   ```bash
   Create a folder named lib in the root of your project directory if it does not exist yet.
   Place the mysql-connector-j-9.7.0.jar file directly inside that lib folder.
   ```
   If using IntelliJ:
   ```bash
   Open Project Structure (Ctrl+Alt+Shift+S), go to Libraries, click the + icon, select Java, then navigate and choose the mysql-connector-j-9.7.0.jar.
      ```
   If using Eclipse:
   ```bash
   Right-click project, go to Build Path, select Configure Build Path..., open the Libraries tab, click Classpath, then select Add JARs... and link the file.
     ```
   If using VS Code:
   ```bash
   Expand the Java Projects panel in the lower left sidebar, locate Referenced Libraries, click the + icon, then navigate and choose the mysql-connector-j-9.7.0.jar.

3. **Run the app:**

    Via IDE:
   ```bash
    Open the project folder, navigate to src/main/HOMYApp.java, right-click, and select Run.
      ```

    Via Terminal: Run the following command from the root folder:
   ```bash
    java --class-path "bin:lib/*" main.HOMYApp

## Running The Application

