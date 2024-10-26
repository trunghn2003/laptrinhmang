package client.controller;

import client.model.ResponseResult;
import client.view.MainView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController {
    private ClientControl clientControl;

    public LoginController() {
        clientControl = new ClientControl();
    }

    public void login(String username, String password) {
        ResponseResult result = clientControl.login(username, password);
        if (result.isSuccess()) {
            // Mở giao diện chính
            MainView mainView = new MainView(clientControl, result.getData());
            // Không cần setVisible(true) như trong Swing, JavaFX sẽ tự động hiển thị
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