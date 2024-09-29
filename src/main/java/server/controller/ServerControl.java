package server.controller;

import server.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ServerControl {
    private Connection con;
    private ServerSocket myServer;
    private int serverPort = 8888;
    private LoginServerController loginController;
    private RegisterServerController  registerController;
    public ServerControl(){
        getDBConnection("thuchanh1", "root", "trunghn2003");
        loginController = new LoginServerController(con);
        registerController = new RegisterServerController(con);
        openServer(serverPort);
        while(true){
            listenning();
        }
    }
    private void getDBConnection(String dbName, String username,
                                 String password){

        String dbUrl = "jdbc:mysql://localhost:3307/" + dbName;
        String dbClass = "com.mysql.jdbc.Driver";
        try {
            Class.forName(dbClass);
            con = DriverManager.getConnection (dbUrl,

                    username, password);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void openServer(int portNumber){
        try {
            myServer = new ServerSocket(portNumber);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void listenning() {
        try {
            Socket clientSocket = myServer.accept();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());

            Object o = ois.readObject();
            if (o instanceof User) {
                User user = (User) o;
                if("login".equals(user.getActionType())){
                    boolean isAuthenticated = loginController.authenticate(user);

                    if (isAuthenticated) {
                        oos.writeObject("ok");
                    } else {
                        oos.writeObject("false");
                    }
                }
                else {
                    boolean isRegistered = registerController.register(user);
                    if (!isRegistered) {
                        oos.writeObject("Username already exists.");
                    } else {
                        oos.writeObject("ok");
                    }

                }



            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}