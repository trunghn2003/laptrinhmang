package server.controller;

import server.controller.handle.ClientHandler;
import server.controller.handle.IClientHandler;
import server.view.ServerView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.cdimascio.dotenv.Dotenv;

public class ServerControl {
    private Connection con;
    private Dotenv dotenv;
    private ServerSocket myServer;
    private int serverPort;
    private LoginServerController loginController;
    private RegisterServerController registerController;
    private UserServerController userController;
    private GameServerController gameController;
    private ServerView serverView;
    private AtomicInteger clientIdCounter = new AtomicInteger(1);

    public ServerControl(ServerView serverView) throws Exception {
        this.serverView = serverView;
        this.dotenv = Dotenv.load();
        this.serverPort = Integer.parseInt(dotenv.get("SERVER_PORT"));
        getDBConnection(dotenv.get("DB_DATABASE"), dotenv.get("DB_USERNAME"), dotenv.get("DB_PASSWORD"));
        loginController = new LoginServerController(con);
        registerController = new RegisterServerController(con);
        userController = new UserServerController(con);
        gameController = new GameServerController(con);
        openServer(serverPort);
    }

    private void getDBConnection(String dbName, String username, String password) throws Exception {
        String dbUrl = "jdbc:mysql://localhost:" + dotenv.get("DB_PORT") + "/" + dbName;
        String dbClass = "com.mysql.cj.jdbc.Driver";
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
            listenForClients();
        } catch (IOException e) {
            serverView.showMessage("Error starting server: " + e.getMessage());
            throw new Exception("Server start error", e);
        }
    }

    private void listenForClients() {
        while (true) {
            try {
                Socket clientSocket = myServer.accept();
                serverView.showMessage("Client connected: " + clientSocket.getInetAddress());
                IClientHandler clientHandler = new ClientHandler(
                        clientSocket, loginController, registerController, userController, serverView, clientIdCounter, gameController
                );
                new Thread((Runnable) clientHandler).start();

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
