package client.view;

import client.controller.RegisterController;
import client.model.ResponseResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterView extends JFrame implements ActionListener {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnRegister;
    private RegisterController registerController;

    public RegisterView(RegisterController controller) {
        this.registerController = controller;
        this.setTitle("Register");

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnRegister = new JButton("Register");

        JPanel content = new JPanel();
        content.add(new JLabel("Username:"));
        content.add(txtUsername);
        content.add(new JLabel("Password:"));
        content.add(txtPassword);
        content.add(btnRegister);

        btnRegister.addActionListener(this);
        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        ResponseResult result = registerController.register(username, password);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage());
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage());
        }
    }
}
