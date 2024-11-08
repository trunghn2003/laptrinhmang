package client.view;

import client.controller.ClientControl;
import client.controller.GameController;
import server.model.User;
import client.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainView extends JFrame {
    private ClientControl clientControl;
    private GameController gameController;
    private DefaultListModel<User> userListModel;
    private JList<User> userList;
    private JButton inviteButton;

    // Chat components
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JButton sendChatButton;
    private User selectedChatUser;

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        setupUI();
        updateUserList((List<User>) userData);
        listenFromServer();
    }

    // Create the main UI
    private void setupUI() {
        setTitle("Online Players");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Left panel: user list and invite button
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Use custom renderer to display user names and statuses
        userList.setCellRenderer(new UserListCellRenderer());

        JScrollPane userScrollPane = new JScrollPane(userList);

        inviteButton = new JButton("Invite to Play");
        inviteButton.addActionListener(e -> {
            User selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                sendInvite(selectedUser.getUserName());
            } else {
                JOptionPane.showMessageDialog(null, "Please select a player to invite.");
            }
        });

        // Add selection listener to set the selectedChatUser
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedChatUser = userList.getSelectedValue();
            }
        });

        leftPanel.add(userScrollPane, BorderLayout.CENTER);
        leftPanel.add(inviteButton, BorderLayout.SOUTH);

        // Right panel: chat area
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BorderLayout());

        chatInputField = new JTextField();
        sendChatButton = new JButton("Send");

        sendChatButton.addActionListener(e -> sendChatMessage());

        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);

        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(chatInputPanel, BorderLayout.SOUTH);

        // Main layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(200);

        add(splitPane);

        setVisible(true);
    }

    // Update the user list
    public void updateUserList(List<User> users) {
        userListModel.clear();
        for (User user : users) {
            if (!user.getUserName().equals(clientControl.getCurrentUser().getUserName())) {
                userListModel.addElement(user);
            }
        }
    }

    // Send game invite
    private void sendInvite(String recipient) {
        String message = Constants.ACTION_INVITE + ":" + recipient;
        clientControl.sendMessage(message);
    }

    // Send chat message
    private void sendChatMessage() {
        String messageText = chatInputField.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }
        if (selectedChatUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to chat with.");
            return;
        }
        String recipientUsername = selectedChatUser.getUserName();
        String message = Constants.ACTION_CHAT_MESSAGE + ":" + recipientUsername + ":" + messageText;
        clientControl.sendMessage(message);

        // Display the message in the chat area
        chatArea.append("Me: " + messageText + "\n");

        // Clear the input field
        chatInputField.setText("");
    }

    // Listen for messages from the server
    private void listenFromServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = clientControl.receiveData();
                    if (obj instanceof String) {
                        String message = (String) obj;
                        System.out.println("Received message: " + message);
                        if (message.startsWith(Constants.RESPONSE_CHAT_MESSAGE)) {
                            handleChatMessage(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE)) {
                            handleInvite(message);
                        } else if (message.startsWith(Constants.RESPONSE_INVITE_RESPONSE)) {
                            handleInviteResponse(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_START)) {
                            handleGameStart(message);
                        } else if (message.startsWith(Constants.RESPONSE_RANDOM_COLORS)) {
                            gameController.receivedColors(message);
                        } else if (message.startsWith(Constants.RESPONSE_GAME_RESULT)) {
                            gameController.receiveGameResult(message);
                        } else if (message.startsWith(Constants.RESPONSE_EXIT_MIDDLE_GAME)) {
                            System.out.println("Exit mid-game");
                        } else if (message.startsWith(Constants.RESPONSE_MATCH_RESULT)) {
                            gameController.receivedMatchResult(message);
                        } else {
                            // Handle other messages
                        }
                    } else if (obj instanceof List) {
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

    // Handle incoming chat messages
    private void handleChatMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 3) {
            String senderUsername = parts[1];
            String chatContent = parts[2];
            chatArea.append(senderUsername + ": " + chatContent + "\n");
        } else {
            System.out.println("Invalid chat message format: " + message);
        }
    }

    // Handle game invite
    private void handleInvite(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String recipient = parts[1]; // Tên người nhận (người gửi phản hồi)
            String response = parts[2];  // Phản hồi (ACCEPT hoặc DECLINE)
            // Xử lý phản hồi dựa trên nội dung
            if (response.equalsIgnoreCase("ACCEPT")) {
                JOptionPane.showMessageDialog(null, recipient + " has accepted your invitation. The game will start!");
                gameController.startGame(recipient); // Bắt đầu trò chơi
            } else if (response.equalsIgnoreCase("DECLINE")) {
                JOptionPane.showMessageDialog(null, recipient + " has declined your invitation.");
            }
        } else {
            // Log lỗi nếu thông điệp không đủ phần tử sau khi tách
            String sender = message.split(":")[1];
            int response = JOptionPane.showConfirmDialog(null, sender + " invites you to a game. Do you accept?", "Game Invitation", JOptionPane.YES_NO_OPTION);
            String responseMessage = Constants.ACTION_INVITE_RESPONSE + ":" + sender + ":" + (response == JOptionPane.YES_OPTION ? "ACCEPT" : "DECLINE");
            clientControl.sendMessage(responseMessage);
        }
    }

    // Handle invite response
    private void handleInviteResponse(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            String recipient = parts[1];
            String response = parts[2];
            if (response.equalsIgnoreCase("ACCEPT")) {
                JOptionPane.showMessageDialog(null, recipient + " has accepted your invitation. The game will start!");
                gameController.startGame(recipient);
            } else {
                JOptionPane.showMessageDialog(null, recipient + " has declined your invitation.");
            }
        } else {
            System.out.println("Invalid invite response format: " + message);
        }
    }

    // Handle game start
    private void handleGameStart(String message) {
        String opponent = message.split(":")[1];
        JOptionPane.showMessageDialog(null, "Starting game with " + opponent + "!");
        gameController.startGame(opponent);
        this.setVisible(false);
    }
}

// Custom renderer for displaying user names and statuses
class UserListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof User) {
            User user = (User) value;
            // Display user name and status
            String statusText = user.getStatus() == 1 ? "Online"
                    : user.getStatus() == 2 ? "Playing" : "Offline";
            label.setText("<html><b>" + user.getUserName() + "</b> (" + statusText + ")</html>");

            // Customize color based on user status
            if (user.getStatus() == 1) {
                label.setForeground(new Color(0, 128, 0));  // Green for Online
            } else if (user.getStatus() == 2) {
                label.setForeground(new Color(0, 0, 255));  // Blue for Playing
            } else {
                label.setForeground(new Color(128, 128, 128));  // Gray for Offline
            }

            // Add padding between items
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        if (isSelected) {
            label.setBackground(new Color(200, 230, 255));  // Background color when selected
        }

        return label;
    }
}
