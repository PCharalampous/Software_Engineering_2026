package entities;

public class Reward {
    private int rewardId;
    private String name;
    private int cost;
    private boolean available;
    private String color;
    
    // Πεδία ψηφοφορίας για την Εναλλακτική Ροή 4
    private int approveVotes = 0;
    private int rejectVotes = 0;

    public Reward(String name, int cost, boolean available, String color) {
        this.name = name;
        this.cost = cost;
        this.available = available;
        this.color = color;
    }

    public Reward(int rewardId, String name, int cost, boolean available, String color, int approveVotes, int rejectVotes) {
        this.rewardId = rewardId;
        this.name = name;
        this.cost = cost;
        this.available = available;
        this.color = color;
        this.approveVotes = approveVotes;
        this.rejectVotes = rejectVotes;
    }

    // Επιστρέφει true αν εγκριθεί από την πλειοψηφία
    public boolean vote(boolean approve, int totalMembers) {
        if (approve) approveVotes++;
        else rejectVotes++;
        
        int majority = (totalMembers / 2) + 1;
        if (approveVotes >= majority) {
            this.available = true;
            return true;
        }
        return false;
    }

    public int getRewardId() { return rewardId; }
    public void setRewardId(int id) { this.rewardId = id; }
    public String getName() { return name; }
    public int getCost() { return cost; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getColor() { return color != null ? color : "#E2E8F0"; }
    public int getApproveVotes() { return approveVotes; }
    public int getRejectVotes() { return rejectVotes; }
}