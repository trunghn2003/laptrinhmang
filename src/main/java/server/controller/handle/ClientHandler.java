package server.controller.handle;

import server.controller.GameServerController;
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
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable, IClientHandler {
    private GameServerController gameServerController;
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
    private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Black", "White", "Pink", "Gray", "Cyan", "Magenta"));
    private int score = 0;
    private int round = 0;
    private ArrayList<String> selectedColors = new ArrayList<>();
    private boolean myClientEndMatch;
    private boolean opponentEndMatch;
    private ClientHandler myClient;
    private ClientHandler opponentClient;
    private User opponentUser;
    private int matchId = -1;
    // Danh sách các client đang kết nối
    private static List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket, LoginServerController loginController,
                         RegisterServerController registerController,
                         UserServerController userController,
                         ServerView serverView, AtomicInteger clientIdCounter, GameServerController gameServerController) {
        this.clientSocket = socket;
        this.loginController = loginController;
        this.registerController = registerController;
        this.userController = userController;
        this.serverView = serverView;
        this.clientIdCounter = clientIdCounter;
        this.gameServerController = gameServerController;
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
                    System.out.println("Received message from client: " + message);
                    if (message.startsWith(Constants.ACTION_INVITE + ":")) {
                        handleInvite(message);
                    } else if (message.startsWith(Constants.ACTION_INVITE_RESPONSE + ":")) {
                        handleInviteResponse(message);
                    } else if(message.startsWith(Constants.ACTION_START_GAME)){
                        sendColorsToClient();
                    } else if (message.startsWith(Constants.ACTION_SEND_COLORS)){
                        sendResultToClient(message);
                    } else if (message.startsWith(Constants.ACTION_EXIT_MID_GAME)){
                        exitMidGame();
                    } else if (message.startsWith(Constants.ACTION_FINISH_GAME)){
                        sendMessage(Constants.RESPONSE_FINISH_GAME);
                    }
                    else if (message.startsWith(Constants.ACTION_GET_HISTORY)) {
                        // Nếu nhận được yêu cầu lấy lịch sử đấu
                        sendMatchHistory(message);
                    }else {
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
//                senderHandler.sendMessage(Constants.RESPONSE_INVITE_RESPONSE + ":" + user.getUserName() + ":" + response);

                if (response.equals("ACCEPT")) {
                    if (!gameStartedWithOpponent && !senderHandler.gameStartedWithOpponent) {
                        userController.updateUserStatus(user.getUserName(), Constants.STATUS_PLAYING);
                        userController.updateUserStatus(senderUsername, Constants.STATUS_PLAYING);

                        matchId = gameServerController.createMatch(user.getUserName(), senderUsername);
                        senderHandler.matchId = matchId;

                        System.out.println("Updated status to PLAYING for both " + user.getUserName() + " and " + senderUsername);
                        serverView.showMessage("Updated status to PLAYING for both " + user.getUserName() + " and " + senderUsername);

                        senderHandler.sendMessage(Constants.RESPONSE_GAME_START + ":" + user.getUserName());
                        sendMessage(Constants.RESPONSE_GAME_START + ":" + senderUsername);

                        System.out.println("Game started between " + user.getUserName() + " and " + senderUsername);
                        serverView.showMessage("Game started between " + user.getUserName() + " and " + senderUsername);

                        gameStartedWithOpponent = true;
                        senderHandler.gameStartedWithOpponent = true;

                        // Lưu thông tin người chơi vào phòng chơi
                        this.myClient = getClientHandler(user.getUserName());
                        this.opponentClient = senderHandler;
                        this.opponentClient.myClient = getClientHandler(senderUsername);
                        this.opponentClient.opponentClient = this.myClient;
                        this.opponentClient.opponentClient = getClientHandler(user.getUserName());

                        // Lưu thông tin người chơi đối thủ
                        this.opponentUser = userController.getUserByUsername(senderUsername);
                        this.opponentClient.opponentUser = userController.getUserByUsername(user.getUserName());

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
        this.selectedColors.clear();
        this.selectedColors.addAll(uniqueColors);
        return String.join(",", uniqueColors);
    }

    // Phương thức kiểm tra màu
    public String checkColors(ArrayList<String> colors) {
        ArrayList<String> duplicateColors = new ArrayList<>(this.selectedColors);
        duplicateColors.retainAll(colors);
        return (colors.size() == 3 && this.selectedColors.containsAll(colors)) + ":" + duplicateColors.size();
    }

    public void sendColorsToClient() {

        System.out.println("Random 3 colors for " + user.getUserName());
        serverView.showMessage("Random 3 colors for " + user.getUserName());

        String randomColors = random3Color(); //Thực hiện chọn 3 màu ngẫu nhiên

        sendMessage(Constants.RESPONSE_RANDOM_COLORS + ":" + randomColors + ":" + this.round); // Gửi 3 màu ngẫu nhiên tới client

        System.out.println("Sending colors to client: " + user.getUserName());
        serverView.showMessage("Sending colors to client: " + user.getUserName());

        this.round++; // Tăng số round lên 1

        System.out.println("send color to client: " + this.round);
    }

    public void sendResultToClient(String message) {

        System.out.println("Server received color: " + message);
        boolean send = true;
        // Xử lý kết quả từ client
        String result = (message.split(":").length > 1) ? message.split(":")[1] : "";
        ArrayList<String> resultColors = new ArrayList<>(Arrays.asList(result.split(",")));
        String resultChecking = checkColors(resultColors);
        boolean check = Boolean.parseBoolean(resultChecking.split(":")[0]);
        int scoreAchieve = Integer.parseInt(resultChecking.split(":")[1]);
        sendMessage(
                Constants.RESPONSE_GAME_RESULT
                        + ":"
                        + check
                        + ":"
                        + scoreAchieve
        );
        opponentClient.sendMessage(Constants.RESPONSE_GET_ENEMY_SCORE_THIS_ROUND + ":" + scoreAchieve);
        this.score+=scoreAchieve;
        if(send) {


            this.gameServerController.addRound(
                    matchId,
                    round,  // Current round number
                    String.join(",", myClient.selectedColors),  // Player's color choice
                    String.join(",", opponentClient.selectedColors),  // Opponent's color choice
                    myClient.getScore(),  // Player's score in this round
                    opponentClient.getScore()  // Opponent's score in this round
            );
            send = false;
        }



        // Nếu chưa đủ 5 rounds sẽ thực hiện chơi tiếp
        if(this.round == 3) {
            actionEndGame();
        } else {
            sendColorsToClient();
        }
    }

    // Phương thức lấy điểm
    public int getScore() {
        return this.score;
    }

    // Phương thức cập nhật thông tin client sau khi kết thúc trận đấu
    public void resetStatus() {
        // Cập nhật trạng thái người dùng thành online
        userController.updateUserStatus(user.getUserName(), Constants.STATUS_ONLINE);
        userController.updateUserStatus(opponentUser.getUserName(), Constants.STATUS_ONLINE);
        opponentClient.gameStartedWithOpponent = false;
        gameStartedWithOpponent = false;

        // Set lại thông tin người chơi sau khi kết thúc trận đấu
        this.opponentClient.score = 0;
        this.opponentClient.round = 0;
        this.score = 0;
        this.round = 0;

        //Xoá thông tin người chơi khỏi phòng chơi
        this.myClient = null;
        this.opponentClient.myClient = null;
        this.opponentClient.opponentClient = null;
        this.opponentClient = null;
    }

    // Phương thức thoát khỏi trận đấu giữa chừng
    public void exitMidGame() {
        try {
            //Gửi kết quả trận đấu cho người chơi
            sendMessage(Constants.RESPONSE_EXIT_MIDDLE_GAME + ":" + "LOSE");
            opponentClient.sendMessage(Constants.RESPONSE_EXIT_MIDDLE_GAME + ":" + "WIN");

            //Cập nhật điểm người chơi
            gameServerController.updateScorePlayer(user.getUserName(), this.score);
//            gameServerController.updateScorePlayer(opponentUser.getUserName(), opponentClient.getScore());

            //Cập nhật trạng thái người chơi
            resetStatus();

            broadcastOnlineUsers();
        } catch (Exception e) {
            serverView.showMessage("Error exiting mid-game: " + e.getMessage());
        }
    }

    // Phương thức kết thúc trận đấu giữa 2 người chơi
    public void actionEndGame() {
        // Cập nhật trạng thái kết thúc trận đấu cho từng người chơi
        this.myClientEndMatch = true;
        opponentClient.opponentEndMatch = true;

        //Khi cả 2 người chơi đã nộp kết quả thì thực hiện xử lí kết quả trận đấu
        if (this.opponentEndMatch && this.myClientEndMatch) {
            boolean n1 = true;
            boolean n2 = true;
            String result;

            if (this.score > opponentClient.getScore()) {
                //Gửi kết quả trận đấu cho người chơi
                sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "WIN" + ":" + this.opponentClient.getScore());
                this.opponentClient.sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "LOSE" + ":" + this.score);
                result = "WIN";
                gameServerController.incrementWinCount(user.getUserName());
                gameServerController.incrementLossCount(opponentUser.getUserName());

            } else if (this.score < opponentClient.getScore()) {
                //Gửi kết quả trận đấu cho người chơi
                sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "LOSE" + ":" + this.opponentClient.getScore());
                this.opponentClient.sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "WIN" + ":" + this.score);
                result = "LOSE";
                gameServerController.incrementLossCount(user.getUserName());
                gameServerController.incrementWinCount(opponentUser.getUserName());

            } else {
                //Gửi kết quả trận đấu cho người chơi
                sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "DRAW" + ":" + this.opponentClient.getScore());
                this.opponentClient.sendMessage(Constants.RESPONSE_MATCH_RESULT + ":" + "DRAW" + ":" + this.score);
                result = "DRAW";
                gameServerController.incrementDrawCount(user.getUserName());
                gameServerController.incrementDrawCount(opponentUser.getUserName());

            }

            gameServerController.finalizeMatch(
                    matchId,
                    this.score,
                    opponentClient.getScore(),
                    result
            );
            if(n1){
                gameServerController.updateScorePlayer(user.getUserName(), this.score + user.getScore());
                n1 = false;
            }
            if(n2){
                gameServerController.updateScorePlayer1(opponentUser.getUserName(), opponentClient.getScore() + opponentUser.getScore());
                n2 = false;
            }
            resetStatus();
            broadcastOnlineUsers();

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

    private void sendMatchHistory(String message) {
        try {
            String[] parts = message.split(":");


                String action = parts[0];
                String username = parts[1];


                System.out.println("Action: " + action);
                System.out.println("Username: " + username);

            // Lấy lịch sử đấu từ GameServerController
            List<Map<String, Object>> matchHistory = gameServerController.getMatchHistoryByUsername(username);

            // Gửi dữ liệu lịch sử đấu đến client
            oos.writeObject(matchHistory);
            oos.flush();

            serverView.showMessage("Sent match history to client: " + user.getUserName());
        } catch (IOException e) {
            serverView.showMessage("Error sending match history to client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
