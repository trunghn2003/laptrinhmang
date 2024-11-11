package client.view;

import client.controller.LoginController;
import javafx.application.Application;
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
import kotlin.Pair;


public class LoginView extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private LoginController loginController;

    @Override
    public void start(Stage primaryStage) {
        loginController = new LoginController(this);

        // Tải font chữ
        Font.loadFont(getClass().getResourceAsStream("/fonts/SVN-Bango.otf"), 52);

        // Tiêu đề với stroke
        Text titleTextStroke = new Text("SIGN IN");
        titleTextStroke.setFont(Font.font("SVN-Bango", 52));
        titleTextStroke.setFill(Color.web("#714F20")); // Màu stroke
        titleTextStroke.setStroke(Color.web("#714F20")); // Màu stroke
        titleTextStroke.setStrokeWidth(6); // Độ dày stroke

        Text titleText = new Text("SIGN IN");
        titleText.setFont(Font.font("SVN-Bango", 52));
        titleText.setFill(Color.WHITE); // Màu chữ chính

        // Thêm hiệu ứng drop shadow cho chữ chính
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#9B6B27"));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(8);
        dropShadow.setRadius(0); // Đặt độ mờ (blur) thành 0
        titleTextStroke.setEffect(dropShadow);

        // Sử dụng StackPane để chồng hai Text
        StackPane titleStack = new StackPane();
        titleStack.getChildren().addAll(titleTextStroke, titleText);
        titleStack.setAlignment(Pos.CENTER);

        // Trường nhập với PromptText
        usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.setPrefWidth(600); // Đặt chiều rộng cho trường nhập
        usernameField.setPrefHeight(60); // Đặt chiều cao cho trường nhập

        passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.setPrefWidth(600);
        passwordField.setPrefHeight(60); // Đặt chiều cao cho trường nhập

        // Tạo Text với hiệu ứng bóng đổ
        Text text = new Text("Đăng nhập");
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 18px;");

        // Thêm stroke cho Text
        text.setStroke(Color.web("#9B6B27")); // Đặt màu stroke (ví dụ: màu đen)
        text.setStrokeWidth(1); // Đặt độ rộng stroke (ví dụ: 1 pixel)

        DropShadow ds = new DropShadow();
        ds.setOffsetY(2.0);
        ds.setColor(Color.web("#9B6B27"));
        ds.setRadius(0);
        text.setEffect(ds);

        // Nút đăng nhập
        loginButton = new Button();
        loginButton.setGraphic(text); // Đặt Text vào nút
        loginButton.setPrefWidth(600); // Đặt chiều rộng cho nút bằng với trường nhập
        loginButton.setPrefHeight(60); // Đặt chiều cao cho nút

        // Thêm hiệu ứng DropShadow cho nút
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.web("#9B6B27"));
        buttonShadow.setRadius(1);
        loginButton.setEffect(buttonShadow);

        // Sự kiện cho nút đăng nhập
        loginButton.setOnAction(e -> login());

        // Dòng chữ "Chưa có tài khoản? Đăng ký ngay!"
        Label promptLabel = new Label("Chưa có tài khoản? ");
        Hyperlink signUpLink = new Hyperlink("Đăng ký ngay!");

        // Sự kiện cho liên kết "Đăng ký"
        signUpLink.setOnAction(e -> openRegisterView());

        // HBox chứa dòng chữ và liên kết
        HBox signUpBox = new HBox(5, promptLabel, signUpLink);
        signUpBox.setAlignment(Pos.CENTER);

        // VBox chứa các thành phần giao diện theo chiều dọc
        VBox vbox = new VBox(40); // Tăng khoảng cách giữa các phần tử
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
                titleStack, // Thay thế titleLabel bằng titleStack
                usernameField,
                passwordField,
                loginButton,
                signUpBox
        );

        // Thêm phong cách CSS
        Scene scene = new Scene(vbox, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        loginController.login(username, password);
    }

    private void openRegisterView() {
        new RegisterView(); // Ví dụ
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void close() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}