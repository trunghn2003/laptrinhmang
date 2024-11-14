package server.controller;

import java.sql.*;

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

    public int createMatch(String player1Username, String player2Username) {
        String getUserIdQuery = "SELECT id FROM users WHERE username = ?";
        String insertMatchQuery = "INSERT INTO matches (player1_id, player2_id, result) VALUES (?, ?, 'IN_PROGRESS')";

        int player1Id = -1;
        int player2Id = -1;

        try {
            // Get player1's ID
            try (PreparedStatement stmt = con.prepareStatement(getUserIdQuery)) {
                stmt.setString(1, player1Username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    player1Id = rs.getInt("id");
                } else {
                    System.out.println("User " + player1Username + " not found.");
                    return -1;
                }
            }

            // Get player2's ID
            try (PreparedStatement stmt = con.prepareStatement(getUserIdQuery)) {
                stmt.setString(1, player2Username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    player2Id = rs.getInt("id");
                } else {
                    System.out.println("User " + player2Username + " not found.");
                    return -1;
                }
            }

            // Insert match if both user IDs were found
            try (PreparedStatement stmt = con.prepareStatement(insertMatchQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, player1Id);
                stmt.setInt(2, player2Id);
                stmt.executeUpdate();

                // Retrieve the auto-generated match ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return match ID for reference
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 if there was an error or no key was generated
    }



    public void addRound(int matchId, int roundNumber, String player1Choice, String player2Choice, int player1Score, int player2Score) {
        String query = "INSERT INTO rounds (match_id, round_number, player1_choice, player2_choice, player1_score, player2_score) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, matchId);
            stmt.setInt(2, roundNumber);
            stmt.setString(3, player1Choice);
            stmt.setString(4, player2Choice);
            stmt.setInt(5, player1Score);
            stmt.setInt(6, player2Score);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void finalizeMatch(int matchId, int player1Score, int player2Score, String result) {
        String query = "UPDATE matches SET player1_score = ?, player2_score = ?, result = ?, end_time = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, player1Score);
            stmt.setInt(2, player2Score);
            stmt.setString(3, result);
            stmt.setInt(4, matchId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
