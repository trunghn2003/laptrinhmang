package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
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

import java.util.ArrayList;

public class ReviewMatchModal {
    private Stage modalStage;
    private double xOffset = 0;
    private double yOffset = 0;
    GameController gameController;
    ArrayList<Integer> getAllScoreRound = new ArrayList<>();

    public ReviewMatchModal(Stage parentStage, GameController gameController) {
        this.gameController = gameController;
        modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(parentStage);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        this.getAllScoreRound = gameController.getAllScoreRound();

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
        Label titleLabel = new Label("Round Results");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Container cho các kết quả round
        VBox roundsContainer = new VBox(10);
        roundsContainer.setAlignment(Pos.CENTER);
        roundsContainer.setPadding(new Insets(10));

        // Thêm kết quả từng round
        for (int i = 0; i < getAllScoreRound.size(); i += 2) {
            if (i + 1 < getAllScoreRound.size()) {
                HBox roundBox = createRoundBox((i / 2) + 1,
                        getAllScoreRound.get(i),
                        getAllScoreRound.get(i + 1));
                roundsContainer.getChildren().add(roundBox);
            }
        }

        // Thêm spacing
        Region spacer = new Region();
        spacer.setPrefHeight(20);

        // Thêm các components vào modal
        modalRoot.getChildren().addAll(
                closeButtonContainer,
                titleLabel,
                spacer,
                roundsContainer
        );

        // Cho phép kéo thả modal
        modalRoot.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        modalRoot.setOnMouseDragged(event -> {
            modalStage.setX(event.getScreenX() - xOffset);
            modalStage.setY(event.getScreenY() - yOffset);
        });

        Scene modalScene = new Scene(modalRoot);
        modalScene.setFill(null);
        modalStage.setScene(modalScene);

        modalStage.setWidth(300);
        modalStage.setHeight(400);
    }

    private HBox createRoundBox(int roundNumber, int playerScore, int enemyScore) {
        HBox roundBox = new HBox(10);
        roundBox.setAlignment(Pos.CENTER);
        roundBox.setStyle(
                "-fx-background-color: #5A3C29;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10;"
        );

        Label roundLabel = new Label("Round " + roundNumber);
        roundLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label scoreLabel = new Label(playerScore + " - " + enemyScore);
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Thêm spacing giữa round label và score
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        roundBox.getChildren().addAll(roundLabel, spacer, scoreLabel);
        return roundBox;
    }

    public void show() {
        modalStage.show();
    }
}