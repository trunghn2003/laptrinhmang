package server.controller;

import server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServerController {
    private Connection con;

    public LoginServerController(Connection con) {
        this.con = con;
    }


    public boolean authenticate(User user) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getPassword());
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // Trả về true nếu người dùng tồn tại
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
