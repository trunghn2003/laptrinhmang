package server.controller;

import server.view.ServerView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerControl {
    private Connection con;
    private ServerSocket myServer;
    private int serverPort = 8888;
    private LoginServerController loginController;
    private RegisterServerController registerController;
    private UserServerController userController;
    private ServerView serverView;
    private AtomicInteger clientIdCounter = new AtomicInteger(1);  // Bộ đếm ID cho client

    public ServerControl(ServerView serverView) throws Exception {
        this.serverView = serverView;
        getDBConnection("thuchanh1", "root", "trunghn2003");
        loginController = new LoginServerController(con);
        registerController = new RegisterServerController(con);
        userController = new UserServerController(con);
        openServer(serverPort);
    }

    private void getDBConnection(String dbName, String username, String password) throws Exception {
        String dbUrl = "jdbc:mysql://localhost:3308/" + dbName;
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
                ClientHandler clientHandler = new ClientHandler(clientSocket, loginController, registerController, userController, serverView, clientIdCounter);
                new Thread(clientHandler).start();

            } catch (IOException e) {
                serverView.showMessage("Error accepting client connection: " + e.getMessage());
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
        }
    }
}
