package client.view;

import client.controller.RegisterController;

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
    }

    public void actionPerformed(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String result = String.valueOf(registerController.register(username, password));
        if (registerController.register(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
        } else {
            JOptionPane.showMessageDialog(this, result);
        }
    }
}
