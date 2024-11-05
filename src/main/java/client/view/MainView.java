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
    private JList<User> userList;  // JList giờ sẽ chứa đối tượng User, không chỉ tên người dùng
    private JButton inviteButton;

    public MainView(ClientControl clientControl, Object userData) {
        this.clientControl = clientControl;
        this.gameController = new GameController(clientControl);
        setupUI();
        updateUserList((List<User>) userData);  // Cập nhật danh sách người chơi ban đầu
        listenFromServer();  // Bắt đầu lắng nghe các tin nhắn từ server
    }

    // Tạo giao diện chính
    private void setupUI() {
        setTitle("Online Players");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Sử dụng renderer tùy chỉnh để hiển thị tên và trạng thái người chơi
        userList.setCellRenderer(new UserListCellRenderer());

        JScrollPane userScrollPane = new JScrollPane(userList);

        inviteButton = new JButton("Invite to Play");
        inviteButton.addActionListener(e -> {
            User selectedUser = userList.getSelectedValue();  // Lấy đối tượng User thay vì chỉ tên người dùng
            if (selectedUser != null) {
                sendInvite(selectedUser.getUserName());  // Gửi lời mời tới người chơi được chọn
            } else {
                JOptionPane.showMessageDialog(null, "Please select a player to invite.");
            }
        });

        add(userScrollPane, BorderLayout.CENTER);
        add(inviteButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Cập nhật danh sách người chơi
    public void updateUserList(List<User> users) {
        userListModel.clear();
        for (User user : users) {
            if(!user.getUserName().equals(clientControl.getCurrentUser().getUserName())) {
                userListModel.addElement(user);
            }
        }
    }

    // Gửi lời mời chơi
    private void sendInvite(String recipient) {
        String message = Constants.ACTION_INVITE + ":" + recipient;
        clientControl.sendMessage(message);
    }

    // Lắng nghe từ server
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
                            this.dispose();
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

    // Xử lý khi nhận được lời mời chơi từ người khác
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

    // Xử lý phản hồi lời mời
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

    // Xử lý khi trò chơi bắt đầu
    private void handleGameStart(String message) {
        String opponent = message.split(":")[1];
        JOptionPane.showMessageDialog(null, "Starting game with " + opponent + "!");
        gameController.startGame(opponent);
    }
}

// Renderer tùy chỉnh để hiển thị tên và trạng thái người dùng
class UserListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof User) {
            User user = (User) value;
            // Hiển thị tên người dùng và trạng thái
            String statusText = user.getStatus() == 1 ? "Online" : user.getStatus() == 2 ? "Playing" : "Offline";
            label.setText("<html><b>" + user.getUserName() + "</b> (" + statusText + ")</html>");

            // Tùy chỉnh màu sắc dựa trên trạng thái người dùng
            if (user.getStatus() == 1) {
                label.setForeground(new Color(0, 128, 0));  // Xanh lá cho Online
            } else if (user.getStatus() == 2) {
                label.setForeground(new Color(0, 0, 255));  // Xanh dương cho Playing
            } else {
                label.setForeground(new Color(128, 128, 128));  // Xám cho Offline
            }

            // Thêm khoảng cách giữa các mục
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        if (isSelected) {
            label.setBackground(new Color(200, 230, 255));  // Màu nền khi được chọn
        }

        return label;
    }
}

