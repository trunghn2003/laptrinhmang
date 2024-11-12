package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import server.model.User;
import client.utils.Constants;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.List;

public class MainView extends Application {
    private ClientControl clientControl;
    private GameController gameController;
    private ObservableList<User> userListModel;
    private ListView<User> userList;  // Sử dụng ListView thay vì JList
    private Button playButton;

    // Chat components
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JButton sendChatButton;
    private User selectedChatUser;

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        this.userListModel = FXCollections.observableArrayList();
        updateUserList((List<User>) userData);
        setupUI();
    }

    // Tạo giao diện chính
    private void setupUI() {
        Stage stage = new Stage();
        stage.setTitle("Đoán Màu");

        BorderPane root = new BorderPane();

        // Cột 1 và 2 gộp thành 1
        VBox leftColumn = new VBox();
        leftColumn.setSpacing(20); // Khoảng cách giữa các phần tử
        leftColumn.setAlignment(javafx.geometry.Pos.CENTER);

        // Thêm logo
        Image logoImage = new Image("/assets/home-logo.png"); // Đường dẫn đến hình ảnh logo
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(650); // Đặt chiều rộng cho logo
        logo.setPreserveRatio(true); // Giữ tỷ lệ cho logo

        Region spacer = new Region();
        spacer.setPrefHeight(100);

        // Tạo Text với hiệu ứng bóng đổ
        Text text = new Text("Play Now");
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 18px;");

        // Thêm stroke cho Text
        text.setStroke(Color.web("#9B6B27")); // Đặt màu stroke (ví dụ: màu đen)
        text.setStrokeWidth(1); // Đặt độ rộng stroke (ví dụ: 1 pixel)

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Sử dụng renderer tùy chỉnh để hiển thị tên và trạng thái người chơi
        userList.setCellRenderer(new UserListCellRenderer());

        playButton.setOnAction(e -> {
            playNow();
        });

        leftColumn.getChildren().addAll(logo, spacer, playButton);

        // Cột 3
        VBox rightColumn = new VBox();
        rightColumn.setSpacing(20); // Khoảng cách giữa các phần tử
//        rightColumn.setAlignment(Pos.CENTER);

        // Thêm hình ảnh từ asset
        Image leaderboardHeader = new Image("/assets/leaderboard.png");
        ImageView leaderboardHeaderImage = new ImageView(leaderboardHeader);

        userList = new ListView<>(userListModel);
        userList.setCellFactory(param -> new UserListCellRenderer()); // Sử dụng renderer tùy chỉnh
        userList.setPrefWidth(300);
        userList.setBackground(new Background(new BackgroundFill(Color.web("#ADD8E6"), CornerRadii.EMPTY, Insets.EMPTY)));

        rightColumn.getChildren().addAll(leaderboardHeaderImage, userList);
        rightColumn.setStyle("-fx-background-color: #453221;");
        rightColumn.setPrefWidth(300);
        rightColumn.setPadding(new Insets(30, 20, 10, 20));

        //add id cho rightColumn
        rightColumn.setId("rightColumn");


        // Thêm các cột vào root
        HBox hbox = new HBox();
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        hbox.getChildren().addAll(leftSpacer, leftColumn, rightColumn, rightSpacer); // Thêm các phần tử vào HBox
        hbox.setSpacing(20); // Khoảng cách giữa các phần tử
        hbox.setStyle("-fx-padding: 20;");
        root.setCenter(hbox); // Đặt HBox vào giữa

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/home-styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // Hiển thị thông báo
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        // Chỉ để thực hiện ứng dụng JavaFX, không sử dụng trong MainView
    }

    public static void main(String[] args) {
        launch(args);
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

                //Space
                Region space = new Region();
                space.setPrefWidth(5);

                // Create Label for the username
                Label userInfo = new Label(user.getUserName());
                userInfo.setStyle("-fx-text-fill: white;"); // Set text color

                // Create a spacer Region
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Make spacer grow horizontally

                // Create Label for the score
                Label scoreInfo = new Label(user.getScore() + " points");
                scoreInfo.setStyle("-fx-text-fill: white;"); // Set text color

                // Add avatar, username, spacer, and score to HBox
                hbox.getChildren().addAll(avatar, space, userInfo, spacer, scoreInfo);

                // Set background color for HBox (optional)
                hbox.setStyle("-fx-background-color: #453221;");

                //set background when hover
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

    public void updateUserList(List<User> users) {
        userListModel.clear();
        System.out.println("Updating user list with " + users.size() + " users.");
        // Sắp xếp danh sách người chơi theo số điểm
        users.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore())); // Sắp xếp giảm dần
        for (User user : users) {
            userListModel.add(user);
        }
    }

    //Play now
    public void playNow() {
        Stage stage = (Stage) playButton.getScene().getWindow();
        double xPos = stage.getX();
        double yPos = stage.getY();
        stage.hide();
        FriendsView friendsView = new FriendsView(clientControl, userListModel);
        Stage friendsStage = friendsView.getStage();
        friendsStage.setX(xPos);
        friendsStage.setY(yPos);
    }

    //get stage
    public Stage getStage() {
        return (Stage) playButton.getScene().getWindow();
    // Send chat message
    private void sendChatMessage() {
        String messageText = chatInputField.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }
        if (selectedChatUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to chat with.");
            return;
        }
        String recipientUsername = selectedChatUser.getUserName();
        String message = Constants.ACTION_CHAT_MESSAGE + ":" + recipientUsername + ":" + messageText;
        clientControl.sendMessage(message);

        // Display the message in the chat area
        chatArea.append("Me: " + messageText + "\n");

        // Clear the input field
        chatInputField.setText("");
    }

    // Listen for messages from the server
    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        System.out.println("Received message: " + message);
                        if (message.startsWith(Constants.RESPONSE_CHAT_MESSAGE)) {
                            handleChatMessage(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE)) {
                            handleInvite(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE_RESPONSE)) {
                            handleInviteResponse(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_START)) {
                            handleGameStart(message);
                        } else if (message.startsWith(Constants.RESPONSE_RANDOM_COLORS)) {
                            gameController.receivedColors(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_RESULT)) {
                            gameController.receiveGameResult(message);
                        } else if (message.startsWith(Constants.RESPONSE_EXIT_MIDDLE_GAME)) {
                            System.out.println("Exit mid-game");
                        } else if (message.startsWith(Constants.RESPONSE_MATCH_RESULT)) {
                            gameController.receivedMatchResult(message);
                        } else {
                            // Handle other messages
                        }
                    } else if (obj instanceof List) {
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
}

    // Handle incoming chat messages
    private void handleChatMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 3) {
            String senderUsername = parts[1];
            String chatContent = parts[2];
            chatArea.append(senderUsername + ": " + chatContent + "\n");
        } else {
            System.out.println("Invalid chat message format: " + message);
        }
    }

    // Handle game invite
    private void handleInvite(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String recipient = parts[1]; // Tên người nhận (người gửi phản hồi)
            String response = parts[2];  // Phản hồi (ACCEPT hoặc DECLINE)
            // Xử lý phản hồi dựa trên nội dung
            if (response.equalsIgnoreCase("ACCEPT")) {
                JOptionPane.showMessageDialog(null, recipient + " has accepted your invitation. The game will start!");
                gameController.startGame(recipient); // Bắt đầu trò chơi
            } else if (response.equalsIgnoreCase("DECLINE")) {
                JOptionPane.showMessageDialog(null, recipient + " has declined your invitation.");
            }
        } else {
            // Log lỗi nếu thông điệp không đủ phần tử sau khi tách
            String sender = message.split(":")[1];
            int response = JOptionPane.showConfirmDialog(null, sender + " invites you to a game. Do you accept?", "Game Invitation", JOptionPane.YES_NO_OPTION);
            String responseMessage = Constants.ACTION_INVITE_RESPONSE + ":" + sender + ":" + (response == JOptionPane.YES_OPTION ? "ACCEPT" : "DECLINE");
            clientControl.sendMessage(responseMessage);
        }
    }

    // Handle invite response
    private void handleInviteResponse(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String recipient = parts[1];
            String response = parts[2];
            if (response.equalsIgnoreCase("ACCEPT")) {
                JOptionPane.showMessageDialog(null, recipient + " has accepted your invitation. The game will start!");
                gameController.startGame(recipient);
            } else {
                JOptionPane.showMessageDialog(null, recipient + " has declined your invitation.");
            }
        } else {
            System.out.println("Invalid invite response format: " + message);
        }
    }

    // Handle game start
    private void handleGameStart(String message) {
        String opponent = message.split(":")[1];
        JOptionPane.showMessageDialog(null, "Starting game with " + opponent + "!");
        gameController.startGame(opponent);
        this.setVisible(false);
    }
}

// Custom renderer for displaying user names and statuses
class UserListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof User) {
            User user = (User) value;
            // Display user name and status
            String statusText = user.getStatus() == 1 ? "Online"
                    : user.getStatus() == 2 ? "Playing" : "Offline";
            label.setText("<html><b>" + user.getUserName() + "</b> (" + statusText + ")</html>");

            // Customize color based on user status
            if (user.getStatus() == 1) {
                label.setForeground(new Color(0, 128, 0));  // Green for Online
            } else if (user.getStatus() == 2) {
                label.setForeground(new Color(0, 0, 255));  // Blue for Playing
            } else {
                label.setForeground(new Color(128, 128, 128));  // Gray for Offline
            }

            // Add padding between items
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        if (isSelected) {
            label.setBackground(new Color(200, 230, 255));  // Background color when selected
        }

        return label;
    }
}
