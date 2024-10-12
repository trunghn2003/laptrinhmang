package client.view;

import client.controller.RegisterController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Giao diện đăng ký người dùng.
 */
public class RegisterView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, cancelButton;
    private RegisterController registerController;

    public RegisterView() {
        registerController = new RegisterController();
        setupUI();
    }

    private void setupUI() {
        setTitle("Register");
        setSize(350, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(usernameLabel);
        panel.add(usernameField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordField);

        panel.add(registerButton);
        panel.add(cancelButton);

        add(panel);

        setVisible(true);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        registerController.register(username, password);
    }
}
