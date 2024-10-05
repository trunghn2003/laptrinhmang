package server.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private String password;
    private String actionType;

    private String name;
    private int score;
    private int status;
    private int id;
    private int clientId = -1;


    public User() { }


    public User(String userName, String password, String actionType) {
        this.userName = userName;
        this.password = password;
        this.actionType = actionType;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "User: " + this.userName + ", Score: " + this.score + ", Status: " + this.status;
    }
}
