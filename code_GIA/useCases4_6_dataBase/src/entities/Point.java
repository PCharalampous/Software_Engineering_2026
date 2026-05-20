package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DatabaseManager;

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

    // --- LIVE SQL OPERATIONS ---

    public int getBalance(String username) {
        String query = "SELECT current_balance FROM user_points up " +
                       "JOIN users u ON up.user_id = u.user_id WHERE u.username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("current_balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean checkPoints(String username, int cost) {
        return getBalance(username) >= cost;
    }

    public void pointDeduction(String username, int cost, int rewardId) {
        String updatePoints = "UPDATE user_points up JOIN users u ON up.user_id = u.user_id " +
                              "SET up.current_balance = up.current_balance - ? WHERE u.username = ?";
        String insertRedeem = "INSERT INTO user_redeemed_rewards (user_id, reward_id) " +
                              "VALUES ((SELECT user_id FROM users WHERE username = ? LIMIT 1), ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps1 = conn.prepareStatement(updatePoints)) {
                ps1.setInt(1, cost);
                ps1.setString(2, username);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(insertRedeem)) {
                ps2.setString(1, username);
                ps2.setInt(2, rewardId);
                ps2.executeUpdate();
            }
            
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}