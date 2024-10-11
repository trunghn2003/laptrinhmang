package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import server.model.User;
import client.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MainView extends JFrame {
    private ClientControl clientControl;
    private GameController gameController;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JButton inviteButton;

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        setupUI();
        updateUserList((List<User>) userData);
        listenFromServer();
    }

    private void setupUI() {
        setTitle("Online Players");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);

        inviteButton = new JButton("Invite to Play");
        inviteButton.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                sendInvite(selectedUser);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a player to invite.");
            }
        });

        add(userScrollPane, BorderLayout.CENTER);
        add(inviteButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void updateUserList(List<User> users) {
        userListModel.clear();
        for (User user : users) {
            if (user.getStatus() == 1) {
                userListModel.addElement(user.getUserName());
            }
        }
    }

    private void sendInvite(String recipient) {
        String message = Constants.ACTION_INVITE + ":" + recipient;
        clientControl.sendMessage(message);
    }

    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        if (message.startsWith(Constants.RESPONSE_INVITE)) {
                            handleInvite(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE_RESPONSE)) {
                            handleInviteResponse(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_START)) {
                            handleGameStart(message);
                        } else {
                            // Xử lý các tin nhắn khác
                        }
                    } else if (obj instanceof List) {
                        // Cập nhật danh sách người chơi online
                        @SuppressWarnings("unchecked")
                        List<User> users = (List<User>) obj;
                        updateUserList(users);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleInvite(String message) {
        String sender = message.split(":")[1];
        int response = JOptionPane.showConfirmDialog(null, sender + " invites you to a game. Do you accept?", "Game Invitation", JOptionPane.YES_NO_OPTION);
        String responseMessage = Constants.ACTION_INVITE_RESPONSE + ":" + sender + ":" + (response == JOptionPane.YES_OPTION ? "ACCEPT" : "DECLINE");
        clientControl.sendMessage(responseMessage);
    }

    private void handleInviteResponse(String message) {
        String[] parts = message.split(":");
        String recipient = parts[1];
        String response = parts[2];
        if (response.equals("ACCEPT")) {
            JOptionPane.showMessageDialog(null, recipient + " has accepted your invitation. The game will start!");
            gameController.startGame(recipient);
        } else {
            JOptionPane.showMessageDialog(null, recipient + " has declined your invitation.");
        }
    }

    private void handleGameStart(String message) {
        String opponent = message.split(":")[1];
        JOptionPane.showMessageDialog(null, "Starting game with " + opponent + "!");
        gameController.startGame(opponent);
    }
}
