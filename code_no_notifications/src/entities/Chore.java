package entities;

public class Chore {
    private int choreId; // Προσθήκη ID για τη βάση
    private String name;
    private int points;
    private String assignee;
    private String status; 
    private int approveVotes = 0;
    private int rejectVotes = 0;
    private final int totalMembers;

    // Constructor για νέες δουλειές (πριν μπουν στη βάση)
    public Chore(String name, int points, String assignee, int totalMembers) {
        this.name = name;
        this.points = points;
        this.assignee = assignee;
        this.totalMembers = totalMembers;
        this.status = "Pending";
    }

    // Constructor για δουλειές που έρχονται έτοιμες από τη βάση
    public Chore(int choreId, String name, int points, String assignee, String status, int approveVotes, int rejectVotes, int totalMembers) {
        this.choreId = choreId;
        this.name = name;
        this.points = points;
        this.assignee = assignee;
        this.status = status;
        this.approveVotes = approveVotes;
        this.rejectVotes = rejectVotes;
        this.totalMembers = totalMembers;
    }

    public boolean vote(boolean approve) {
        if (approve) approveVotes++;
        else rejectVotes++;
        
        int majority = (totalMembers / 2) + 1;
        
        if (approveVotes >= majority) {
            return true; 
        }
        if (rejectVotes >= majority) {
            this.status = "Pending"; 
            this.approveVotes = 0;
            this.rejectVotes = 0;
            return false;
        }
        return false;
    }

    public int getChoreId() { return choreId; }
    public void setChoreId(int choreId) { this.choreId = choreId; }
    public String getName() { return name; }
    public int getPoints() { return points; }
    public String getAssignee() { return assignee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getApproveVotes() { return approveVotes; }
    public int getRejectVotes() { return rejectVotes; }
}