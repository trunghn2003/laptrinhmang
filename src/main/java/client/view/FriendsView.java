package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import client.utils.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import server.model.User;

import java.util.List;

public class FriendsView extends Application {
    private ClientControl clientControl;
    private GameController gameController;
    private ObservableList<User> userListModel;
    private ListView<User> userList;  // Sử dụng ListView thay vì JList
    private Button inviteButton;
    private Button backButton;

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

        Image leaderboardHeader = new Image("/assets/friend-list.png");
        ImageView leaderboardHeaderImage = new ImageView(leaderboardHeader);

        // Khởi tạo userList
        userList = new ListView<>(userListModel);
        userList.setCellFactory(param -> new UserListCellRenderer()); // Sử dụng renderer tùy chỉnh
        userList.setPrefWidth(700); // Đặt chiều rộng mong muốn là 700
        userList.setMaxWidth(700);  // Đảm bảo chiều rộng tối đa là 700

        // Thiết lập chiều cao nếu cần
        // userList.setPrefHeight(500);

        // Tạo VBox chứa leaderboardHeaderImage và userList
        VBox vbox = new VBox(10); // Thêm khoảng cách giữa các phần tử
        vbox.getChildren().addAll(leaderboardHeaderImage, userList);
        vbox.setStyle("-fx-background-color: #453221;");
        vbox.setPadding(new Insets(30, 20, 10, 20));
        vbox.setPrefWidth(700);
        vbox.setMaxWidth(700);
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setId("centerPanel");

        // Tạo nút mời
        inviteButton = new Button("Invite to Play");
        inviteButton.setOnAction(e -> {
            User selectedUser = userList.getSelectionModel().getSelectedItem();  // Lấy đối tượng User
            if (selectedUser != null) {
                sendInvite(selectedUser.getUserName());  // Gửi lời mời tới người chơi được chọn
            } else {
                showAlert("Please select a player to invite.");
            }
        });

        // Tạo nút "Back"
        backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Xử lý sự kiện khi nhấn nút "Back"
            backToMainView();
        });

        // Tạo HBox để chứa nút "Back"
        HBox topPane = new HBox();
        topPane.getChildren().add(backButton);
        topPane.setAlignment(Pos.CENTER_LEFT); // Căn trái
        topPane.setPadding(new Insets(10));

        // Tạo StackPane để căn giữa vbox
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(vbox);
        centerPane.setAlignment(Pos.CENTER);
        StackPane.setAlignment(vbox, Pos.CENTER); // Đảm bảo vbox được căn giữa trong StackPane

        // Đặt chiều rộng tối đa cho centerPane nếu cần
        // centerPane.setMaxWidth(700);

        // Tạo BorderPane và đặt các thành phần vào
        BorderPane root = new BorderPane();
        root.setTop(topPane);
        root.setCenter(centerPane); // Đặt centerPane vào giữa BorderPane
