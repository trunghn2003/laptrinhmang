package server.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameServerController {
    private final Connection con;

    public GameServerController(Connection con) {
        this.con = con;
    }

    public void updateScorePlayer(String username, int score) {
        String query = "UPDATE users SET score = ? WHERE username = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(score));
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
