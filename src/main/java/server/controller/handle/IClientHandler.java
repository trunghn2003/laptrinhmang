package server.controller.handle;

import java.io.ObjectOutputStream;
import server.model.User;

public interface IClientHandler {
    void handleUserRequest(User user, ObjectOutputStream oos);
    void closeClientSocket();
}
