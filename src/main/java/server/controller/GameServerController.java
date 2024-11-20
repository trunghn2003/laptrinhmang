package server.controller;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.UIManager.getInt;

public class GameServerController {
    private final Connection con;

    public GameServerController(Connection con) {
        this.con = con;
    }

    public void updateScorePlayer(String username, int score1) {

        String query3 = "SELECT * FROM users WHERE username LIKE ?";
        int diem = 0;
        try (PreparedStatement stmt = con.prepareStatement(query3)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                     diem = rs.getInt("score");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(diem < score1 && score1 > 0) {
            score1 = Math.abs(diem - score1);
        }

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


//
//    public void addRound(int matchId, int roundNumber, String player1Choice, String player2Choice, int player1Score, int player2Score) {
//        String query = "INSERT INTO rounds (match_id, round_number, player1_choice, player2_choice, player1_score, player2_score) VALUES (?, ?, ?, ?, ?, ?)";
//        try (PreparedStatement stmt = con.prepareStatement(query)) {
//            stmt.setInt(1, matchId);
//            stmt.setInt(2, roundNumber);
//            stmt.setString(3, player1Choice);
//            stmt.setString(4, player2Choice);
//            stmt.setInt(5, player1Score);
//            stmt.setInt(6, player2Score);
//            stmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

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

    public void incrementWinCount(String username) {
        String query = "UPDATE users SET wins = wins + 1 WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            System.out.println("Win count updated for " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementLossCount(String username) {
        String query = "UPDATE users SET losses = losses + 1 WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            System.out.println("Loss count updated for " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementDrawCount(String username) {
        String query = "UPDATE users SET draws = draws + 1 WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            System.out.println("Draw count updated for " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Map<String, Object>> getMatchHistoryByUsername(String username) {
        Map<Integer, Map<String, Object>> matchHistory = new HashMap<>();
        String getUserIdQuery = "SELECT id FROM users WHERE username = ?";
        String getMatchesQuery = "SELECT * FROM matches WHERE player1_id = ? OR player2_id = ?";
        String getRoundsQuery = "SELECT * FROM rounds WHERE match_id = ? ORDER BY round_number";

        try {
            int userId = -1;

            // Lấy user_id của người chơi
            try (PreparedStatement stmt = con.prepareStatement(getUserIdQuery)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                } else {
                    System.out.println("User " + username + " not found.");
                    return matchHistory; // Trả về map rỗng nếu không tìm thấy người dùng
                }
            }

            // Lấy tất cả các trận đấu của người chơi
            try (PreparedStatement stmt = con.prepareStatement(getMatchesQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int matchId = rs.getInt("id");
                    int player1Id = rs.getInt("player1_id");
                    int player2Id = rs.getInt("player2_id");
                    int player1Score = rs.getInt("player1_score");
                    int player2Score = rs.getInt("player2_score");
                    String result = rs.getString("result");
                    Timestamp startTime = rs.getTimestamp("start_time");
                    Timestamp endTime = rs.getTimestamp("end_time");

                    // Tạo một bản ghi chứa thông tin của trận đấu
                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("matchId", matchId);
                    matchData.put("player1Id", player1Id);
                    matchData.put("player2Id", player2Id);
                    matchData.put("player1Score", player1Score);
                    matchData.put("player2Score", player2Score);
                    matchData.put("result", result);
                    matchData.put("startTime", startTime);
                    matchData.put("endTime", endTime);

                    // Lấy chi tiết các vòng đấu trong trận đấu này
//                    List<Map<String, Object>> rounds = new ArrayList<>();
//                    try (PreparedStatement roundStmt = con.prepareStatement(getRoundsQuery)) {
//                        roundStmt.setInt(1, matchId);
//                        ResultSet roundRs = roundStmt.executeQuery();
//                        while (roundRs.next()) {
//                            Map<String, Object> roundData = new HashMap<>();
//                            roundData.put("roundNumber", roundRs.getInt("round_number"));
//                            roundData.put("player1Choice", roundRs.getString("player1_choice"));
//                            roundData.put("player2Choice", roundRs.getString("player2_choice"));
//                            roundData.put("player1Score", roundRs.getInt("player1_score"));
//                            roundData.put("player2Score", roundRs.getInt("player2_score"));
//                            rounds.add(roundData);
//                        }
//                    }
//                    matchData.put("rounds", rounds); // Thêm thông tin vòng vào trận đấu

                    // Thêm trận đấu vào map với matchId làm key
                    matchHistory.put(matchId, matchData);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return matchHistory;
    }


}
