package server.model;

import java.io.Serializable;

public class ResponseResult implements Serializable {
    private boolean success;
    private String message;
    private Object data;

    public ResponseResult() { }

    public ResponseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getter và Setter

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
