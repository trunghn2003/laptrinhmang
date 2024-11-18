package client.controller;

import client.model.ResponseResult;
import client.utils.Constants;
import server.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;


public class ClientControl {
    Dotenv dotenv = Dotenv.load();
    private Socket mySocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String serverHost = dotenv.get("SERVER_HOST");
    private int serverPort = Integer.parseInt(dotenv.get("SERVER_PORT"));
    private User currentUser;
    public ClientControl() {
    }

    /**
     * Mở kết nối tới server.
     *
     * @return Socket kết nối hoặc null nếu thất bại.
     */
    public Socket openConnection() {
        try {
            mySocket = new Socket(serverHost, serverPort);
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());
            System.out.println("Connected to server " + serverHost + ":" + serverPort);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return mySocket;
    }

    /**
     * Gửi đối tượng User tới server (dùng cho đăng nhập hoặc đăng ký).
     *
     * @param user Đối tượng User chứa thông tin đăng nhập hoặc đăng ký.
     * @return true nếu gửi thành công, false nếu thất bại.
     */
    public boolean sendUser(User user) {
        try {
            oos.writeObject(user);
            oos.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Gửi tin nhắn dạng String tới server.
     *
     * @param message Tin nhắn cần gửi.
     * @return true nếu gửi thành công, false nếu thất bại.
     */
    public boolean sendMessage(String message) {
        try {
            oos.writeObject(message);
            oos.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Nhận dữ liệu từ server.
     *
     * @return Đối tượng nhận được từ server hoặc null nếu thất bại.
     */
    public synchronized Object receiveData() {
        try {
            Object o = ois.readObject();
            System.out.println("Data received from server: " + o);
            System.out.println("Received data type: " + o.getClass().getName());
            return o;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Đóng kết nối với server.
     *
     * @return true nếu đóng thành công, false nếu thất bại.
     */
    public boolean closeConnection() {
        try {
            if (mySocket != null) {
                mySocket.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Thực hiện đăng nhập.
     *
     * @param username Tên người dùng.
     * @param password Mật khẩu.
     * @return Đối tượng ResponseResult chứa kết quả đăng nhập.
     */
    public ResponseResult login(String username, String password) {
        User user = new User(username, password, Constants.ACTION_LOGIN);
        if (openConnection() == null) {
            return new ResponseResult(false, "Cannot connect to server.");
        }
        if (!sendUser(user)) {
            closeConnection();
            return new ResponseResult(false, "Error sending data to server.");
        }
        Object response = receiveData();
        if (response instanceof String) {
            String message = (String) response;
            if (message.equals(Constants.LOGIN_SUCCESS)) {
                currentUser = user;
                System.out.println(user);
                // Nhận danh sách người chơi online
                Object userList = receiveData();
                if (userList instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<User> users = (java.util.List<User>) userList;
                    ResponseResult result = new ResponseResult(true, "Login successful.");
                    result.setData(users);
                    return result;
                }
            } else if (message.startsWith(Constants.LOGIN_FAILURE)) {
                String errorMessage = message.split(":", 2)[1];
                closeConnection();
                return new ResponseResult(false, errorMessage);
            }
        }
//        closeConnection();
        return new ResponseResult(false, "Unknown error occurred.");
    }

    /**
     * Thực hiện đăng ký.
     *
     * @param username Tên người dùng.
     * @param password Mật khẩu.
     * @return Đối tượng ResponseResult chứa kết quả đăng ký.
     */
    public ResponseResult register(String username, String password) {
        User user = new User(username, password, Constants.ACTION_REGISTER);
        if (openConnection() == null) {
            return new ResponseResult(false, "Cannot connect to server.");
        }
        if (!sendUser(user)) {
            closeConnection();
            return new ResponseResult(false, "Error sending data to server.");
        }
        Object response = receiveData();
        if (response instanceof String) {
            String message = (String) response;
            if (message.equals(Constants.REGISTER_SUCCESS)) {
                closeConnection();
                return new ResponseResult(true, "Registration successful. Please login.");
            } else if (message.startsWith(Constants.REGISTER_FAILURE)) {
                String errorMessage = message.split(":", 2)[1];
                closeConnection();
                return new ResponseResult(false, errorMessage);
            }
        }
        closeConnection();
        return new ResponseResult(false, "Unknown error occurred.");
    }

    public User getCurrentUser() {
        return currentUser;
    }


    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }


    /**
     * Nhận luồng đầu vào.
     *
     * @return ObjectInputStream
     */
    public ObjectInputStream getObjectInputStream() {
        return ois;
    }

    /**
     * Nhận luồng đầu ra.
     *
     * @return ObjectOutputStream
     */
    public ObjectOutputStream getObjectOutputStream() {
        return oos;
    }

//    public java.util.List<User> getAllUser() {
////        User user = new User(username, password, Constants.ACTION_LOGIN);
////        if (openConnection() == null) {
////            return new ResponseResult(false, "Cannot connect to server.");
////        }
////        if (!sendUser(user)) {
////            closeConnection();
////            return new ResponseResult(false, "Error sending data to server.");
////        }
//        System.out.println("Get all user");
//        Object response = receiveData();
//        System.out.println("get all user: "+response);
//        if (response instanceof java.util.List) {
//            @SuppressWarnings("unchecked")
//            java.util.List<User> users = (java.util.List<User>) response;
//            return users;
//        }
//        return null;
//    }
// Send request for match history to the server
    public boolean sendMatchHistoryRequest() {
        if (currentUser == null) {
            System.out.println("User chưa đăng nhập.");
            return false;
        }

        // Create the request message
        String message = Constants.ACTION_GET_HISTORY + ":" + currentUser.getUserName();
        sendMessage(message);
    //    if (!sendMessage(message)) {
    //        System.out.println("Gửi yêu cầu lịch sử đấu thất bại.");
    //        return false;
    //    }
        return true;  // Request was sent successfully
    }

    // Receive and process match history response from the server
    // Receive and process match history response from the server
    public Map<String, Map<String, Object>> receiveMatchHistory() {
        // Nhận dữ liệu từ server
        Object response =receiveData(); // ois là ObjectInputStream
        if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> matchHistory = (Map<String, Map<String, Object>>) response;
            return matchHistory;
        } else {
            System.out.println("Phản hồi không đúng định dạng.");
            return Collections.emptyMap();
        }
    }



}


