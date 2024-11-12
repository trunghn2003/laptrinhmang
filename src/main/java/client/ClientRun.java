package client;

import client.view.LoginView;
import javafx.application.Application;
import client.view.MainView;

public class ClientRun {
    public static void main(String[] args) {
        Application.launch(LoginView.class, args);
    }
}
