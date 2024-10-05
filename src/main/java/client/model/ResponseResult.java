package client.model;

import server.model.User;

import java.util.List;

public class ResponseResult {
    private boolean success;
    private String message;
    private List<User> data;

    public ResponseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }
}
