package client.controller;

import client.utils.Constants;
import client.view.GameView;

import java.util.ArrayList;
import java.util.Arrays;

public class GameController {
    private ClientControl clientControl;
    private GameView gameView;
    private ArrayList<String> colors = new ArrayList<>();
    private boolean gameResult = false;
    private String matchResult = "";
    private int score = 0;
    private int totalScore = 0;
    private String opponent;

    public GameController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public void startGame(String opponent) {
        clientControl.sendMessage(Constants.ACTION_START_GAME);
        this.opponent = opponent;
    }

    public int getScore() {
        return this.score;
    }
    public int getTotalScore() {
        return this.totalScore;
    }

    public ArrayList<String> getColors() {
        return this.colors;
    }

    public String getMatchResult() {
        return this.matchResult;
    }

    //Phương thức nhận màu ngẫu nhiên từ server
    public void receivedColors(String message) {
        System.out.println("Received colors: " + message);
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
        ArrayList<String> receivedColors = new ArrayList<>(Arrays.asList(parts.get(1).split(",")));

        int currentRound = Integer.parseInt(parts.get(2));

        colors.clear();
        colors.addAll(receivedColors);

        if(currentRound == 0) {
            this.gameView = new GameView(this);
            gameView.setOpponent(opponent);
        }

    }

    // Gửi màu đã chọn lên server
    public void sendColors(String colors) {
        clientControl.sendMessage(Constants.ACTION_SEND_COLORS + ":" + colors);
    }

    // Nhận kết quả từ server
    public void receiveGameResult(String message) {
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
        this.gameResult = Boolean.parseBoolean(parts.get(1));
        this.score = Integer.parseInt(parts.get(2));

        this.totalScore += score;

    }

    // Kết thúc trò chơi giữa chừng
    public void endMidGame() {
        clientControl.sendMessage(Constants.ACTION_EXIT_MID_GAME);
    }

    // Nhận kết quả trận đấu
    public void receivedMatchResult(String message) {
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(message.split(":")));
        this.matchResult = parts.get(1);
    }

    public void handleEndGame() {
        this.gameResult = false;
        this.score = 0;
        this.totalScore = 0;
        this.matchResult = "";
        this.opponent = "";
    }
}
