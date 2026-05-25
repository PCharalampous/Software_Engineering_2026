package entities;

import java.util.HashMap;
import java.util.Map;

public class Point {
    private String member;
    private int amount;
    private String choreName;

    public Point(String member, int amount, String choreName) {
        this.member = member;
        this.amount = amount;
        this.choreName = choreName;
    }

    public String getMember() { return member; }
    public int getAmount() { return amount; }
    public String getChoreName() { return choreName; }

    @Override
    public String toString() {
        return member + " earned " + amount + " points (" + choreName + ")";
    }

    private static final Map<String, Integer> balances = new HashMap<>();
    static {
        balances.put("Makis", 1000);
        balances.put("Giannis", 850);
        balances.put("Manos", 66600); 
    }

    public int getBalance(String user) {
        return balances.getOrDefault(user, 0);
    }

    public boolean checkPoints(String user, int cost) {
        return getBalance(user) >= cost;
    }

    public void pointDeduction(String user, int cost) {
        balances.put(user, getBalance(user) - cost);
    }
}