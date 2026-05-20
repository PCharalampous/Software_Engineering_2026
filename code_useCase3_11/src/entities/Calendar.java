package entities;

import java.util.ArrayList;
import java.util.List;

public class Calendar {
    private List<Event> events;

    public Calendar() {
        this.events = new ArrayList<>();
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