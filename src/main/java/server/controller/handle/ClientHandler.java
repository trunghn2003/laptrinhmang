package server.controller.handle;

import server.controller.LoginServerController;
import server.controller.RegisterServerController;
import server.controller.UserServerController;
import server.model.ResponseResult;
import server.model.User;
import server.view.ServerView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable, IClientHandler {
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

            Object receivedObject = ois.readObject();
            if (receivedObject instanceof User) {
                User user = (User) receivedObject;
                handleUserRequest(user, oos);
            } else {
                serverView.showMessage("Invalid object received from client.");
                oos.writeObject("Invalid object received.");
            }

        } catch (Exception e) {
            serverView.showMessage("Error handling client connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeClientSocket();
        }
    }

    @Override
    public void handleUserRequest(User user, ObjectOutputStream oos) {
        try {
            ResponseResult result;
            switch (user.getActionType().toLowerCase()) {
                case "login":
                    handleLoginRequest(user, oos);
                    break;

                case "register":
                    handleRegisterRequest(user, oos);
                    break;

                default:
                    handleUnknownRequest(user, oos);
                    break;
            }
        } catch (Exception e) {
            serverView.showMessage("Error processing user request: " + e.getMessage());
        }
    }

    private void handleLoginRequest(User user, ObjectOutputStream oos) throws Exception {
        serverView.showMessage("Processing login request for user: " + user.getUserName());
        ResponseResult result = loginController.authenticate(user);

        if (result.isSuccess()) {
            oos.writeObject(userController.getAllUser());
            serverView.showMessage("Login successful for user: " + user.getUserName());
        } else {
            oos.writeObject(result.getMessage());
            serverView.showMessage("Login failed for user: " + user.getUserName() + " - " + result.getMessage());
        }
    }

    private void handleRegisterRequest(User user, ObjectOutputStream oos) throws Exception {
        serverView.showMessage("Processing registration request for user: " + user.getUserName());
        ResponseResult result = registerController.register(user);

        if (result.isSuccess()) {
            oos.writeObject("Registration successful");
            serverView.showMessage("Registration successful for user: " + user.getUserName());
        } else {
            oos.writeObject(result.getMessage());
            serverView.showMessage("Registration failed for user: " + user.getUserName() + " - " + result.getMessage());
        }
    }

    private void handleUnknownRequest(User user, ObjectOutputStream oos) throws Exception {
        String message = "Unknown action type received: " + user.getActionType();
        serverView.showMessage(message);
        oos.writeObject(message);
    }

    @Override
    public void closeClientSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                serverView.showMessage("Client socket closed.");
            }
        } catch (Exception e) {
            serverView.showMessage("Error closing client socket: " + e.getMessage());
        }
    }
}
