package server.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameServerController {
    private final Connection con;

    public GameServerController(Connection con) {
        this.con = con;
    }

    public void updateScorePlayer(String username, int score1) {
        String query = "UPDATE users SET score = score + ? WHERE username = ?";
        System.out.println(query);
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(score1));
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Score updated for " + username);
            } else {
                System.out.println("Failed to update score for " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateScorePlayer1(String username, int score1) {
        String query = "UPDATE users SET score =   ? WHERE username = ?";
        System.out.println(query);
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(score1));
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Score updated for " + username);
            } else {
                System.out.println("Failed to update score for " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
