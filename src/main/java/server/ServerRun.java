package server;

import server.controller.ServerControl;

public class ServerRun {
    public static void main(String[] args) {
        try {
            ServerControl serverControl = new ServerControl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}