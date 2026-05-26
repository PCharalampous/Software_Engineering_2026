package entities;

public class Reward {
    private int rewardId;
    private int userId; // <-- ΠΡΟΣΘΗΚΗ: Το ID του χρήστη που έκανε το proposal
    private String name;
    private int cost;
    private boolean available;
    private String color;
    private int approveVotes = 0;
    private int rejectVotes = 0;

    // Ενημερωμένος Constructor που δέχεται και το userId
    public Reward(int rewardId, int userId, String name, int cost, boolean available, String color, int approveVotes, int rejectVotes) {
        this.rewardId = rewardId;
        this.userId = userId; // <-- Αποθήκευση
        this.name = name;
        this.cost = cost;
        this.available = available;
        this.color = color;
        this.approveVotes = approveVotes;
        this.rejectVotes = rejectVotes;
    }

    // --- GETTERS & SETTERS ---
    public int getRewardId() { return rewardId; }
    public void setRewardId(int id) { this.rewardId = id; }
    
    public int getUserId() { return userId; } // <-- ΠΡΟΣΘΗΚΗ: Getter για τον έλεγχο του Creator
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public int getCost() { return cost; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getColor() { return color != null ? color : "#E2E8F0"; }
    public int getApproveVotes() { return approveVotes; }
    public int getRejectVotes() { return rejectVotes; }
}