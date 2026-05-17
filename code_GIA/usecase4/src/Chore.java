public class Chore {
    private String name;
    private int points;
    private String assignee;
    private String status; 
    private int approveVotes = 0;
    private int rejectVotes = 0;
    private final int totalMembers;

    public Chore(String name, int points, String assignee, int totalMembers) {
        this.name = name;
        this.points = points;
        this.assignee = assignee;
        this.totalMembers = totalMembers;
        this.status = "Pending";
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

    public boolean isMajorityReached() {
        int majority = (totalMembers / 2) + 1;
        return approveVotes >= majority || rejectVotes >= majority;
    }

    public String getName() { return name; }
    public int getPoints() { return points; }
    public String getAssignee() { return assignee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}