package client.view;

import client.controller.GameController;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
//        showColorsFromServer();
//        startTimer();
    }

    private void setupUI() {
        stage = new Stage();
        stage.setTitle("Color Guessing Game");

        root = new BorderPane();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/game-styles.css").toExternalForm());

        opponentLabel = new Label("Opponent: ");
        scoreLabel = new Label("0");

        stage.show();

        // We will add content to 'root' in startGame() and setupMainUI()
        showColorsFromServer();
    }

    private void showColorsFromServer() {

        System.out.println("log in show color");

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
        // Create color buttons
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Black", "White", "Pink", "Gray", "Cyan", "Magenta"};
        colorButtons = new ArrayList<>();

        // Create timer label
        timerLabel = new Label("30");
        timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        timeRemaining = 30;
        timerLabel.setText(String.valueOf(timeRemaining));


        // Create an HBox for the timer with the same background style as scoreBox
        HBox timerBox = new HBox(timerLabel);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setPadding(new Insets(0, 20, 0, 20));
        timerBox.setStyle("-fx-background-color: #453221; -fx-background-radius: 25;");
        timerBox.setMinWidth(100);

        // Create icon image and align it within a StackPane
        Image iconImage = new Image("/assets/coin.png"); // Path to icon image
        ImageView iconView = new ImageView(iconImage);
        iconView.setFitWidth(45);
        iconView.setFitHeight(45);
        iconView.setTranslateX(-20);

        // Create score label
        scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        scoreLabel.setAlignment(Pos.CENTER);
        scoreLabel.setTranslateX(15);

        // Use StackPane to contain iconView and scoreLabel
        StackPane scorePane = new StackPane();
        scorePane.getChildren().addAll(iconView, scoreLabel);
        StackPane.setAlignment(iconView, Pos.CENTER_LEFT);
        StackPane.setAlignment(scoreLabel, Pos.CENTER);

        // Create scoreBox with background color and alignment
        HBox scoreBox = new HBox(scorePane);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPadding(new Insets(0, 20, 0, 20));
        scoreBox.setStyle("-fx-background-color: #453221; -fx-background-radius: 25;");
        scoreBox.setMinWidth(100);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        //Thêm tên đối thủ
        Label opponentLabel = new Label("Opponent: " + opponent);
        opponentLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #453221;");
        opponentLabel.setAlignment(Pos.CENTER);

        // Create topBox to hold scoreBox and timerBox
        HBox topBox = new HBox(scoreBox,spacer, opponentLabel, spacer2 , timerBox);
        topBox.setAlignment(Pos.CENTER);
        topBox.setSpacing(20); // Space between scoreBox and timerBox
        topBox.setPadding(new Insets(10));

        // Set topBox at the top of the root
        root.setTop(topBox);

        // Remaining setup in setupMainUI
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20); // Increase row gap
        gridPane.setHgap(20); // Increase column gap
        gridPane.setAlignment(Pos.CENTER); // Center content in GridPane

        int row = 0;
        int col = 0;
        for (String color : colors) {
            Button colorButton = new Button();
            colorButton.setUserData(color); // Store color name in user data
            colorButton.setStyle("-fx-background-color: " + color.toLowerCase());
            colorButton.setPrefSize(100, 100); // Set button size to 100x100
            colorButton.setOnAction(e -> handleColorButton(colorButton));
            colorButtons.add(colorButton);

            gridPane.add(colorButton, col, row);

            col++;
            if (col > 2) { // Keep 3-column layout
                col = 0;
                row++;
            }
        }

        submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmitButton());

        submitButton.setId("submit-button");

        // Center the gridPane in the root
        root.setCenter(gridPane);

        // Place submitButton at the bottom, centered
        HBox bottomBox = new HBox(submitButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        root.setBottom(bottomBox);
    }


    public void showGameOverScreen() {
        if(timer != null) timer.stop();
        timer = null;

        Font.loadFont(getClass().getResourceAsStream("/fonts/SVN-Bango.otf"), 40);

        // result label
        Text resultLabelStroke = new Text(gameController.getMatchResult());
        resultLabelStroke.setFont(Font.font("SVN-Bango", 40));
        resultLabelStroke.setFill(Color.web("#714F20"));
        resultLabelStroke.setStroke(Color.web("#714F20"));
        resultLabelStroke.setStrokeWidth(6);

        Text resultLabel = new Text(gameController.getMatchResult());
        resultLabel.setFill(Color.WHITE);
        resultLabel.setFont(Font.font("SVN-Bango", 40));

        StackPane resultStack = new StackPane();
        resultStack.getChildren().addAll(resultLabelStroke, resultLabel);
        resultStack.setAlignment(Pos.CENTER);
        resultStack.setTranslateY(-40);

        Label gameOverLabel = new Label("Game Over");
        gameOverLabel.setTranslateY(-95);

        Text scoreLabelStroke = new Text(gameController.getTotalScore() +" - " + gameController.getEnemyScore());
        scoreLabelStroke.setFont(Font.font("SVN-Bango", 26));
        scoreLabelStroke.setFill(Color.web("#714F20"));
        scoreLabelStroke.setStroke(Color.web("#714F20"));
        scoreLabelStroke.setStrokeWidth(6);

        Text scoreLabel = new Text(gameController.getTotalScore() +" - " + gameController.getEnemyScore());
        scoreLabel.setFont(Font.font("SVN-Bango", 26));
        scoreLabel.setFill(Color.WHITE);

        StackPane scoreStack = new StackPane();
        scoreStack.getChildren().addAll(scoreLabelStroke, scoreLabel);
        scoreStack.setAlignment(Pos.CENTER);
        scoreStack.setTranslateY(-40);

        // Tạo nút "View Rounds"
        Button viewRoundsButton = new Button("View Rounds");
        viewRoundsButton.setOnAction(e -> openReview());
        viewRoundsButton.setTranslateY(-20);

        // Thêm hiệu ứng DropShadow cho nút và các thành phần khác
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.web("#9B6B27"));
        buttonShadow.setRadius(1);
        resultStack.setEffect(buttonShadow);
        scoreStack.setEffect(buttonShadow);
        viewRoundsButton.setEffect(buttonShadow);
        viewRoundsButton.setId("view-rounds-btn");

        VBox spacer = new VBox(180); // Giảm spacing để chừa chỗ cho nút mới

        // Style cho từng Label
        String baseStyle = "-fx-font-weight: bold; -fx-text-fill: white;";
        gameOverLabel.setStyle(baseStyle + "-fx-font-size: 24px;");

        // Tạo VBox để chứa các Label theo thứ tự dọc
        VBox labelsBox = new VBox(10); // spacing 10 pixels
        labelsBox.setAlignment(Pos.CENTER);
        labelsBox.getChildren().addAll(
                gameOverLabel,
                resultStack,
                scoreStack,
                viewRoundsButton, // Thêm nút mới vào đây
                spacer
        );

        // Tạo ImageView để hiển thị hình ảnh
        ImageView backgroundImage = new ImageView(new Image("/assets/machine.png"));
        backgroundImage.setFitWidth(350);
        backgroundImage.setPreserveRatio(true);

        // Tạo StackPane để xếp chồng các phần tử
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(backgroundImage, labelsBox);
        StackPane.setAlignment(labelsBox, Pos.CENTER);

        // Tạo VBox để chứa StackPane
        VBox gameOverBox = new VBox(centerStack);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setPadding(new Insets(20));

        // Đặt VBox vào giữa màn hình
        root.setCenter(gameOverBox);

        // Tạo nút "Back to Friends"
        Button backButton = new Button("Back to Friends");
        backButton.setOnAction(e -> {
            stage.close();
            Stage friendsStage = friendsView.getStage();
            friendsStage.show();
        });
        backButton.setId("back-button");

        // Tạo nút "Replay"
        Button replayButton = new Button("Replay");
        replayButton.setOnAction(e -> {
            replayGame();
        });
        replayButton.setId("replay-button");

        // Sử dụng HBox để căn hai nút ngang hàng
        HBox buttonBox = new HBox(10, replayButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
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

        System.out.println("log in submit");

        if (selectedColors.size() == 3 || timeRemaining == 0) {
            if (timer != null) timer.stop();
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
                if (roundCnt <= 2) {
                    showRoundScoreScreen();  // Method to show score between rounds
                    updateScore();
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        showColorsFromServer();
//                        startTimer();
                    });

                    pause.play();
                } else {
//                    timer = null;
                    String checkRes = gameController.getMatchResult();
                    System.out.println("Check res: " + checkRes);
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
        Text endRoundLable = new Text("Round "+ roundCnt + "/ 3 Completed!");
        endRoundLable.setFill(Color.WHITE);
        endRoundLable.setFont(Font.font("SVN-Bango", 30));

        Text endRoundBorderLable = new Text("Round "+ roundCnt + "/ 3 Completed!");
        endRoundBorderLable.setFill(Color.web("#714F20"));
        endRoundBorderLable.setStroke(Color.web("#714F20"));
        endRoundBorderLable.setStrokeWidth(6);
        endRoundBorderLable.setFont(Font.font("SVN-Bango", 30));

        StackPane endRoundStack = new StackPane();
        endRoundStack.getChildren().addAll(endRoundBorderLable, endRoundLable);
        endRoundStack.setAlignment(Pos.CENTER);

        Text roundScoreLabel = new Text("+" + gameController.getScore() + " Points");
        roundScoreLabel.setFill(Color.WHITE);
        roundScoreLabel.setFont(Font.font("SVN-Bango", 50));

        Text roundScoreBorderLabel = new Text("+" + gameController.getScore() + " Points");
        roundScoreBorderLabel.setFill(Color.web("#714F20"));
        roundScoreBorderLabel.setStroke(Color.web("#714F20"));
        roundScoreBorderLabel.setStrokeWidth(6);
        roundScoreBorderLabel.setFont(Font.font("SVN-Bango", 50));

        StackPane roundScoreStack = new StackPane();
        roundScoreStack.getChildren().addAll(roundScoreBorderLabel, roundScoreLabel);
        roundScoreStack.setAlignment(Pos.CENTER);

        // Thêm hiệu ứng DropShadow cho nút
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.web("#9B6B27"));
        buttonShadow.setRadius(1);
        endRoundStack.setEffect(buttonShadow);
        roundScoreStack.setEffect(buttonShadow);

        VBox scoreBox = new VBox();
        scoreBox.setSpacing(30);
        scoreBox.getChildren().addAll(endRoundStack, roundScoreStack);
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

                        System.out.println("log in timer");

//                        timer.stop(); // Dừng timer
//                        timer = null; // Xóa timer để tạo timer mới ở lần tiếp theo
                        handleSubmitButton(); // Tự động nộp khi hết thời gian
                    }
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE); // Lặp lại vô hạn cho đến khi dừng
        timer.play(); // Bắt đầu timer
    }

    private void openReview() {
        ReviewMatchModal reviewMatchModal = new ReviewMatchModal((Stage) root.getScene().getWindow(), gameController);
        reviewMatchModal.show();
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
        scoreLabel.setText(String.valueOf(score));
    }

    public String getOpponent() {
        return opponent;
    }

    public void replayGame() {
        friendsView.sendInvite(opponent);
    }

    public void close() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}