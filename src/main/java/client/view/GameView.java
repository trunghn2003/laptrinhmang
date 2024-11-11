package client.view;

import client.controller.GameController;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameView {
    private GameController gameController;
    private String opponent;
    private Label opponentLabel;
    private Button moveButton;
    private Button submitButton;
    private ArrayList<Button> colorButtons;
    private ArrayList<String> selectedColors;
    private Label scoreLabel;
    private int roundCnt = 0;

    private Stage stage;
    private BorderPane root; // Root pane to switch content
    private Scene scene; // Main scene

    public GameView(GameController gameController) {
        this.gameController = gameController;
        this.selectedColors = new ArrayList<>();
        setupUI();
        showColorsFromServer();
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
        });
        pause.play();
    }

    private void setupMainUI() {
        // Tạo các nút màu
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Orange", "Purple",
                "Black", "White", "Pink", "Gray", "Cyan", "Magenta"};
        colorButtons = new ArrayList<>();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20); // Tăng khoảng cách giữa các hàng
        gridPane.setHgap(20); // Tăng khoảng cách giữa các cột
        gridPane.setAlignment(Pos.CENTER); // Căn giữa nội dung trong GridPane

        moveButton = new Button("Send Move");
        moveButton.setOnAction(e -> {
            // Gửi lượt chơi
            gameController.endMidGame();
        });

        // Đặt opponentLabel và moveButton vào HBox
        HBox topBox = new HBox(10, opponentLabel, moveButton);
        topBox.setAlignment(Pos.CENTER);

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

    private void handleColorButton(Button button) {
        String color = (String) button.getUserData(); // Get the color from user data
        if (selectedColors.contains(color)) {
            selectedColors.remove(color);
            button.setDisable(false);
        } else if (selectedColors.size() < 3) {
            selectedColors.add(color);
            button.setDisable(true);
        }
    }

    private void handleSubmitButton() {
        if (selectedColors.size() == 3) {
            String colors = String.join(",", selectedColors);
            System.out.println("Colors chosen: " + colors);
            gameController.sendColors(colors);
//            showAlert(Alert.AlertType.INFORMATION, "Colors sent: " + colors);

            // Reset các nút màu và danh sách màu đã chọn
            for (Button button : colorButtons) {
                button.setDisable(false);
            }
            roundCnt += 1;
            selectedColors.clear();
            if (roundCnt <= 5) {
                showColorsFromServer();
                showAlert(Alert.AlertType.INFORMATION, "Round " + roundCnt + " completed.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Game completed.");
                roundCnt = 0;
                // Chuyển sang màn hình kết quả
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select exactly 3 colors.");
        }
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

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public String getOpponent() {
        return opponent;
    }
}