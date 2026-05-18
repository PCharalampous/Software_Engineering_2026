package usecase9;

import java.util.ArrayList;
import java.util.List;

public class Calendar {
    private List<Event> events;

    public Calendar() {
        this.events = new ArrayList<>();
        
        // ΔΙΟΡΘΩΘΗΚΕ: Προσθήκη Μήνα (5) και Έτους (2026) στα dummy δεδομένα για να μην επαναλαμβάνονται
        events.add(new Event(5, 5, 2026, 2359, "Λογαριασμός Ρεύματος (ΔΕΗ)", "BILL", 0, ""));
        events.add(new Event(12, 5, 2026, 2359, "Κοινόχρηστα Μήνα", "BILL", 1, ""));
        events.add(new Event(15, 5, 2026, 1000, "Βλάβη στο Πλυντήριο", "ISSUE", 0, ""));
        events.add(new Event(20, 5, 2026, 1615, "Διαρροή Μπάνιου", "ISSUE", 1, ""));
    }

    public void update() {
        System.out.println("Calendar system state updated.");
    }

    public void delete() {
        System.out.println("Item removed from calendar state.");
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
    }
}