//        root.setBottom(inviteButton);
        BorderPane.setAlignment(inviteButton, Pos.CENTER); // Căn giữa nút mời trong BorderPane

        // Tạo Scene và hiển thị
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/friends-styles.css").toExternalForm());
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
        showAlert("Invitation sent to " + recipient + "!");
    }

    // Lắng nghe từ server
    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        System.out.println("in mainView: " + message);
                        if (message.startsWith(Constants.RESPONSE_INVITE)) {
                            // Gọi handleInvite trên luồng chính
                            Platform.runLater(() -> handleInvite(message));
                        } else if (message.startsWith(Constants.RESPONSE_INVITE_RESPONSE)) {
                            // Gọi handleInviteResponse trên luồng chính
                            Platform.runLater(() -> handleInviteResponse(message));
                        } else if (message.startsWith(Constants.RESPONSE_GAME_START)) {
                            // Gọi handleGameStart trên luồng chính
                            Platform.runLater(() -> handleGameStart(message));
                        } else if (message.startsWith(Constants.RESPONSE_RANDOM_COLORS)) {
                            Platform.runLater(() -> gameController.receivedColors(message));
                        } else if (message.startsWith(Constants.RESPONSE_GAME_RESULT)) {
                            Platform.runLater(() -> gameController.receiveGameResult(message));
                        } else if (message.startsWith(Constants.RESPONSE_EXIT_MIDDLE_GAME)) {
                            System.out.println("EXIT MID GAME");
                        } else if (message.startsWith(Constants.RESPONSE_MATCH_RESULT)) {
                            Platform.runLater(() -> gameController.receivedMatchResult(message));
                        } else {
                            // Xử lý các tin nhắn khác
                        }
                    } else if (obj instanceof List) {
                        // Cập nhật danh sách người chơi online
                        @SuppressWarnings("unchecked")
                        List<User> users = (List<User>) obj;
                        Platform.runLater(() -> updateUserList(users));
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

            // Sử dụng Platform.runLater để đảm bảo hiển thị trên luồng chính
            Platform.runLater(() -> {
                boolean response = showConfirmation(sender + " invites you to a game. Do you accept?");
                String responseMessage = Constants.ACTION_INVITE_RESPONSE + ":" + sender + ":" + (response ? "ACCEPT" : "DECLINE");
                clientControl.sendMessage(responseMessage);
            });
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
                // Create an HBox to contain avatar, name, spacer, and score
                HBox hbox = new HBox();
                hbox.setSpacing(10); // Spacing between elements
                hbox.setAlignment(Pos.CENTER_LEFT); // Center vertically, align left horizontally

                ImageView avatar = new ImageView(new Image("/assets/avatar/avt1.png")); // Path to avatar
                avatar.setFitWidth(50); // Set width for avatar
                avatar.setFitHeight(50); // Set height for avatar

                // Space
                Region space = new Region();
                space.setPrefWidth(5);

                // Create Label for the username
                Label username = new Label(user.getUserName());
                username.setStyle("-fx-text-fill: white;"); // Set text color

                // Tạo 1 label để hiển thị trạng thái online
                String statusText = user.getStatus() == 1 ? "Online" : user.getStatus() == 2 ? "Playing" : "Offline";
                Label status = new Label(statusText);
                // Font size
                status.setId("statusLbl");
                status.setStyle(user.getStatus() == 1 ? "-fx-text-fill: green;" : user.getStatus() == 2 ? "-fx-text-fill: blue;" : "-fx-text-fill: gray;");

                VBox userInfo = new VBox();
                userInfo.getChildren().addAll(username, status);
                userInfo.setAlignment(Pos.CENTER_LEFT);

                // Create a spacer Region
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Make spacer grow horizontally

                // Add avatar, username, spacer, and score to HBox
                hbox.getChildren().addAll(avatar, space, userInfo, spacer);

                if (user.getStatus() == 1) {
                    // If status == 1, display Button on the right
                    Button actionButton = new Button("Invite");
                    actionButton.setStyle("-fx-background-color: #E29A36; -fx-text-fill: white;"); // Set style for button

                    actionButton.setOnAction(e -> {
                        // Gửi lời mời đến người dùng được chọn
                        sendInvite(user.getUserName());
                    });

                    hbox.getChildren().add(actionButton);
                }

                // Set background color for HBox (optional)
                hbox.setStyle("-fx-background-color: #453221;");

                // Set background when hover
                hbox.setOnMouseEntered(e -> {
                    hbox.setStyle("-fx-background-color: #5A3C29;");
                });

                hbox.setOnMouseExited(e -> {
                    hbox.setStyle("-fx-background-color: #453221;");
                });

                // Set the HBox as the graphic for this cell
                setGraphic(hbox);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Chỉ để thực hiện ứng dụng JavaFX, không sử dụng trong MainView
    }

    public void backToMainView() {
        // Xử lý sự kiện khi nhấn nút "Back"
        MainView mainView = new MainView(clientControl, userListModel);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}