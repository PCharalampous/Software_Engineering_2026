package entities;
public class UserProfile {
    public int user_id;
    public int room_id; 
    public String name, username, bio, preferences, flatName;
    public int points, members;

    public UserProfile(int user_id, String name, String username, String bio, String prefs, int points, String flatName, int members, int room_id) {
        this.user_id = user_id;
        this.room_id = room_id; // ← ΠΡΟΣΘΗΚΗ
        this.name = name;
        this.username = username;
        this.bio = bio;
        this.preferences = prefs;
        this.points = points;
        this.flatName = flatName;
        this.members = members;
    }
}