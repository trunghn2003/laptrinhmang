package server.controller;

import server.model.ResponseResult;
import server.model.User;
import server.view.ServerView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private LoginServerController loginController;
    private RegisterServerController registerController;
    private UserServerController userController;
    private ServerView serverView;

    public ClientHandler(Socket socket, LoginServerController loginController,
                         RegisterServerController registerController,
                         UserServerController userController,
                         ServerView serverView, AtomicInteger clientIdCounter) {
        this.clientSocket = socket;
        this.loginController = loginController;
        this.registerController = registerController;
        this.userController = userController;
        this.serverView = serverView;
    }

    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Object o = ois.readObject();
            if (o instanceof User) {
                User user = (User) o;
                handleUserRequest(user, oos);
            } else {
                serverView.showMessage("Invalid object received from client.");
            }

        } catch (Exception e) {
            serverView.showMessage("Error handling client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Xử lý yêu cầu đăng nhập hoặc đăng ký
    private void handleUserRequest(User user, ObjectOutputStream oos) {
        try {
            ResponseResult result;
            switch (user.getActionType()) {
                case "login":
                    serverView.showMessage("Processing login request from client: " + user.getUserName());
                    result = loginController.authenticate(user);
                    if (result.isSuccess()) {
                        oos.writeObject(userController.getAllUser());
                        serverView.showMessage("Login successful for user: " + user.getUserName());
                    } else {
                        oos.writeObject(result.getMessage());
                        serverView.showMessage("Login failed for user: " + user.getUserName() + " - " + result.getMessage());
                    }
                    break;

                case "register":
                    serverView.showMessage("Processing registration request from client: " + user.getUserName());
                    result = registerController.register(user);  // Gọi phương thức register từ registerController
                    if (result.isSuccess()) {
                        oos.writeObject("ok");
                        serverView.showMessage("Registration successful for user: " + user.getUserName());
                    } else {
                        oos.writeObject(result.getMessage());
                        serverView.showMessage("Registration failed for user: " + user.getUserName() + " - " + result.getMessage());
                    }
                    break;

                default:
                    serverView.showMessage("Unknown action type received from client: " + user.getUserName());
                    oos.writeObject("Unknown action type");
            }
        } catch (Exception e) {
            serverView.showMessage("Error sending response to client: " + e.getMessage());
        }
    }
}
