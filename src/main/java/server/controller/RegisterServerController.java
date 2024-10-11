package server.controller;

import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;
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

    /**
     * Registers a new user.
     *
     * @param user The user to register.
     * @return A ResponseResult indicating success or failure.
     */
    public ResponseResult register(User user) {
        if (isUsernameTaken(user.getUserName())) {
            return new ResponseResult(false, Constants.REGISTER_FAILURE + ": Username already exists.");
        }
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, hashedPassword);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return new ResponseResult(true, Constants.REGISTER_SUCCESS);
            } else {
                return new ResponseResult(false, Constants.REGISTER_FAILURE + ": Failed to register user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseResult(false, "Database error occurred: " + e.getMessage());
        }
    }

    /**
     * Checks if a username is already taken.
     *
     * @param username The username to check.
     * @return True if the username is taken, false otherwise.
     */
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
