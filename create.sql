-- -----------------------------------------------------
-- Database Setup
-- -----------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS homy_db;
CREATE DATABASE homy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE homy_db;
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------
-- Table: rooms
-- -----------------------------------------------------
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT,
    room_code VARCHAR(50) NOT NULL UNIQUE,          -- Κωδικός δωματίου ("Enter room id...")
    room_flat_name VARCHAR(150) NULL,               -- Το όνομα του διαμερίσματος (flatName)
    max_roommates INT NOT NULL DEFAULT 1,           -- Φίλτρο μέγιστου αριθμού συγκατοίκων
    rent_value DECIMAL(10, 2) NOT NULL DEFAULT 0.00,-- Φίλτρο ενοικίου (Min/Max Rent)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,                 -- Username (π.χ. "makis99")
    display_name VARCHAR(150) NULL,                 -- Εμφανιζόμενο όνομα (name, π.χ. "Makis Kosta")
    email VARCHAR(150) NOT NULL UNIQUE,             -- Email για το LogInPanel
    password_hash VARCHAR(255) NOT NULL,            -- Password για το LogInPanel
    bio TEXT NULL,                                  -- Βιογραφικό χρήστη (bio)
    preferences TEXT NULL,                          -- Προτιμήσεις συγκατοίκησης (preferences)
    room_id INT NULL,                               -- Το δωμάτιο που έκανε JOIN ο χρήστης
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: room_requests
-- Αιτήματα χρηστών για είσοδο σε δωμάτιο (Incoming Requests)
-- -----------------------------------------------------
CREATE TABLE room_requests (
    request_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Το δωμάτιο στο οποίο θέλει να μπει ο χρήστης
    sender_id INT NOT NULL,                         -- Ο χρήστης που στέλνει το αίτημα συγκατοίκησης
    request_time VARCHAR(50) NOT NULL,              -- Ώρα/Ημερομηνία αιτήματος (time String)
    justification TEXT NULL,                        -- Προαιρετική αιτιολόγηση (αποδοχής/απόρριψης)
    request_status ENUM('PENDING', 'ACCEPTED', 'DECLINED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (request_id),
    CONSTRAINT fk_requests_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_requests_users
        FOREIGN KEY (sender_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: bills
-- -----------------------------------------------------
CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο ανήκει ο λογαριασμός
    bill_type VARCHAR(100) NOT NULL,                -- Τύπος λογαριασμού (π.χ. Electricity, Internet)
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,    -- Ποσό λογαριασμού
    bill_date DATE NOT NULL,                        -- Ημερομηνία (yyyy-MM-dd)
    payers TEXT NOT NULL,                           -- Ποιοι πληρώνουν (π.χ. "Giorgos, Alex")
    bill_status ENUM('Pending', 'Paid') NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bill_id),
    CONSTRAINT fk_bills_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: issues
-- -----------------------------------------------------
CREATE TABLE issues (
    issue_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο αφορά το τεχνικό πρόβλημα
    issue_type VARCHAR(150) NOT NULL,               -- Τύπος/Κατηγορία βλάβης (π.χ. Plumbing)
    reported_by VARCHAR(100) NOT NULL,              -- Όνομα του χρήστη που το δήλωσε
    payers TEXT NOT NULL,                           -- Ποιοι καλύπτουν το κόστος (π.χ. "All Roommates")
    issue_date DATE NOT NULL,                       -- Ημερομηνία καταγραφής ή επίλυσης
    issue_status ENUM('Pending', 'Resolved') NOT NULL DEFAULT 'Pending',
    scheduled_time TIME NULL,                       -- Προαιρετικό: Time window για το ραντεβού με τεχνικό
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (issue_id),
    CONSTRAINT fk_issues_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: chores
-- -----------------------------------------------------
CREATE TABLE chores (
    chore_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο ανήκει η δουλειά
    chore_name VARCHAR(200) NOT NULL,               -- Όνομα δουλειάς (π.χ. Σκούπισμα σαλονιού)
    points INT NOT NULL DEFAULT 0,                  -- Πόντοι ανταμοιβής (Reward Points)
    assignee VARCHAR(100) NOT NULL,                 -- Όνομα του υπεύθυνου συγκατοίκου
    chore_status ENUM('Pending', 'Completed', 'Suspended') NOT NULL DEFAULT 'Pending',
    approve_votes INT NOT NULL DEFAULT 0,           -- Ψήφοι έγκρισης ολοκλήρωσης
    reject_votes INT NOT NULL DEFAULT 0,            -- Ψήφοι απόρριψης ολοκλήρωσης
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chore_id),
    CONSTRAINT fk_chores_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: chore_reports
-- -----------------------------------------------------
CREATE TABLE chore_reports (
    report_id INT AUTO_INCREMENT,
    chore_id INT NOT NULL,                          -- Σε ποια δουλειά αναφέρεται
    title VARCHAR(200) NULL,                        -- Τίτλος αναφοράς
    description TEXT NOT NULL,                      -- Περιγραφή του τι πήγε λάθος
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id),
    CONSTRAINT fk_reports_chores
        FOREIGN KEY (chore_id)
        REFERENCES chores (chore_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: user_points
-- -----------------------------------------------------
CREATE TABLE user_points (
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    current_balance INT NOT NULL DEFAULT 0,         -- Τρέχοντες διαθέσιμοι πόντοι συγκατοίκου
    PRIMARY KEY (user_id),
    CONSTRAINT fk_points_users
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_points_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: rewards
-- -----------------------------------------------------
CREATE TABLE rewards (
    reward_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Ανταμοιβές ανά δωμάτιο/σπίτι
    reward_name VARCHAR(150) NOT NULL,              -- Όνομα ανταμοιβής
    cost INT NOT NULL DEFAULT 0,                    -- Κόστος σε πόντους
    is_available BOOLEAN NOT NULL DEFAULT TRUE,     -- Διαθεσιμότητα ανταμοιβής
    ui_color VARCHAR(20) NULL,                      -- Χρώμα για το UI αναπαράστασης
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (reward_id),
    CONSTRAINT fk_rewards_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: user_redeemed_rewards
-- -----------------------------------------------------
CREATE TABLE user_redeemed_rewards (
    redeem_id INT AUTO_INCREMENT,
    user_id INT NOT NULL,                           -- Ποιος χρήστης αγόρασε το πάσο
    reward_id INT NOT NULL,                         -- Ποιο πάσο/reward αγόρασε
    redeemed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (redeem_id),
    CONSTRAINT fk_redeemed_users
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_redeemed_rewards
        FOREIGN KEY (reward_id)
        REFERENCES rewards (reward_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: applications
-- -----------------------------------------------------
CREATE TABLE applications (
    application_id INT AUTO_INCREMENT,
    user_id INT NOT NULL,                           -- Ο χρήστης που δημιούργησε την αγγελία
    title VARCHAR(200) NOT NULL,                    -- Τίτλος (π.χ. Flat Κυψέλη)
    location VARCHAR(150) NOT NULL,                 -- Περιοχή
    house_address VARCHAR(255) NOT NULL,            -- Διεύθυνση
    rent DECIMAL(10, 2) NOT NULL DEFAULT 0.00,      -- Μηνιαίο ενοίκιο
    roommates_wanted INT NOT NULL DEFAULT 1,        -- Αριθμός συγκατοίκων που αναζητούνται
    description TEXT NULL,                          -- Περιγραφή αγγελίας
    application_status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (application_id),
    CONSTRAINT fk_applications_users
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: shopping_list
-- -----------------------------------------------------
CREATE TABLE shopping_list (
    item_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο ανήκει η λίστα αγορών
    item_name VARCHAR(150) NOT NULL,                -- Όνομα προϊόντος
    quantity INT NOT NULL DEFAULT 1,                -- Ποσότητα προϊόντος
    is_checked BOOLEAN NOT NULL DEFAULT FALSE,      -- Κατάσταση (FALSE: Main List, TRUE: Checked)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    CONSTRAINT fk_shopping_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: allocations
-- -----------------------------------------------------
CREATE TABLE allocations (
    allocation_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο ανήκει η δαπάνη
    allocation_date DATE NOT NULL,                  -- Ημερομηνία της απόδειξης/επιμερισμού
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Συνολικό ποσό απόδειξης
    receiver_username VARCHAR(100) NOT NULL,        -- Ο συγκατοίκος που πλήρωσε (receiver)
    image_path VARCHAR(255) NULL,                   -- Διαδρομή αρχείου της απόδειξης στο σύστημα
    is_done BOOLEAN NOT NULL DEFAULT FALSE,         -- Κατάσταση (Done ή Εκκρεμές)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (allocation_id),
    CONSTRAINT fk_allocations_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: allocation_shares
-- -----------------------------------------------------
CREATE TABLE allocation_shares (
    share_id INT AUTO_INCREMENT,
    allocation_id INT NOT NULL,                     -- Σύνδεση με τη γενική εγγραφή της απόδειξης
    roommate_username VARCHAR(100) NOT NULL,        -- Όνομα του συγκατοίκου που επιμερίζεται το έξοδο
    amount_owed DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Το ποσό που αναλογεί στον συγκεκριμένο συγκατοίκο
    PRIMARY KEY (share_id),
    CONSTRAINT fk_shares_allocations
        FOREIGN KEY (allocation_id)
        REFERENCES allocations (allocation_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: calendar_events
-- -----------------------------------------------------
CREATE TABLE calendar_events (
    event_id INT AUTO_INCREMENT,
    room_id INT NOT NULL,                           -- Σε ποιο δωμάτιο/σπίτι ανήκει το ημερολόγιο
    event_name VARCHAR(200) NOT NULL,               -- Όνομα/Τίτλος συμβάντος
    event_description TEXT NULL,                    -- Περιγραφή συμβάντος
    event_date DATE NOT NULL,                       -- Πλήρης ημερομηνία
    event_time INT NOT NULL,                        -- Ώρα ως 4ψήφιος ακέραιος (π.χ. 1615)
    event_type ENUM('BILL', 'ISSUE', 'GENERAL') NOT NULL DEFAULT 'GENERAL',
    is_accepted INT NOT NULL DEFAULT 0,             -- Κατάσταση έγκρισης (0 = Εκκρεμότητα, 1 = Εγκρίθηκε)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    CONSTRAINT fk_events_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: notifications
-- -----------------------------------------------------
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT,
    user_id INT NOT NULL,                           -- Ο χρήστης που λαμβάνει την ειδοποίηση
    room_id INT NOT NULL,                           -- Το δωμάτιο/σπίτι στο οποίο αφορά η ειδοποίηση
    category VARCHAR(50) NOT NULL,                  -- Κατηγορία (π.χ. CHORES, BILLS)
    notification_text VARCHAR(255) NOT NULL,         -- Σύντομη περιγραφή / Τίτλος
    detail TEXT NOT NULL,                           -- Αναλυτικές πληροφορίες
    target_screen VARCHAR(100) NOT NULL,            -- Η οθόνη προορισμού (target)
    tag_color VARCHAR(20) NOT NULL,                 -- Χρώμα ετικέτας στο UI
    is_read BOOLEAN NOT NULL DEFAULT FALSE,         -- Κατάσταση ανάγνωσης
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_users
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_notifications_rooms
        FOREIGN KEY (room_id)
        REFERENCES rooms (room_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

INSERT INTO rooms (room_id, room_code, room_flat_name, max_roommates, rent_value) 
VALUES (1, 'TEST1234', 'Δοκιμαστικό Διαμέρισμα', 3, 350.00);