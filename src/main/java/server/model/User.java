package server.model;

import java.io.Serializable;
public class User implements Serializable{
    private String userName
            ;
    private String password
            ;
    private String actionType;

    public User(){ }

    public User(String userName, String password, String actionType) {
        this.userName = userName;
        this.password = password;
        this.actionType = actionType;
    }

    public String getPassword() {
        return password
                ;

    }
    public void setPassword(String password) {
        this
                .password = password;

    }
    public String getUserName() {
        return userName
                ;

    }
    public void setUserName(String userName) {
        this
                .userName = userName;

    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}