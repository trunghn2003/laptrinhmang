package server.controller;

import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;
import utils.PasswordUtils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginServerController {
    private Connection con;

    public LoginServerController(Connection con) {
        this.con = con;
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param user The user attempting to log in.
     * @return A ResponseResult indicating success or failure.
     */
    public ResponseResult authenticate(User user) {
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ResponseResult(true, Constants.LOGIN_SUCCESS);
            } else {
                return new ResponseResult(false, Constants.LOGIN_FAILURE + ": Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseResult(false, "Database error occurred: " + e.getMessage());
        }
    }
}
