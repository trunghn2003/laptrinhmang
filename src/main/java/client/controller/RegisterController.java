package client.controller;

import client.model.ResponseResult;

import javax.swing.*;

public class RegisterController {
    private ClientControl clientControl;

    public RegisterController() {
        clientControl = new ClientControl();
    }

    public void register(String username, String password) {
        ResponseResult result = clientControl.register(username, password);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(null, result.getMessage(), "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, result.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
