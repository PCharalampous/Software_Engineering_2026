public class Reward {
    public static void checkRewards(Chore chore) {
        Point newPoints = new Point(chore.getAssignee(), chore.getPoints(), chore.getName());
        System.out.println("Εκχωρήθηκαν " + newPoints.getAmount() + " πόντοι στον/στην " + chore.getAssignee());
    }

    private String name;
    private int cost;
    private boolean available;
    private String color;

    public Reward(String name, int cost, boolean available, String color) {
        this.name = name;
        this.cost = cost;
        this.available = available;
        this.color = color;
    }

    public String getName() { return name; }
    public int getCost() { return cost; }
    public String getColor() { return color; }
    public boolean checkRewardAvailability() { 
        return available; 
    }
    
    public void rewardUpdate() {
    }
}