package server.view;

import server.controller.ServerControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerView extends JFrame {
    private JTextArea textArea;
    private ServerControl serverControl;

    public ServerView() {
        setTitle("TCP Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);


        new Thread(() -> {
            try {
                serverControl = new ServerControl(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            showMessage("TCP server is running...");
        }).start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                serverControl.stopServer();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(msg + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }


}
