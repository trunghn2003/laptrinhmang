package client.view;

import client.controller.RegisterController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterView {
    private TextField usernameField;
    private PasswordField passwordField, confirmPasswordField;
    private Button registerButton;
    private RegisterController registerController;
    private Stage stage;

    public RegisterView() {
        registerController = new RegisterController();
        createRegisterView();
    }

    private void createRegisterView() {
        stage = new Stage();

        // Tải font chữ
        Font.loadFont(getClass().getResourceAsStream("/fonts/SVN-Bango.otf"), 52);

        // Tiêu đề với stroke
        Text titleTextStroke = new Text("SIGN UP");
        titleTextStroke.setFont(Font.font("SVN-Bango", 52));
        titleTextStroke.setFill(Color.web("#714F20"));
        titleTextStroke.setStroke(Color.web("#714F20"));
        titleTextStroke.setStrokeWidth(6);

        Text titleText = new Text("SIGN UP");
        titleText.setFont(Font.font("SVN-Bango", 52));
        titleText.setFill(Color.WHITE);

        // Thêm hiệu ứng drop shadow cho chữ chính
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#9B6B27"));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(8);
        dropShadow.setRadius(0);
        titleTextStroke.setEffect(dropShadow);

        // Sử dụng StackPane để chồng hai Text
        StackPane titleStack = new StackPane();
        titleStack.getChildren().addAll(titleTextStroke, titleText);
        titleStack.setAlignment(Pos.CENTER);

        // Các trường nhập
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(600);
        usernameField.setPrefHeight(60);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(600);
        passwordField.setPrefHeight(60);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefWidth(600);
        confirmPasswordField.setPrefHeight(60);

        // Tạo Text cho nút đăng ký
        Text registerText = new Text("Sign Up");
        registerText.setFill(Color.WHITE);
        registerText.setStyle("-fx-font-size: 18px;");
        registerText.setStroke(Color.web("#9B6B27"));
        registerText.setStrokeWidth(1);

        DropShadow textShadow = new DropShadow();
        textShadow.setOffsetY(2.0);
        textShadow.setColor(Color.web("#9B6B27"));
        textShadow.setRadius(0);
        registerText.setEffect(textShadow);

        // Nút đăng ký
        registerButton = new Button();
        registerButton.setGraphic(registerText);
        registerButton.setPrefWidth(600);
        registerButton.setPrefHeight(60);

        // Thêm hiệu ứng DropShadow cho nút
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.web("#9B6B27"));
        buttonShadow.setRadius(1);
        registerButton.setEffect(buttonShadow);

        // Sự kiện cho nút đăng ký
        registerButton.setOnAction(e -> register());

        // Dòng chữ "Đã có tài khoản? Đăng nhập ngay!"
        Label promptLabel = new Label("Đã có tài khoản? ");
        Hyperlink signInLink = new Hyperlink("Đăng nhập ngay!");

        // Sự kiện cho liên kết "Đăng nhập"
        signInLink.setOnAction(e -> {
            stage.close();
            new LoginView().start(new Stage());
        });

        // HBox chứa dòng chữ và liên kết
        HBox signInBox = new HBox(5, promptLabel, signInLink);
        signInBox.setAlignment(Pos.CENTER);

        // VBox chứa các thành phần giao diện
        VBox vbox = new VBox(40);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
                titleStack,
                usernameField,
                passwordField,
                confirmPasswordField,
                registerButton,
                signInBox
        );

        // Tạo scene và áp dụng CSS
        Scene scene = new Scene(vbox, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Đăng ký");
        stage.setScene(scene);
        stage.show();
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Lỗi", "Mật khẩu không khớp", Alert.AlertType.ERROR);
            return;
        }

        registerController.register(username, password);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void close() {
        stage.close();
    }

    public Stage getStage() {
        return stage;
    }
}