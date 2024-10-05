package server.controller;

import server.model.ResponseResult;
import server.model.User;

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
}
