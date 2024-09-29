package server.controller;


import server.model.ResponseResult;
import server.model.User;
import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterServerController {
    private Connection con;


    public RegisterServerController(Connection con) {
        this.con = con;
    }

    public ResponseResult register(User user) {

        if (isUsernameTaken(user.getUserName())) {
            return (new ResponseResult(false, "Username already exists."));
        }
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, hashedPassword);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                new ResponseResult(true, "Registration successful!");
            } else {
                new ResponseResult(false, "Failed to register user.");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return new ResponseResult(false, "Database error occurred.");
        }
        return (new ResponseResult(true, "User registered successfully!"));
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
