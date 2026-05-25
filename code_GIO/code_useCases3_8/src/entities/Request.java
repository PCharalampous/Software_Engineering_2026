package entities;
public class Request {
    public int request_id; // ← ΠΡΟΣΘΗΚΗ
    public String name, initials, time;

    public Request(int request_id, String n, String i, String t) {
        this.request_id = request_id; // ← ΠΡΟΣΘΗΚΗ
        this.name = n; this.initials = i; this.time = t;
    }
}