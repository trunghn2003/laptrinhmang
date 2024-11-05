package client.controller;

import client.utils.Constants;
import client.view.GameView;

import java.util.ArrayList;
import java.util.Arrays;

public class GameController {
    private ClientControl clientControl;
    private GameView gameView;
    public final ArrayList<String> fullColors = new ArrayList<>(
            Arrays.asList("red", "green", "blue", "yellow", "orange", "purple", "black", "white")
    );
    private ArrayList<String> colors = new ArrayList<>();
    private boolean check = false;
    private int score = 0;

    public GameController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public void startGame(String opponent) {
        this.gameView = new GameView(this);
        gameView.setOpponent(opponent);
        gameView.setVisible(true);
        clientControl.sendMessage(Constants.ACTION_START_GAME);
    }

    public void sendGameMove(String move) {
        String message = Constants.ACTION_GAME_MOVE + ":" + move;
        clientControl.sendMessage(message);
    }

    public void receiveGameUpdate(String message) {
        // Xử lý cập nhật trò chơi
    }

    public ArrayList<String> receivedColors(String message) {
        System.out.println("message colors: " + message);
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
        ArrayList<String> receivedColors = new ArrayList<>(Arrays.asList(parts.get(1).split(",")));
        if (receivedColors.isEmpty()) {
            colors.clear();
        } else {
            colors.addAll(receivedColors);
        }

        return colors;
    }

    public int getScore() {
        return score;
    }

    public void sendColors(String colors) {
        clientControl.sendMessage(Constants.ACTION_SEND_COLORS + ":" + colors);
    }

    public void receiveGameResult(String message) {
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
        System.out.println("RESULT: " + message);
        check = Boolean.parseBoolean(parts.get(2));
        score = Integer.parseInt(parts.get(3));
    }

    public void endMidGame() {
        String opponent = gameView.getOpponent();
        clientControl.sendMessage(Constants.ACTION_EXIT_MID_GAME + ":" + opponent);
    }

    public String receivedMatchResult() {
        String result = clientControl.receiveData().toString();
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(result.split(":")));
        if (parts.get(1).equals("true")) {
            return "You win!";
        } else {
            return "You lose!";
        }
    }

    public Object receiveData() {
        System.out.println("receive data " + clientControl.receiveData());
        return clientControl.receiveData();
    }
}
