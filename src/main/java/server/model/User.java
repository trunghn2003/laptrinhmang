package server.model;

import java.io.Serializable;
public class User implements Serializable{
    private String userName
            ;
    private String password
            ;

    public User(){ }
    public User(String username, String password){
        this
                .userName = username;
        this
                .password = password;

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

}