package client.controller;

import client.model.ResponseResult;
import server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    private ClientControl clientControl;

    public LoginController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public ResponseResult login(String username, String password) {
        User user = new User(username, password, "login");
        clientControl.openConnection();
        System.out.println("Connected to server");
        clientControl.sendData(user);
        String result = clientControl.receiveData();
        System.out.println("Result: " + result);
        clientControl.closeConnection();
        if ("ok".equals(result)) {
            return new ResponseResult(true, result);

        }
        else {
            return new ResponseResult(false, result);
        }
    }
}

