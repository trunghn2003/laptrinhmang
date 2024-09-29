package server;

import server.view.ServerView;

import javax.swing.*;

public class ServerRun {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerView());

    }
}