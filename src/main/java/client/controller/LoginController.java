package client.controller;

import client.model.ResponseResult;
import client.view.MainView;

import javax.swing.*;

public class LoginController {
    private ClientControl clientControl;

    public LoginController() {
        clientControl = new ClientControl();
    }

    public void login(String username, String password) {
        ResponseResult result = clientControl.login(username, password);
        if (result.isSuccess()) {
            // Mở giao diện chính
            MainView mainView = new MainView(clientControl, result.getData());
            mainView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, result.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
