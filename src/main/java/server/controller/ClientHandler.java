package server.controller;

import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;
import server.view.ServerView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private LoginServerController loginController;
    private RegisterServerController registerController;
    private UserServerController userController;
    private ServerView serverView;
    private AtomicInteger clientIdCounter;
    private User user;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    // Danh sách các client đang kết nối
    private static List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket, LoginServerController loginController,
                         RegisterServerController registerController,
                         UserServerController userController,
                         ServerView serverView, AtomicInteger clientIdCounter) {
        this.clientSocket = socket;
        this.loginController = loginController;
        this.registerController = registerController;
        this.userController = userController;
        this.serverView = serverView;
        this.clientIdCounter = clientIdCounter;
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
            // Nếu đăng nhập thành công, bắt đầu lắng nghe các yêu cầu khác
            if (user != null && user.getActionType().equals(Constants.ACTION_LOGIN)) {
                clientHandlers.add(this); // Thêm vào danh sách client đang kết nối
                // Cập nhật trạng thái người dùng thành online
                userController.updateUserStatus(user.getUserName(), Constants.STATUS_ONLINE);
                listenForMessages(); // Bắt đầu lắng nghe các tin nhắn từ client
            }

        } catch (Exception e) {
            serverView.showMessage("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Xử lý khi client ngắt kết nối
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (user != null) {
                    // Cập nhật trạng thái người dùng thành offline
                    userController.updateUserStatus(user.getUserName(), Constants.STATUS_OFFLINE);
                    clientHandlers.remove(this);
                    // Gửi cập nhật danh sách người chơi online tới các client khác
                    broadcastOnlineUsers();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    // Lắng nghe các tin nhắn từ client
    private void listenForMessages() {
        try {
            // Gửi danh sách người chơi online ban đầu
            sendOnlineUsers();

            while (true) {
                Object obj = ois.readObject();
                if (obj instanceof String) {
                    String message = (String) obj;
                    if (message.startsWith(Constants.ACTION_INVITE + ":")) {
                        handleInvite(message);
                    } else if (message.startsWith(Constants.ACTION_INVITE_RESPONSE + ":")) {
                        handleInviteResponse(message);
                    } else {
                        serverView.showMessage("Nhận được tin nhắn không xác định: " + message);
                    }
                } else {
                    serverView.showMessage("Nhận được đối tượng không xác định từ client.");
                }
            }
        } catch (IOException e) {
            serverView.showMessage("Client ngắt kết nối: " + user.getUserName());
        } catch (Exception e) {
            serverView.showMessage("Lỗi trong listenForMessages: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Xử lý khi client ngắt kết nối
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (user != null) {
                    // Cập nhật trạng thái người dùng thành offline
                    userController.updateUserStatus(user.getUserName(), Constants.STATUS_OFFLINE);
                    clientHandlers.remove(this);
                    // Gửi cập nhật danh sách người chơi online tới các client khác
                    broadcastOnlineUsers();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Gửi danh sách người chơi online tới client
    private void sendOnlineUsers() {
        try {
            List<User> onlineUsers = userController.getAllUser();
            oos.writeObject(onlineUsers);
            oos.flush();
        } catch (IOException e) {
            serverView.showMessage("Lỗi khi gửi danh sách người chơi online: " + e.getMessage());
        }
    }

    // Phát sóng danh sách người chơi online tới tất cả các client
    private void broadcastOnlineUsers() {
        for (ClientHandler handler : clientHandlers) {
            handler.sendOnlineUsers();
        }
    }

    // Xử lý lời mời chơi game
    private void handleInvite(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String recipientUsername = parts[1];
            // Kiểm tra xem người nhận có online không
            int recipientStatus = userController.getUserStatus(recipientUsername);
            if (recipientStatus == Constants.STATUS_ONLINE) {
                // Tìm ClientHandler của người nhận
                ClientHandler recipientHandler = getClientHandler(recipientUsername);
                if (recipientHandler != null) {
                    // Gửi lời mời tới người nhận
                    recipientHandler.sendMessage(Constants.RESPONSE_INVITE + ":" + user.getUserName());
                    serverView.showMessage(user.getUserName() + " đã gửi lời mời tới " + recipientUsername);
                } else {
                    sendMessage("INVITE_ERROR:" + recipientUsername + " không khả dụng.");
                }
            } else {
                sendMessage("INVITE_ERROR:" + recipientUsername + " không khả dụng.");
            }
        }
    }

    // Xử lý phản hồi lời mời
    private void handleInviteResponse(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String senderUsername = parts[1];
            String response = parts[2];

            // Tìm ClientHandler của người gửi lời mời
            ClientHandler senderHandler = getClientHandler(senderUsername);
            if (senderHandler != null) {
                senderHandler.sendMessage(Constants.RESPONSE_INVITE_RESPONSE + ":" + user.getUserName() + ":" + response);
                if (response.equals("ACCEPT")) {
                    // Cập nhật trạng thái người chơi thành đang chơi
                    userController.updateUserStatus(user.getUserName(), Constants.STATUS_PLAYING);
                    userController.updateUserStatus(senderUsername, Constants.STATUS_PLAYING);
                    // Gửi thông báo bắt đầu trận đấu
                    senderHandler.sendMessage(Constants.RESPONSE_GAME_START + ":" + user.getUserName());
                    sendMessage(Constants.RESPONSE_GAME_START + ":" + senderUsername);
                    serverView.showMessage("Trận đấu giữa " + user.getUserName() + " và " + senderUsername + " đã bắt đầu.");
                    // Gửi cập nhật danh sách người chơi online
                    broadcastOnlineUsers();
                } else {
                    serverView.showMessage(user.getUserName() + " đã từ chối lời mời từ " + senderUsername);
                }
            } else {
                sendMessage("INVITE_ERROR:" + senderUsername + " không khả dụng.");
            }
        }
    }

    // Gửi tin nhắn tới client
    public void sendMessage(String message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            serverView.showMessage("Lỗi khi gửi tin nhắn tới client: " + e.getMessage());
        }
    }

    // Tìm ClientHandler theo tên người dùng
    private ClientHandler getClientHandler(String username) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.user != null && handler.user.getUserName().equals(username)) {
                return handler;
            }
        }
        return null;
    }

    // Getter cho user
    public User getUser() {
        return user;
    }
}
