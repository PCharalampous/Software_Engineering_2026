import java.util.List;

public class ManageProfileClass {
    private UserProfile currentUser;
    private List<Request> incomingRequests;

    public ManageProfileClass(UserProfile user, List<Request> requests) {
        this.currentUser = user;
        this.incomingRequests = requests;
    }

    public UserProfile queryProfile() { return currentUser; }
    
    public boolean validateChanges(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public void save(String name, String bio, String prefs) {
        currentUser.name = name.trim();
        currentUser.bio = bio.trim();
        currentUser.preferences = prefs.trim();
    }

    public void choseACCEPT(Request req) { incomingRequests.remove(req); }
    public void choseDECLINE(Request req) { incomingRequests.remove(req); }
}