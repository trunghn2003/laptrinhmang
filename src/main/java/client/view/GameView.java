package client.view;

import client.controller.GameController;

import javax.swing.*;
import java.awt.*;

public class GameView extends JFrame {
    private GameController gameController;
    private String opponent;
    private JLabel opponentLabel;
    private JButton moveButton;

    public GameView(GameController gameController) {
        this.gameController = gameController;
        setupUI();
    }

    private void setupUI() {
        setTitle("Color Guessing Game");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        opponentLabel = new JLabel("Opponent: ");
        moveButton = new JButton("Send Move");
        moveButton.addActionListener(e -> {
            // Gửi lượt chơi
            gameController.sendGameMove("MOVE_DATA");
        });

        JPanel panel = new JPanel();
        panel.add(opponentLabel);
        panel.add(moveButton);

        add(panel);

        setVisible(true);
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
        opponentLabel.setText("Opponent: " + opponent);
    }
}
