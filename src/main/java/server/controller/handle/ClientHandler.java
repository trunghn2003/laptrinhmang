package server.controller.handle;

import server.controller.LoginServerController;
import server.controller.RegisterServerController;
import server.controller.UserServerController;
import server.model.ResponseResult;
import server.model.User;
import server.utils.Constants;
import server.view.ServerView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.*;
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
    private boolean gameStartedWithOpponent = false;
    private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("red", "green", "blue", "yellow", "orange", "purple", "black", "white"));
    private int score = 0;
    private ArrayList<String> selectedColors = new ArrayList<>();

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
                System.out.println("user da gui len server" + user.toString());
                handleUserRequest(user, oos);
            }
            else {
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
            System.out.println("Processing user request: " + user.getActionType());
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
                    }
                    else if(message.startsWith(Constants.ACTION_START_GAME)){
                        sendColorsToClient();
                    }
                    else if (message.startsWith(Constants.ACTION_SEND_COLORS)){
                        sendResultToClient(message, user.getUserName(), this.score);
                    }
                    else if (message.startsWith(Constants.ACTION_GAME_MOVE)){
                        System.out.println("log: " + message);
                    }
                    else if (message.startsWith(Constants.ACTION_EXIT_MID_GAME)){
                        System.out.println("log: " + message);
                    }
                    else {
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

    private void handleInvite(String message) {
        System.out.println("Handling invite with message: " + message);
        serverView.showMessage("Handling invite with message: " + message);

        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String recipientUsername = parts[1];

            System.out.println("Invite recipient: " + recipientUsername);
            serverView.showMessage("Invite recipient: " + recipientUsername);

            // Kiểm tra xem người nhận có online không
            int recipientStatus = userController.getUserStatus(recipientUsername);
            System.out.println("Recipient status: " + (recipientStatus == 1 ? "Online" : "Offline"));
            serverView.showMessage("Recipient status: " + (recipientStatus == Constants.STATUS_ONLINE ? "Online" : "Offline"));

            if (recipientStatus == Constants.STATUS_ONLINE) {
                ClientHandler recipientHandler = getClientHandler(recipientUsername);
                if (recipientHandler != null && !recipientHandler.gameStartedWithOpponent) {
                    recipientHandler.sendMessage(Constants.RESPONSE_INVITE + ":" + user.getUserName());
                    serverView.showMessage(user.getUserName() + " has sent an invite to " + recipientUsername);
                    System.out.println(user.getUserName() + " has sent an invite to " + recipientUsername);
                } else {
                    sendMessage("INVITE_ERROR:" + recipientUsername + " is not available or game already started.");
                    System.out.println("Error: ClientHandler for " + recipientUsername + " not found or game already started.");
                    serverView.showMessage("Error: ClientHandler for " + recipientUsername + " not found or game already started.");
                }
            } else {
                sendMessage("INVITE_ERROR:" + recipientUsername + " is not available.");
                System.out.println("Error: " + recipientUsername + " is not online.");
                serverView.showMessage("Error: " + recipientUsername + " is not online.");
            }
        } else {
            System.out.println("Error: Invalid message format.");
            serverView.showMessage("Error: Invalid message format.");
        }
    }

    // Phương thức xử lý phản hồi lời mời
    private void handleInviteResponse(String message) {
        System.out.println("Handling invite response with message: " + message);
        serverView.showMessage("Handling invite response with message: " + message);

        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String senderUsername = parts[1];
            String response = parts[2];

            System.out.println("Invite response from " + user.getUserName() + ": " + response);
            serverView.showMessage("Invite response from " + user.getUserName() + ": " + response);

            ClientHandler senderHandler = getClientHandler(senderUsername);
            if (senderHandler != null) {
                senderHandler.sendMessage(Constants.RESPONSE_INVITE_RESPONSE + ":" + user.getUserName() + ":" + response);

                if (response.equals("ACCEPT")) {
                    if (!gameStartedWithOpponent && !senderHandler.gameStartedWithOpponent) {
                        userController.updateUserStatus(user.getUserName(), Constants.STATUS_PLAYING);
                        userController.updateUserStatus(senderUsername, Constants.STATUS_PLAYING);

                        System.out.println("Updated status to PLAYING for both " + user.getUserName() + " and " + senderUsername);
                        serverView.showMessage("Updated status to PLAYING for both " + user.getUserName() + " and " + senderUsername);

                        senderHandler.sendMessage(Constants.RESPONSE_GAME_START + ":" + user.getUserName());
                        sendMessage(Constants.RESPONSE_GAME_START + ":" + senderUsername);

                        System.out.println("Game started between " + user.getUserName() + " and " + senderUsername);
                        serverView.showMessage("Game started between " + user.getUserName() + " and " + senderUsername);

                        gameStartedWithOpponent = true;
                        senderHandler.gameStartedWithOpponent = true;

                        broadcastOnlineUsers();
                    } else {
                        System.out.println("Game already started, ignoring duplicate invite response.");
                    }
                } else {
                    System.out.println(user.getUserName() + " has declined the invite from " + senderUsername);
                    serverView.showMessage(user.getUserName() + " has declined the invite from " + senderUsername);
                }
            } else {
                System.out.println("Error: ClientHandler for " + senderUsername + " not found.");
                serverView.showMessage("Error: ClientHandler for " + senderUsername + " not found.");
                sendMessage("INVITE_ERROR:" + senderUsername + " is not available.");
            }
        } else {
            System.out.println("Error: Invalid invite response format.");
            serverView.showMessage("Error: Invalid invite response format.");
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

    // Phương thức tạo màu ngẫu nhiên
    public String random3Color() {
        Set<String> uniqueColors = new HashSet<>();
        Random random = new Random();
        while (uniqueColors.size() < 3) {
            int randomIndex = random.nextInt(colors.size());
            uniqueColors.add(colors.get(randomIndex));
        }
        this.selectedColors.addAll(uniqueColors);
        return String.join(",", uniqueColors);
    }

    // Phương thức kiểm tra màu
    public boolean checkColors(ArrayList<String> colors) {
        System.out.println("Checking colors: " + colors);
        return colors.size() == 3 && this.selectedColors.containsAll(colors);
    }

    public void sendColorsToClient() {

        System.out.println("Sending colors to client: " + user.getUserName());
        serverView.showMessage("Sending colors to client: " + user.getUserName());

        try {
            System.out.println("Random 3 colors for " + user.getUserName());
            serverView.showMessage("Random 3 colors for " + user.getUserName());
            String randomColors = random3Color();

            oos.writeObject(Constants.RESPONSE_RANDOM_COLORS + ":" + randomColors);
            oos.flush();

            Thread.sleep(3000);

            oos.writeObject(null);
            oos.flush();

        } catch (IOException | InterruptedException e) {
            serverView.showMessage("Error sending message to client: " + e.getMessage());
        }
    }

    public void sendResultToClient(String message, String username, int score) {
        ClientHandler client = getClientHandler(username);
        System.out.println("Sending result to client: " + username);
        System.out.println("Message: " + message);
        String result = message.split(":")[1];
        ArrayList<String> resultColors = new ArrayList<>(Arrays.asList(result.split(",")));
        boolean check = checkColors(resultColors);
        System.out.println("result: " + check);
        if(client != null) {
            System.out.println("client found");
//            if(check) {
//                this.score++;
//            }
            sendMessage(
                    Constants.RESPONSE_GAME_RESULT
                            + ":"
                            + client.user.getUserName()
                            + ":"
                            + check
                            + ":"
                            + (check ? score + 1 : score));
        } else {
            serverView.showMessage("Can not find client");
        }
    }

    public void exitMidGame(String message) {
        try {
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
            String opponent = parts.get(1);
            ClientHandler client = getClientHandler(opponent);
            if(client != null) {
                oos.writeObject(Constants.RESPONSE_GAME_RESULT + ":" + user.getUserName() + ":" + "LOSE");
                client.sendMessage(Constants.RESPONSE_GAME_RESULT + ":" + opponent + ":" + "WIN");
            } else {
                serverView.showMessage("Can not find client");
            }
            userController.updateUserStatus(user.getUserName(), Constants.STATUS_ONLINE);
            User opponentUser = userController.getUserByUsername(opponent);
            opponentUser.setStatus(Constants.STATUS_ONLINE);
            gameStartedWithOpponent = false;
            broadcastOnlineUsers();
        } catch (Exception e) {
            serverView.showMessage("Error exiting mid-game: " + e.getMessage());
        }
    }

    public void getGameResult(String message) {
        try {
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
            String opponent = parts.get(1);
            oos.writeObject(Constants.ACTION_GAME_RESULT);
            oos.flush();
        } catch (IOException e) {
            serverView.showMessage("Error sending message to client: " + e.getMessage());
        }
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
