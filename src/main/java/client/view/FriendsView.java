package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import client.utils.Constants;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import server.model.User;

import java.util.List;

public class FriendsView extends Application {
    private ClientControl clientControl;
    private GameController gameController;
    private ObservableList<User> userListModel;
    private ListView<User> userList;  // Sử dụng ListView thay vì JList
    private Button inviteButton;

    public FriendsView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        this.userListModel = FXCollections.observableArrayList();
        setupUI();
        updateUserList((List<User>) userData);  // Cập nhật danh sách người chơi ban đầu
        listenFromServer();  // Bắt đầu lắng nghe các tin nhắn từ server
    }

    // Tạo giao diện chính
    private void setupUI() {
        Stage stage = new Stage();
        stage.setTitle("Online Players");

        userList = new ListView<>(userListModel);
        userList.setCellFactory(param -> new UserListCellRenderer()); // Sử dụng renderer tùy chỉnh

        inviteButton = new Button("Invite to Play");
        inviteButton.setOnAction(e -> {
            User selectedUser = userList.getSelectionModel().getSelectedItem();  // Lấy đối tượng User
            if (selectedUser != null) {
                sendInvite(selectedUser.getUserName());  // Gửi lời mời tới người chơi được chọn
            } else {
                showAlert("Please select a player to invite.");
            }
        });

        BorderPane root = new BorderPane();
        root.setCenter(userList);
        root.setBottom(inviteButton);

        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Cập nhật danh sách người chơi
    public void updateUserList(List<User> users) {
        userListModel.clear();
        for (User user : users) {
            if (!user.getUserName().equals(clientControl.getCurrentUser().getUserName())) {
                userListModel.add(user);
            }
        }
    }

    // Gửi lời mời chơi
    private void sendInvite(String recipient) {
        String message = Constants.ACTION_INVITE + ":" + recipient;
        clientControl.sendMessage(message);
    }

    // Lắng nghe từ server
    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        if (message.startsWith(Constants.RESPONSE_INVITE)) {
                            handleInvite(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE_RESPONSE)) {
                            handleInviteResponse(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_START)) {
                            handleGameStart(message);
                        }
                    } else if (obj instanceof List) {
                        // Cập nhật danh sách người chơi online
                        @SuppressWarnings("unchecked")
                        List<User> users = (List<User>) obj;
                        updateUserList(users);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Xử lý khi nhận được lời mời chơi từ người khác
    private void handleInvite(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String recipient = parts[1]; // Tên người nhận (người gửi phản hồi)
            String response = parts[2];  // Phản hồi (ACCEPT hoặc DECLINE)

            // Xử lý phản hồi dựa trên nội dung
            if (response.equalsIgnoreCase("ACCEPT")) {
                showAlert(recipient + " has accepted your invitation. The game will start!");
                gameController.startGame(recipient); // Bắt đầu trò chơi
            } else if (response.equalsIgnoreCase("DECLINE")) {
                showAlert(recipient + " has declined your invitation.");
            }
        } else {
            String sender = message.split(":")[1];
            boolean response = showConfirmation(sender + " invites you to a game. Do you accept?");
            String responseMessage = Constants.ACTION_INVITE_RESPONSE + ":" + sender + ":" + (response ? "ACCEPT" : "DECLINE");
            clientControl.sendMessage(responseMessage);
        }
    }

    // Xử lý phản hồi lời mời
    private void handleInviteResponse(String message) {
        String[] parts = message.split(":");
        String recipient = parts[1];
        String response = parts[2];
        if (response.equals("ACCEPT")) {
            showAlert(recipient + " has accepted your invitation. The game will start!");
            gameController.startGame(recipient);
        } else {
            showAlert(recipient + " has declined your invitation.");
        }
    }

    // Xử lý khi trò chơi bắt đầu
    private void handleGameStart(String message) {
        String opponent = message.split(":")[1];
        showAlert("Starting game with " + opponent + "!");
        gameController.startGame(opponent);
    }

    // Hiển thị thông báo
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hiển thị hộp thoại xác nhận
    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // Renderer tùy chỉnh để hiển thị tên và trạng thái người dùng
    private class UserListCellRenderer extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                String statusText = user.getStatus() == 1 ? "Online" : user.getStatus() == 2 ? "Playing" : "Offline";
                setText(user.getUserName() + " (" + statusText + ")");
                setStyle(user.getStatus() == 1 ? "-fx-text-fill: green;" : user.getStatus() == 2 ? "-fx-text-fill: blue;" : "-fx-text-fill: gray;");
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Chỉ để thực hiện ứng dụng JavaFX, không sử dụng trong MainView
    }

    public static void main(String[] args) {
        launch(args);
    }
}