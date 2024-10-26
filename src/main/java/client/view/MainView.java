package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
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

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        this.userListModel = FXCollections.observableArrayList();
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

        // Thêm nút
        playButton = new Button("Play Now");
        playButton.setPrefWidth(250);
        playButton.setPrefHeight(60);
        playButton.setOnAction(e -> {
            // Logic cho nút mời
            showAlert("Play button clicked!");
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
                String statusText = user.getStatus() == 1 ? "Online" : user.getStatus() == 2 ? "Playing" : "Offline";
                setText(user.getUserName() + " (" + statusText + ")");
                setStyle(user.getStatus() == 1 ? "-fx-text-fill: green;" : user.getStatus() == 2 ? "-fx-text-fill: blue;" : "-fx-text-fill: gray;");
            }
        }
    }
}