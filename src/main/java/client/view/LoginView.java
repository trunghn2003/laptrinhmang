package client.view;

import client.controller.LoginController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Giao diện đăng nhập người dùng bằng JavaFX với thiết kế đẹp hơn.
 */
public class LoginView extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private LoginController loginController;

    @Override
    public void start(Stage primaryStage) {
        loginController = new LoginController();

        // Tiêu đề
        Label titleLabel = new Label("Login");
        titleLabel.setFont(Font.font(42));

        // Trường nhập với PromptText
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250); // Đặt chiều rộng cho trường nhập

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        // Nút đăng nhập
        loginButton = new Button("Login");
        loginButton.setPrefWidth(250); // Đặt chiều rộng cho nút bằng với trường nhập

        // Sự kiện cho nút đăng nhập
        loginButton.setOnAction(e -> login());

        // Dòng chữ "Don't have an account? Sign up"
        Label promptLabel = new Label("Don't have an account? ");
        Hyperlink signUpLink = new Hyperlink("Sign up");

        // Sự kiện cho liên kết "Sign up"
        signUpLink.setOnAction(e -> openRegisterView());

        // HBox chứa dòng chữ và liên kết
        HBox signUpBox = new HBox(5, promptLabel, signUpLink);
        signUpBox.setAlignment(Pos.CENTER);

        // VBox chứa các thành phần giao diện theo chiều dọc
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
                titleLabel,
                usernameField,
                passwordField,
                loginButton,
                signUpBox
        );

        // Thêm phong cách CSS
        Scene scene = new Scene(vbox, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        titleLabel.setId("title-label");

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        loginController.login(username, password);
    }

    private void openRegisterView() {
        // Mở giao diện đăng ký
        // Bạn cần chuyển đổi lớp RegisterView sang JavaFX tương tự
         new RegisterView(); // Ví dụ
    }

    public static void main(String[] args) {
        launch(args);
    }
}
