package client.view;

import client.controller.LoginController;
import client.model.ResponseResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame implements ActionListener {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private LoginController loginController;

    public LoginView(LoginController controller) {
        this.loginController = controller;
        this.setTitle("Login");

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnLogin = new JButton("Login");

        JPanel content = new JPanel();
        content.add(new JLabel("Username:"));
        content.add(txtUsername);
        content.add(new JLabel("Password:"));
        content.add(txtPassword);
        content.add(btnLogin);

        btnLogin.addActionListener(this);
        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());


        ResponseResult result = loginController.login(username, password);
        System.out.println(result);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Login successful! " + result.getMessage());
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage());
        }
    }
}
