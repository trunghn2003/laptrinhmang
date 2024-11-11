package client.controller;

import client.model.ResponseResult;
import client.view.MainView;
import client.view.LoginView; // Import LoginView
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import kotlin.Pair;

public class LoginController {
    private ClientControl clientControl;
    private LoginView loginView; // Tham chiếu đến LoginView

    // Constructor nhận LoginView
    public LoginController(LoginView loginView) {
        this.clientControl = new ClientControl();
        this.loginView = loginView; // Khởi tạo tham chiếu
    }

    public void login(String username, String password) {
        ResponseResult result = clientControl.login(username, password);
        if (result.isSuccess()) {
            loginView.close();
            // Mở giao diện chính
            MainView mainView = new MainView(clientControl, result.getData());
        } else {
            showAlert(result.getMessage(), "Login Failed", AlertType.ERROR);
        }
    }

    // Phương thức để hiển thị thông báo
    private void showAlert(String message, String title, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}