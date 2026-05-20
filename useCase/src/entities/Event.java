package entities;

public class Event {
    private int date;         // Ημέρα του μήνα (1-31)
    private int month;        // ΔΙΟΡΘΩΘΗΚΕ: Προσθήκη συγκεκριμένου μήνα (1-12)
    private int year;         // ΔΙΟΡΘΩΘΗΚΕ: Προσθήκη συγκεκριμένου έτους (π.χ. 2026)
    private int time;         // Ώρα ως 4ψήφιος ακέραιος (π.χ., 2000 για 20:00)
    private String name;      // Όνομα/Τίτλος συμβάντος
    private String type;      // Τύπος: "BILL", "ISSUE", ή "GENERAL"
    private int isAccepted;   // 0 = Εκκρεμότητα, 1 = Εγκρίθηκε
    private String description; // Περιγραφή

    // Κατασκευαστής με πλήρη υποστήριξη ημερομηνίας (Ημέρα, Μήνας, Έτος)
    public Event(int date, int month, int year, int time, String name, String type, int isAccepted, String description) {
        this.date = date;
        this.month = month;
        this.year = year;
        this.time = time;
        this.name = name;
        this.type = type;
        this.isAccepted = isAccepted;
        this.description = description;
    }

    public void update() {
        System.out.println("Event updated: " + name);
    }

    public void delete() {
        System.out.println("Event deleted: " + name);
    }

    // Getters και Setters
    public int getDate() { return date; }
    public void setDate(int date) { this.date = date; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getIsAccepted() { return isAccepted; }
    public void setIsAccepted(int isAccepted) { this.isAccepted = isAccepted; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTimeFormatted() {
        String timeStr = String.format("%04d", time);
        return timeStr.substring(0, 2) + ":" + timeStr.substring(2);
    }
}