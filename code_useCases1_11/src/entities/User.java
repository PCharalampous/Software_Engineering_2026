package entities;

public class User {
    private int id;
    private String username;
    private String email;
    private int roomId; // ← ΠΡΟΣΘΗΚΗ

    public User(int id, String username, String email, int roomId) { // ← ΕΝΗΜΕΡΩΣΗ
        this.id = id;
        this.username = username;
        this.email = email;
        this.roomId = roomId;
    }

    public int getId() { return this.id; }
    public String getUsername() { return this.username; }
    public String getEmail() { return this.email; }
    public int getRoomId() { return this.roomId; } // ← ΠΡΟΣΘΗΚΗ
}