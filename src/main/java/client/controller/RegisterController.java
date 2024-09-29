package client.controller;



import server.model.User;

public class RegisterController {
    private ClientControl clientControl;

    public RegisterController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public boolean register(String username, String password) {
        User newUser = new User(username, password, "register");
        clientControl.openConnection();
        clientControl.sendData(newUser);
        String result = clientControl.receiveData();
        clientControl.closeConnection();
        return "ok".equals(result);
    }
}
