public class Chore {
    private final String name;
    private final int points;
    private final String assignee;

    public Chore(String name, int points, String assignee) {
        this.name = name;
        this.points = points;
        this.assignee = assignee;
    }

    public String getName() { return name; }
    public int getPoints() { return points; }
    public String getAssignee() { return assignee; }
}