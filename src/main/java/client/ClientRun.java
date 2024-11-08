package client;

import client.view.LoginView;

public class ClientRun {
    public static void main(String[] args) {
//        for (int i = 0; i < 2; i++) {
//            new Thread(() -> {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
//            }).start();


//        }
    }
}
