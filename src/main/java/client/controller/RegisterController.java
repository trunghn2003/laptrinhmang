package client.controller;



import client.model.ResponseResult;
import server.model.User;

public class RegisterController {
    private ClientControl clientControl;


    public RegisterController(ClientControl clientControl) {
        this.clientControl = clientControl;
    }

    public ResponseResult register(String username, String password) {
        User newUser = new User(username, password, "register");
        clientControl.openConnection();
        clientControl.sendData(newUser);
        String result = (String) clientControl.receiveData();
        clientControl.closeConnection();
//        System.out.println(result);

        return new ResponseResult(true, result);
    }
}
