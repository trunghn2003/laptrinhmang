package client.view;

import client.controller.ClientControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsModal {
    private Stage modalStage;
    private double xOffset = 0;
    private double yOffset = 0;
    private ClientControl clientControl;
    private Stage parrentStage;

    public SettingsModal(Stage parentStage, ClientControl clientControl) {
        modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(parentStage);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        this.clientControl = clientControl;
        this.parrentStage = parentStage;

        VBox modalRoot = new VBox(15);
        modalRoot.setAlignment(Pos.TOP_CENTER);
        modalRoot.setPadding(new Insets(10));
        modalRoot.setStyle("-fx-background-color: #453221; -fx-background-radius: 10;");

        // Thêm hiệu ứng shadow cho modal
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.5, 0.5, 0.5));
        modalRoot.setEffect(dropShadow);

        // Container cho nút close
        HBox closeButtonContainer = new HBox();
        closeButtonContainer.setAlignment(Pos.TOP_RIGHT);

        // Nút close
        Button closeButton = new Button("×");
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 5 0 5;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;" +
                        "-fx-background-radius: 15;"
        );

        // Hiệu ứng hover cho nút close
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(
                    "-fx-background-color: #6B4D3A;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0 5 0 5;" +
                            "-fx-min-width: 30px;" +
                            "-fx-min-height: 30px;" +
                            "-fx-background-radius: 15;"
            );
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 20px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0 5 0 5;" +
                            "-fx-min-width: 30px;" +
                            "-fx-min-height: 30px;" +
                            "-fx-background-radius: 15;"
            );
        });

        closeButton.setOnAction(e -> modalStage.close());
        closeButtonContainer.getChildren().add(closeButton);

        // Tiêu đề
        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Thêm spacing giữa title và các nút
        Region spacer = new Region();
        spacer.setPrefHeight(20);

        Button logoutButton = createSettingButton("Logout");

        // Thêm các components vào modal
        modalRoot.getChildren().addAll(
                closeButtonContainer,
                titleLabel,
                spacer,
                logoutButton
        );

        // ... phần code kéo thả giữ nguyên ...

        Scene modalScene = new Scene(modalRoot);
        modalScene.setFill(null);
        modalStage.setScene(modalScene);

        // Điều chỉnh kích thước modal cho phù hợp với ít nút hơn
        modalStage.setWidth(300);
        modalStage.setHeight(300); // Giảm chiều cao xuống vì có ít nút hơn
    }

    private Button createSettingButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #5A3C29;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        button.setPrefWidth(200);

        // Hiệu ứng hover
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #6B4D3A;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 10 20;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #5A3C29;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 10 20;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;"
                )
        );

        // Xử lý sự kiện click
        button.setOnAction(e -> {
            switch(text) {
                case "History":
                    // TODO: Implement history view
                    System.out.println("History clicked");
                    break;
                case "Logout":
                    // TODO: Implement logout
                    logout();
                    modalStage.close(); // Đóng modal khi logout
                    break;
            }
        });

        return button;
    }

    public void show() {
        modalStage.show();
    }

    private void logout() {
        clientControl.closeConnection();
        Stage stage = (Stage) parrentStage.getScene().getWindow();
        stage.close();

        // Launch the LoginView
        LoginView loginView = new LoginView();
        Stage loginStage = new Stage();
        try {
            loginView.start(loginStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}