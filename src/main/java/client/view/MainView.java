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
    private ListView<User> userList;
    private Button playButton;

    // Chat components
    private TextArea chatArea;
    private TextField chatInputField;
    private Button sendChatButton;
    private User selectedChatUser;

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        this.userListModel = FXCollections.observableArrayList();
        updateUserList((List<User>) userData);
        setupUI();
    }

    // Main UI setup
    private void setupUI() {
        Stage stage = new Stage();
        stage.setTitle("Đoán Màu");

        BorderPane root = new BorderPane();

        // Left column
        VBox leftColumn = new VBox();
        leftColumn.setSpacing(20);
        leftColumn.setAlignment(Pos.CENTER);

        // Logo
        Image logoImage = new Image("/assets/home-logo.png");
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(650);
        logo.setPreserveRatio(true);

        Region spacer = new Region();
        spacer.setPrefHeight(100);

        // Play Button
        playButton = new Button("Play Now");
        playButton.setOnAction(e -> playNow());

        leftColumn.getChildren().addAll(logo, spacer, playButton);

        // Right column for user list
        VBox rightColumn = new VBox();
        rightColumn.setSpacing(20);
        Image leaderboardHeader = new Image("/assets/leaderboard.png");
        ImageView leaderboardHeaderImage = new ImageView(leaderboardHeader);

        userList = new ListView<>(userListModel);
        userList.setCellFactory(param -> new UserListCellRenderer());
        userList.setPrefWidth(300);
        userList.setBackground(new Background(new BackgroundFill(Color.web("#ADD8E6"), CornerRadii.EMPTY, Insets.EMPTY)));

        rightColumn.getChildren().addAll(leaderboardHeaderImage, userList);
        rightColumn.setStyle("-fx-background-color: #453221;");
        rightColumn.setPrefWidth(300);
        rightColumn.setPadding(new Insets(30, 20, 10, 20));
        rightColumn.setId("rightColumn");

        // Chat components
        VBox chatBox = new VBox();
        chatBox.setSpacing(10);
        chatBox.setPadding(new Insets(10));
        chatBox.setStyle("-fx-background-color: #333333;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(200);

        chatInputField = new TextField();
        chatInputField.setPromptText("Enter your message...");

        sendChatButton = new Button("Send");
        sendChatButton.setOnAction(e -> sendChatMessage());

        HBox chatInputBox = new HBox(10, chatInputField, sendChatButton);
        chatInputBox.setAlignment(Pos.CENTER_RIGHT);

        chatBox.getChildren().addAll(new Label("Chat"), chatArea, chatInputBox);

        // Add columns and chat to main layout
        HBox hbox = new HBox();
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        hbox.getChildren().addAll(leftSpacer, leftColumn, rightColumn, chatBox, rightSpacer);
        hbox.setSpacing(20);
        hbox.setStyle("-fx-padding: 20;");
        root.setCenter(hbox);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/home-styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        // Launch JavaFX application
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Renderer for user list
    private class UserListCellRenderer extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.setAlignment(Pos.CENTER_LEFT);

                ImageView avatar = new ImageView(new Image("/assets/avatar/avt1.png"));
                avatar.setFitWidth(50);
                avatar.setFitHeight(50);

                Label userInfo = new Label(user.getUserName());
                userInfo.setStyle("-fx-text-fill: white;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label scoreInfo = new Label(user.getScore() + " points");
                scoreInfo.setStyle("-fx-text-fill: white;");

                hbox.getChildren().addAll(avatar, userInfo, spacer, scoreInfo);
                hbox.setStyle("-fx-background-color: #453221;");

                hbox.setOnMouseEntered(e -> hbox.setStyle("-fx-background-color: #5A3C29;"));
                hbox.setOnMouseExited(e -> hbox.setStyle("-fx-background-color: #453221;"));

                setGraphic(hbox);
            }
        }
    }

    public void updateUserList(List<User> users) {
        userListModel.clear();
        users.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));
        userListModel.addAll(users);
    }

    private void playNow() {
        Stage stage = (Stage) playButton.getScene().getWindow();
        double xPos = stage.getX();
        double yPos = stage.getY();
        stage.hide();
        FriendsView friendsView = new FriendsView(clientControl, userListModel);
        Stage friendsStage = friendsView.getStage();
        friendsStage.setX(xPos);
        friendsStage.setY(yPos);
    }

    private void sendChatMessage() {
        String messageText = chatInputField.getText().trim();
        if (messageText.isEmpty() || selectedChatUser == null) return;

        String recipientUsername = selectedChatUser.getUserName();
        String message = Constants.ACTION_CHAT_MESSAGE + ":" + recipientUsername + ":" + messageText;
        clientControl.sendMessage(message);

        chatArea.appendText("Me: " + messageText + "\n");
        chatInputField.clear();
    }

    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        if (message.startsWith(Constants.RESPONSE_CHAT_MESSAGE)) {
                            handleChatMessage(message);
                        }
                        // Other conditions...
                    } else if (obj instanceof List) {
                        updateUserList((List<User>) obj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleChatMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 3) {
            String senderUsername = parts[1];
            String chatContent = parts[2];
            chatArea.appendText(senderUsername + ": " + chatContent + "\n");
        }
    }
    public Stage getStage() {
        return (Stage) playButton.getScene().getWindow();
    }
}
