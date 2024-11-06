package client.view;

import client.controller.GameController;
import client.utils.Constants;
import server.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GameView extends JFrame {
    private GameController gameController;
    private String opponent;
    private JLabel opponentLabel;
    private JButton moveButton;
    private JButton submitButton;
    private ArrayList<JButton> colorButtons;
    private ArrayList<String> selectedColors;

    private ArrayList<String> colors = new ArrayList<>();

    public GameView(GameController gameController) {
        this.gameController = gameController;
        this.selectedColors = new ArrayList<>();
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
            gameController.endMidGame();
        });

        JPanel panel = new JPanel();
        panel.add(opponentLabel);
        panel.add(moveButton);

        add(panel);

        setLayout(new GridLayout(5, 3));

        opponentLabel = new JLabel("Opponent: ");
        add(opponentLabel);

        String[] colors = {"Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Black", "White", "Pink", "Gray", "Cyan", "Magenta"};
        colorButtons = new ArrayList<>();

        for (String color : colors) {
            JButton colorButton = new JButton(color);
            colorButton.setBackground(getColorFromName(color));
            colorButton.addActionListener(new ColorButtonListener());
            colorButtons.add(colorButton);
            add(colorButton);
        }

        submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitButtonListener());
        add(submitButton);

        setVisible(true);
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
        opponentLabel.setText("Opponent: " + opponent);
    }

    public String getOpponent() {
        return opponent;
    }

    private class ColorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            String color = source.getText();
            if (selectedColors.contains(color)) {
                selectedColors.remove(color);
                source.setEnabled(true);
            } else if (selectedColors.size() < 3) {
                selectedColors.add(color);
                source.setEnabled(false);
            }
        }
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedColors.size() == 3) {
                String colors = String.join(",", selectedColors);
                System.out.println("Colors chosen: " + colors);
                gameController.sendColors(colors);
                JOptionPane.showMessageDialog(null, "Colors sent: " + colors);
            } else {
                JOptionPane.showMessageDialog(null, "Please select exactly 3 colors.");
            }
        }
    }

    private Color getColorFromName(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "yellow": return Color.YELLOW;
            case "orange": return Color.ORANGE;
            case "purple": return new Color(128, 0, 128); // Purple isn't predefined
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            case "pink": return Color.PINK;
            case "gray": return Color.GRAY;
            case "cyan": return Color.CYAN;
            case "magenta": return Color.MAGENTA;
            default: return Color.LIGHT_GRAY; // Fallback color
        }
    }
}
