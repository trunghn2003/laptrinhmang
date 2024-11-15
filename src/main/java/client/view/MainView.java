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
    private Button settingsButton;

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

        // Tạo nút cài đặt
        settingsButton = new Button();
        ImageView settingsIcon = new ImageView(new Image("/assets/setting-btn.png"));
        settingsIcon.setFitWidth(35);
        settingsIcon.setFitHeight(35);
        settingsButton.setGraphic(settingsIcon);
        settingsButton.getStyleClass().add("settings-button");

        // Tạo container cho nút cài đặt
        StackPane topRight = new StackPane(settingsButton);
        StackPane.setAlignment(settingsButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(settingsButton, new Insets(10, 10, 0, 0));

        // Thêm style cho nút
        settingsButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        // Thêm hiệu ứng hover
        settingsButton.setOnMouseEntered(e -> {
            settingsButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-cursor: hand;"
            );
        });

        settingsButton.setOnMouseExited(e -> {
            settingsButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-cursor: hand;"
            );
        });

        // Xử lý sự kiện click
        settingsButton.setOnAction(e -> {
            openSettings();
        });

        // Đặt nút cài đặt vào vị trí top của BorderPane
        root.setTop(topRight);

        // Cột 1 và 2 gộp thành 1
        VBox leftColumn = new VBox();
        leftColumn.setSpacing(20);
        leftColumn.setAlignment(javafx.geometry.Pos.CENTER);

        // Thêm logo
        Image logoImage = new Image("/assets/home-logo.png");
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(420);
        logo.setPreserveRatio(true);
        logo.setTranslateY(-40);

        Region spacer = new Region();
        spacer.setPrefHeight(100);

        // Tạo Text với hiệu ứng bóng đổ
        Text text = new Text("Play Now");
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 18px;");

        // Thêm stroke cho Text
        text.setStroke(Color.web("#9B6B27"));
        text.setStrokeWidth(1);

        DropShadow ds = new DropShadow();
        ds.setOffsetY(2.0);
        ds.setColor(Color.web("#A37029"));
        ds.setRadius(0);
        text.setEffect(ds);

        // Thêm nút
        playButton = new Button();
        playButton.setGraphic(text);
        playButton.setPrefWidth(250);
        playButton.setPrefHeight(60);

        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.web("#A37029"));
        buttonShadow.setRadius(1);
        playButton.setEffect(buttonShadow);

        playButton.setOnAction(e -> {
            playNow();
        });

        leftColumn.getChildren().addAll(logo, spacer, playButton);

        // Cột 3
        VBox rightColumn = new VBox();
        rightColumn.setSpacing(20);

        Image leaderboardHeader = new Image("/assets/leaderboard.png");
        ImageView leaderboardHeaderImage = new ImageView(leaderboardHeader);
        leaderboardHeaderImage.setFitWidth(270);
        leaderboardHeaderImage.setPreserveRatio(true);

        userList = new ListView<>(userListModel);
        userList.setCellFactory(param -> new UserListCellRenderer());
        userList.setPrefWidth(300);
        userList.setBackground(new Background(new BackgroundFill(Color.web("#ADD8E6"), CornerRadii.EMPTY, Insets.EMPTY)));

        rightColumn.getChildren().addAll(leaderboardHeaderImage, userList);
        rightColumn.setStyle("-fx-background-color: #453221;");
        rightColumn.setPrefWidth(300);
        rightColumn.setPadding(new Insets(30, 20, 10, 20));
        rightColumn.setId("rightColumn");

        // Thêm các cột vào root
        HBox hbox = new HBox();
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        Region centerSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);

        hbox.getChildren().addAll(leftColumn, centerSpacer, rightColumn);
        hbox.setSpacing(20);
        hbox.setStyle("-fx-padding: 20;");
        root.setCenter(hbox);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/home-styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void openSettings() {
        SettingsModal settingsModal = new SettingsModal((Stage) playButton.getScene().getWindow(), clientControl);
        settingsModal.show();
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
                //Thêm padding y cho hbox
                hbox.setPadding(new Insets(5, 0, 5, 0));
                hbox.setSpacing(5); // Spacing between elements
                hbox.setAlignment(Pos.CENTER_LEFT); // Center vertically, align left horizontally

                ImageView avatar = new ImageView(new Image("/assets/avatar/avt1.png")); // Path to avatar
                avatar.setFitWidth(40); // Set width for avatar
                avatar.setFitHeight(40); // Set height for avatar

                //Space
                Region space = new Region();
                space.setPrefWidth(5);

                // Create Label for the username
                Label userInfo = new Label(user.getUserName());
                userInfo.setStyle("-fx-text-fill: white; -fx-font-family: '';-fx-font-weight: bold;"); // Set text color

                // Create a spacer Region
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Make spacer grow horizontally

                // Tạo HBox để chứa score và icon
                HBox scoreBox = new HBox(5); // spacing 5 pixels
                scoreBox.setAlignment(Pos.CENTER);

                // Label cho điểm số
                Label scoreInfo = new Label(String.valueOf(user.getScore()));
                scoreInfo.setStyle("-fx-text-fill: #FFAF40;");

                // Tạo ImageView cho icon point
                ImageView pointIcon = new ImageView(new Image("/assets/coin.png")); // Thay đường dẫn icon của bạn
                pointIcon.setFitWidth(20); // Điều chỉnh kích thước icon
                pointIcon.setFitHeight(20);

                // Thêm score và icon vào scoreBox
                scoreBox.getChildren().addAll(scoreInfo, pointIcon);


                // Add avatar, username, spacer, and score to HBox
                hbox.getChildren().addAll(avatar, space, userInfo, spacer, scoreBox);

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
        FriendsView friendsView = new FriendsView(clientControl,userListModel);
        Stage friendsStage = friendsView.getStage();
        friendsStage.setX(xPos);
        friendsStage.setY(yPos);
    }

    //get stage
    public Stage getStage() {
        return (Stage) playButton.getScene().getWindow();
    }
}