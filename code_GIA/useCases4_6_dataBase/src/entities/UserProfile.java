package entities;
public class UserProfile {
    public String name, username, bio, preferences, flatName;
    public int points, members;

    public UserProfile(String name, String username, String bio, String prefs, int points, String flatName, int members) {
        this.name = name; this.username = username;
        this.bio = bio; this.preferences = prefs;
        this.points = points; this.flatName = flatName; this.members = members;
    }
}