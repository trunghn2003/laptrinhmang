package server.controller.handle;

import server.controller.LoginServerController;
import server.controller.RegisterServerController;
import server.controller.UserServerController;
import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;
import server.view.ServerView;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable, IClientHandler {
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
        try {
            // Khởi tạo luồng vào ra
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            // Đọc đối tượng ban đầu từ client (đăng nhập hoặc đăng ký)
            Object receivedObject = ois.readObject();
            if (receivedObject instanceof User) {
                user = (User) receivedObject;
                handleUserRequest(user, oos);
            } else {
                serverView.showMessage("Invalid object received from client.");
                oos.writeObject("Invalid object received.");
            }

            // Nếu đăng nhập thành công, bắt đầu lắng nghe các yêu cầu khác
            if (user != null && user.getActionType().equalsIgnoreCase(Constants.ACTION_LOGIN)) {
                clientHandlers.add(this); // Thêm vào danh sách client đang kết nối
                // Cập nhật trạng thái người dùng thành online
                userController.updateUserStatus(user.getUserName(), Constants.STATUS_ONLINE);
                // Phát sóng danh sách người chơi online
                broadcastOnlineUsers();
                listenForMessages(); // Bắt đầu lắng nghe các tin nhắn từ client
            }

        } catch (Exception e) {
            serverView.showMessage("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeClientSocket();
        }
    }

    @Override
    public void handleUserRequest(User user, ObjectOutputStream oos) {
        try {
            switch (user.getActionType().toLowerCase()) {
                case Constants.ACTION_LOGIN:
                    handleLoginRequest(user, oos);
                    break;

                case Constants.ACTION_REGISTER:
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
            oos.writeObject(Constants.LOGIN_SUCCESS);
            oos.flush();
            serverView.showMessage("Login successful for user: " + user.getUserName());
        } else {
            oos.writeObject(Constants.LOGIN_FAILURE + ":" + result.getMessage());
            oos.flush();
            serverView.showMessage("Login failed for user: " + user.getUserName() + " - " + result.getMessage());
            this.user = null; // Hủy người dùng
        }
    }

    private void handleRegisterRequest(User user, ObjectOutputStream oos) throws Exception {
        serverView.showMessage("Processing registration request for user: " + user.getUserName());
        ResponseResult result = registerController.register(user);

        if (result.isSuccess()) {
            oos.writeObject(Constants.REGISTER_SUCCESS);
            oos.flush();
            serverView.showMessage("Registration successful for user: " + user.getUserName());
        } else {
            oos.writeObject(Constants.REGISTER_FAILURE + ":" + result.getMessage());
            oos.flush();
            serverView.showMessage("Registration failed for user: " + user.getUserName() + " - " + result.getMessage());
        }
        this.user = null; // Sau khi đăng ký, cần đăng nhập lại
    }

    private void handleUnknownRequest(User user, ObjectOutputStream oos) throws Exception {
        String message = "Unknown action type received: " + user.getActionType();
        serverView.showMessage(message);
        oos.writeObject(message);
        oos.flush();
        this.user = null;
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
                        serverView.showMessage("Received unknown message: " + message);
                    }
                } else {
                    serverView.showMessage("Received unknown object from client.");
                }
            }
        } catch (IOException e) {
            serverView.showMessage("Client disconnected: " + user.getUserName());
        } catch (Exception e) {
            serverView.showMessage("Error in listenForMessages: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeClientSocket();
        }
    }

    // Gửi danh sách người chơi online tới client này
    private void sendOnlineUsers() {
        try {
            List<User> onlineUsers = userController.getAllUser();
            oos.writeObject(onlineUsers);
            oos.flush();
        } catch (IOException e) {
            serverView.showMessage("Error sending online users list: " + e.getMessage());
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
                    serverView.showMessage(user.getUserName() + " has sent an invite to " + recipientUsername);
                } else {
                    sendMessage("INVITE_ERROR:" + recipientUsername + " is not available.");
                }
            } else {
                sendMessage("INVITE_ERROR:" + recipientUsername + " is not available.");
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
                    serverView.showMessage("Game started between " + user.getUserName() + " and " + senderUsername);
                    // Phát sóng danh sách người chơi online
                    broadcastOnlineUsers();
                } else {
                    serverView.showMessage(user.getUserName() + " has declined the invite from " + senderUsername);
                }
            } else {
                sendMessage("INVITE_ERROR:" + senderUsername + " is not available.");
            }
        }
    }

    // Gửi tin nhắn tới client
    public void sendMessage(String message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            serverView.showMessage("Error sending message to client: " + e.getMessage());
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

    @Override
    public void closeClientSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                serverView.showMessage("Connection closed with client: " + (user != null ? user.getUserName() : "unknown"));
            }
            if (user != null) {
                // Cập nhật trạng thái người dùng thành offline
                userController.updateUserStatus(user.getUserName(), Constants.STATUS_OFFLINE);
                clientHandlers.remove(this);
                // Phát sóng danh sách người chơi online
                broadcastOnlineUsers();
            }
        } catch (Exception e) {
            serverView.showMessage("Error closing client connection: " + e.getMessage());
        }
    }
}
