package client.controller;

import server.model.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;


public class ClientControl {
    Dotenv dotenv = Dotenv.load();
    private Socket mySocket;
    private String serverHost = dotenv.get("SERVER_HOST");
    private int serverPort = Integer.parseInt(dotenv.get("SERVER_PORT"));
    public ClientControl() {
    }

    public Socket openConnection() {
        try {

            mySocket = new Socket(serverHost, serverPort);
            System.out.println("Connected to " + serverHost + ":" + serverPort);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return mySocket;
    }

    public boolean sendData(User user) {
        try {

            ObjectOutputStream oos =

                    new ObjectOutputStream(mySocket.getOutputStream());
            oos.writeObject(user);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public Object receiveData() {
        Object result = null;
        try {
            ObjectInputStream ois =
                    new ObjectInputStream(mySocket.getInputStream());
            Object o = ois.readObject();


            if (o instanceof ArrayList<?>) {
                ArrayList<?> list = (ArrayList<?>) o;
                if (!list.isEmpty() && list.get(0) instanceof User) {
                    result = list;
                }
            }
            if (o instanceof String) {
                result = (String) o;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return result;
    }


    public boolean closeConnection() {
        try {
            mySocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }
}