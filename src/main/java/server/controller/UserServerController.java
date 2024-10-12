package server.controller;

import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServerController {
    private Connection con;

    public UserServerController(Connection con) {
        this.con = con;
    }
    public List<User> getAllUser(){
        List<User> users = new ArrayList<User>();
        String query = "SELECT * FROM users";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
//                user.setName(rs.getString("name"));
//                user.setPassword(rs.getString("password"));
                user.setUserName(rs.getString("username"));
                user.setScore(rs.getInt("score"));
                user.setStatus(rs.getInt("status"));
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return users;
    }

    // Cập nhật trạng thái của người dùng.
    public void updateUserStatus(String username, int status) {
        String query = "UPDATE users SET status = ? WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, status);
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("Update status of " + username + " to " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy trạng thái của người dùng.
    public int getUserStatus(String username) {
        String query = "SELECT status FROM users WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("status");
                return status;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Mặc định trả về trạng thái offline nếu không tìm thấy người dùng
        return Constants.STATUS_OFFLINE;
    }

    // Lấy thông tin người dùng theo tên đăng nhập.
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUserName(rs.getString("username"));
                // user.setPassword(rs.getString("password")); // Không cần thiết
                user.setScore(rs.getInt("score"));
                user.setStatus(rs.getInt("status"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Trả về null nếu không tìm thấy người dùng
        return null;
    }

    // Cập nhật điểm số của người dùng.
    public void updateUserScore(String username, int score) {
        String query = "UPDATE users SET score = ? WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, score);
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("Update the score of " + username + " to " + score);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
