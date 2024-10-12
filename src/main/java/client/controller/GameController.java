package client.controller;

import client.utils.Constants;
import client.view.GameView;

public class GameController {
    private ClientControl clientControl;
    private GameView gameView;

    public GameController(ClientControl clientControl) {
        this.clientControl = clientControl;
        this.gameView = new GameView(this);
    }

    public void startGame(String opponent) {
        gameView.setOpponent(opponent);
        gameView.setVisible(true);
    }

    public void sendGameMove(String move) {
        String message = Constants.ACTION_GAME_MOVE + ":" + move;
        clientControl.sendMessage(message);
    }

    public void receiveGameUpdate(String message) {
        // Xử lý cập nhật trò chơi
    }
}
