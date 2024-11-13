package client.view;

import client.controller.GameController;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameView {
    private GameController gameController;
    private String opponent;
    private Label opponentLabel;
    private Button submitButton;
    private ArrayList<Button> colorButtons;
    private ArrayList<String> selectedColors;
    private Label scoreLabel;
    private int roundCnt = 0;
    private Label timerLabel;
    private int timeRemaining = 30;
    private Timeline timer;
    private FriendsView friendsView;
    private Stage stage;
    private BorderPane root; // Root pane to switch content
    private Scene scene; // Main scene

    public GameView(GameController gameController, FriendsView friendsView) {
        this.gameController = gameController;
        this.friendsView = friendsView;
        this.selectedColors = new ArrayList<>();
        setupUI();
        showColorsFromServer();
//        startTimer();
    }

    private void setupUI() {
        stage = new Stage();
        stage.setTitle("Color Guessing Game");

        root = new BorderPane();
        scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/game-styles.css").toExternalForm());

        opponentLabel = new Label("Opponent: ");
        scoreLabel = new Label("Score: 0");

        stage.show();

        // We will add content to 'root' in startGame() and setupMainUI()
        showColorsFromServer();
    }

    private void showColorsFromServer() {
        // Lấy 3 màu từ server
        List<String> colorsFromServer = gameController.getColors(); // Bạn cần cài đặt phương thức này trong GameController
        System.out.println("Colors from server: " + colorsFromServer);
        // Tạo HBox hiển thị các màu từ server
        HBox colorDisplayBox = new HBox();
        colorDisplayBox.setAlignment(Pos.CENTER);
        colorDisplayBox.setSpacing(20);
        colorDisplayBox.setPadding(new Insets(20));

        for (String color : colorsFromServer) {
            Label colorLabel = new Label();
            colorLabel.setStyle("-fx-background-color: " + color.toLowerCase() + "; -fx-min-width: 200px; -fx-min-height: 200px;");
            colorDisplayBox.getChildren().add(colorLabel);
        }

        // Đặt colorDisplayBox vào trung tâm của root
        root.setCenter(colorDisplayBox);

        // Sau 3 giây, chuyển sang giao diện bảng màu chính
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> {
            // Xóa nội dung hiện tại và thiết lập giao diện chính của game
            root.getChildren().clear();
            setupMainUI();
            startTimer(); // Bắt đầu đồng hồ đếm ngược
        });
        pause.play();
    }


    private void setupMainUI() {
        // Tạo các nút màu
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Orange", "Purple",
                "Black", "White", "Pink", "Gray", "Cyan", "Magenta"};
        colorButtons = new ArrayList<>();

        // Tạo Label cho đồng hồ đếm ngược
        timerLabel = new Label("30");
        timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        timeRemaining = 30; // Reset đồng hồ về 30 giây
        timerLabel.setText(String.valueOf(timeRemaining));

        // Tạo một VBox cho phần trên, bao gồm opponentLabel và timerLabel
        HBox topBox = new HBox(10, opponentLabel, timerLabel);
        topBox.setAlignment(Pos.CENTER_RIGHT);


        // Các bước còn lại trong setupMainUI
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20); // Tăng khoảng cách giữa các hàng
        gridPane.setHgap(20); // Tăng khoảng cách giữa các cột
        gridPane.setAlignment(Pos.CENTER); // Căn giữa nội dung trong GridPane

        // Đặt opponentLabel và moveButton vào HBox
        topBox.setAlignment(Pos.CENTER_RIGHT); // Căn phải cho topBox
        topBox.setPadding(new Insets(10));

        int row = 0;
        int col = 0;
        for (String color : colors) {
            Button colorButton = new Button();
            colorButton.setUserData(color); // Store color name in user data
            colorButton.setStyle("-fx-background-color: " + color.toLowerCase());
            colorButton.setPrefSize(100, 100); // Đặt kích thước nút thành 100x100
            colorButton.setOnAction(e -> handleColorButton(colorButton));
            colorButtons.add(colorButton);

            gridPane.add(colorButton, col, row);

            col++;
            if (col > 2) { // Giữ nguyên bố cục 3 cột
                col = 0;
                row++;
            }
        }

        submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmitButton());

        // Tạo HBox cho scoreLabel và căn trái
        HBox scoreBox = new HBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        scoreBox.setPadding(new Insets(10));

        // Tạo VBox cho phần top, bao gồm scoreBox và topBox
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(scoreBox, topBox);
        topContainer.setAlignment(Pos.CENTER);

        root.setTop(topContainer);

        // Đặt gridPane vào trung tâm
        root.setCenter(gridPane);

        // Đặt submitButton vào vùng dưới cùng, căn giữa
        HBox bottomBox = new HBox(submitButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        root.setBottom(bottomBox);
    }

    public void showGameOverScreen() {
        // Tạo một Label hiển thị điểm số cuối cùng
        String result = gameController.getMatchResult();
        Label gameOverLabel = new Label(result + "\nGame Over\nTotal Score: " + gameController.getTotalScore());
        gameOverLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: green;");

        // Tạo một VBox để căn giữa gameOverLabel
        VBox gameOverBox = new VBox(gameOverLabel);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setPadding(new Insets(20));

        // Đặt VBox vào giữa màn hình
        root.setCenter(gameOverBox);

        // Thêm nút quay lại hoặc kết thúc trò chơi
        Button restartButton = new Button("Back to Friends");
        restartButton.setOnAction(e -> {
            // Close GameView stage
            stage.close();
            Stage friendsStage = friendsView.getStage();
            friendsStage.show();
//            // Show FriendView
//            FriendsView friendsView = new FriendsView(this.gameController.getClientControl());
//            Stage friendsStage = friendsView.getStage();
//            friendsStage.show();
        });

        VBox buttonBox = new VBox(restartButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));

        root.setBottom(buttonBox);
    }


    private void handleColorButton(Button button) {
        String color = (String) button.getUserData(); // Get the color from user data
        System.out.println("Color selected: " + color + " - " + "Selected colors: " + selectedColors);
        if (selectedColors.contains(color)) {
            selectedColors.remove(color);
            button.setOpacity(1.0);
        } else if (selectedColors.size() < 3) {
            selectedColors.add(color);
            button.setOpacity(0.5);
        }
    }

    private void handleSubmitButton() {
        if (selectedColors.size() == 3 || timeRemaining == 0) {
            timer.stop();
            timer = null;

            String colors = String.join(",", selectedColors);
            System.out.println("Colors chosen: " + colors);

            List<String> correctColors = gameController.getColors();

            for (Button button : colorButtons) {
                String color = (String) button.getUserData();
                if (selectedColors.contains(color)) {
                    if (correctColors.contains(color)) {
                        flashButton(button, "green");
                    } else {
                        flashButton(button, "red");
                    }
                }
                button.setOpacity(1);
            }

            gameController.sendColors(colors);
            selectedColors.clear();
            roundCnt += 1;
//            updateScore();

            // Add a pause after flashing to display the score screen
            PauseTransition pauseAfterFlash = new PauseTransition(Duration.seconds(1.5));
            pauseAfterFlash.setOnFinished(event -> {
                if (roundCnt <= 4) {
                    showRoundScoreScreen();  // Method to show score between rounds
                    updateScore();
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        showColorsFromServer();
//                        startTimer();
                    });

                    pause.play();
                } else {
                    String checkRes = gameController.getMatchResult();
                    System.out.println("Check res: " + Objects.equals(checkRes, ""));
                    if (Objects.equals(checkRes, "")) {
                        showWaitingMessage();
                    }
                    roundCnt = 0;
                }
            });
            pauseAfterFlash.play();
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select exactly 3 colors.");
        }
    }

    public void showWaitingMessage() {
        Label waitingLabel = new Label("Waiting for opponent to finish...");
        root.setCenter(waitingLabel); // Temporarily replace the center of the root with the waiting message
    }


    private void showRoundScoreScreen() {
        Label roundScoreLabel = new Label("Round Score: " + gameController.getScore());
        roundScoreLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: blue;");

        VBox scoreBox = new VBox(roundScoreLabel);
        scoreBox.setAlignment(Pos.CENTER);
        root.setCenter(scoreBox);
    }


    private void flashButton(Button button, String color) {
        String originalStyle = button.getStyle();
        String overlayColor = "rgba(255, 0, 0, 0.3)"; // Màu đỏ với opacity 0.3

        // Thiết lập hiệu ứng flash để thay đổi giữa màu chính và màu overlay
        PauseTransition flashOn = new PauseTransition(Duration.seconds(0.2));
        flashOn.setOnFinished(e -> button.setStyle("-fx-background-color: " + overlayColor + ", " + color + ";"));

        PauseTransition flashOff = new PauseTransition(Duration.seconds(0.2));
        flashOff.setOnFinished(e -> button.setStyle(originalStyle));

        // Chain các flash để tạo hiệu ứng nhấp nháy
        flashOn.setOnFinished(e -> {
            button.setStyle("-fx-background-color: " + overlayColor + ", " + color + ";");
            flashOff.play();
        });

        flashOff.setOnFinished(e -> {
            button.setStyle(originalStyle);
            flashOn.play();
        });

        // Bắt đầu chuỗi flash và dừng sau 1 giây
        flashOn.play();

        PauseTransition stopFlash = new PauseTransition(Duration.seconds(1));
        stopFlash.setOnFinished(e -> button.setStyle(originalStyle));
        stopFlash.play();
    }


    private void startTimer() {
        // Nếu đã tồn tại timer từ trước, hãy dừng nó trước khi tạo timer mới
        if (timer != null) {
            timer.stop();
        }

        // Đặt thời gian đếm ngược ban đầu là 30 giây
        timeRemaining = 5;
        timerLabel.setText(String.valueOf(timeRemaining));

        // Tạo Timeline mới cho timer
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (timeRemaining > 0) {
                        timeRemaining--;
                        timerLabel.setText(String.valueOf(timeRemaining));
                    } else {
//                        timer.stop(); // Dừng timer
//                        timer = null; // Xóa timer để tạo timer mới ở lần tiếp theo
                        handleSubmitButton(); // Tự động nộp khi hết thời gian
                    }
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE); // Lặp lại vô hạn cho đến khi dừng
        timer.play(); // Bắt đầu timer
    }


    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
        opponentLabel.setText("Opponent: " + opponent);
    }

    public void updateScore() {
        int score = gameController.getTotalScore();
        scoreLabel.setText("Score: " + score);
    }

    public String getOpponent() {
        return opponent;
    }
}