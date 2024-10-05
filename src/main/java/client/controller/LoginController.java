package client.controller;

import client.model.ResponseResult;
import server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        Object result = clientControl.receiveData();
        if( result instanceof String) {
            System.out.println("Result: " + result);
            clientControl.closeConnection();
            if ("ok".equals(result)) {
                return new ResponseResult(true, (String) result);

            } else {
                return new ResponseResult(false, (String) result);
            }
        }
        if(result instanceof ArrayList<?>) {
            System.out.println("Result: " + result);
            ResponseResult res =  new ResponseResult(true, null);
            res.setData((List<User>) result);
            return res;
        }
        return new ResponseResult(false, null);
    }
}

