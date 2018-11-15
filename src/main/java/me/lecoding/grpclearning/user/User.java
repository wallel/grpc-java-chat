package me.lecoding.grpclearning.user;

public class User {
    private String userName;
    private String token;
    public User(String name){
        this.userName = name;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
