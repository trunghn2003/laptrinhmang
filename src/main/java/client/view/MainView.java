package client.view;

import client.controller.ClientControl;
import client.controller.LoginController;
import client.controller.RegisterController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainView extends JFrame implements ActionListener {
    private JButton btnLogin, btnRegister;

    public MainView() {
        super("Main Menu");

        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");

        JPanel content = new JPanel();
        content.add(btnLogin);
        content.add(btnRegister);

        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);

        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(btnLogin)) {
            ClientControl clientControl = new ClientControl();
            LoginController loginController = new LoginController(clientControl);  // Truyền ClientControl vào LoginController
            LoginView loginView = new LoginView(loginController);
            loginView.setVisible(true);
        }
        else if (e.getSource().equals(btnRegister)) {
            ClientControl clientControl = new ClientControl();
            RegisterController registerController = new RegisterController(clientControl);
            RegisterView registerView = new RegisterView(registerController);
            registerView.setVisible(true);
        }
    }

    public static void main(String[] args) {
        MainView mainView = new MainView();
        mainView.setVisible(true);
    }
}
