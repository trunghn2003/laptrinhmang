package client.controller;

import server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    private ClientControl clientControl;

    public LoginController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public boolean login(String username, String password) {
        User user = new User(username, password);
        clientControl.openConnection();
        System.out.println("Connected to server");
        clientControl.sendData(user);
        String result = clientControl.receiveData();
        System.out.println("Result: " + result);
        clientControl.closeConnection();
        return "ok".equals(result);  //
    }
}

