package entities;

public class Event {
    private int eventId;      
    private int date;         
    private int month;        
    private int year;         
    private int time;         
    private String name;      
    private String type;      
    private int isAccepted;   
    private String description; 
    private int createdBy;    

    // Constructor για νέα συμβάντα (πριν μπουν στη βάση)
    public Event(int date, int month, int year, int time, String name, String type, int isAccepted, String description, int createdBy) {
        this.date = date;
        this.month = month;
        this.year = year;
        this.time = time;
        this.name = name;
        this.type = type;
        this.isAccepted = isAccepted;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Constructor για συμβάντα που έρχονται έτοιμα από τη βάση με το ID τους
    public Event(int eventId, int date, int month, int year, int time, String name, String type, int isAccepted, String description, int createdBy) {
        this.eventId = eventId;
        this.date = date;
        this.month = month;
        this.year = year;
        this.time = time;
        this.name = name;
        this.type = type;
        this.isAccepted = isAccepted;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Επιστρέφει την ώρα σε μορφή HH:MM (π.χ. 1430 -> "14:30")
    public String getTimeFormatted() {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    public void update() {
        System.out.println("Event updated: " + name);
    }

    public void delete() {
        System.out.println("Event deleted: " + name);
    }

    // Getters και Setters
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

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

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
}