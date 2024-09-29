package server.controller;

 import server.model.ResponseResult;
import server.model.User;
import server.view.ServerView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

public class ServerControl {
    private Connection con;
    private ServerSocket myServer;
    private int serverPort = 8888;
    private LoginServerController loginController;
    private RegisterServerController registerController;
    private ServerView serverView;

    public ServerControl(ServerView serverView) throws Exception {
        this.serverView = serverView;
        getDBConnection("thuchanh1", "root", "trunghn2003");
        loginController = new LoginServerController(con);
        registerController = new RegisterServerController(con);
        openServer(serverPort);
    }

    private void getDBConnection(String dbName, String username, String password) throws Exception {
        String dbUrl = "jdbc:mysql://localhost:3307/" + dbName;
        String dbClass = "com.mysql.jdbc.Driver";
        try {
            Class.forName(dbClass);
            con = DriverManager.getConnection(dbUrl, username, password);
            serverView.showMessage("Database connected.");
        } catch (Exception e) {
            serverView.showMessage("Error connecting to database: " + e.getMessage());
            throw new Exception("Database connection error", e);
        }
    }

    private void openServer(int portNumber) throws Exception {
        try {
            myServer = new ServerSocket(portNumber);
            serverView.showMessage("Server started on port " + portNumber);
            listenning();
        } catch (IOException e) {
            serverView.showMessage("Error starting server: " + e.getMessage());
            throw new Exception("Server start error", e);
        }
    }

    private void listenning() {
        while (true) {
            try {
                Socket clientSocket = myServer.accept();
                serverView.showMessage("Client connected: " + clientSocket.getInetAddress());
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException e) {
                serverView.showMessage("Error accepting client connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Object o = ois.readObject();
            try {
                if (o instanceof User) {
                    User user = (User) o;
                    ResponseResult result;


                    if ("login".equals(user.getActionType())) {
                        serverView.showMessage("Processing login request from client: " + user.getUserName());
                        result = loginController.authenticate(user);
                        if (result.isSuccess()) {
                            oos.writeObject("ok");
                            serverView.showMessage("Login successful for user: " + user.getUserName());
                        } else {
                            oos.writeObject(result.getMessage());
                            serverView.showMessage("Login failed for user: " + user.getUserName() + " - " + result.getMessage());
                        }


                    } else if ("register".equals(user.getActionType())) {
                        serverView.showMessage("Processing registration request from client: " + user.getUserName());
                        result = registerController.register(user);
                        if (result.isSuccess()) {
                            oos.writeObject("ok");
                            serverView.showMessage("Registration successful for user: " + user.getUserName());
                        } else {
                            oos.writeObject(result.getMessage());
                            serverView.showMessage("Registration failed for user: " + user.getUserName() + " - " + result.getMessage());
                        }
                    } else {
                        serverView.showMessage("Unknown action type received from client: " + user.getUserName());
                    }
                } else {
                    serverView.showMessage("Invalid object received from client.");
                }

            } catch (Exception e) {
                serverView.showMessage("Error processing client request: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public void stopServer() {
        try {
            if (myServer != null && !myServer.isClosed()) {
                myServer.close();
                serverView.showMessage("Server stopped.");
            }
        } catch (IOException e) {
            serverView.showMessage("Error stopping server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
