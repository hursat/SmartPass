package com.example.hursat.smartpass2;

/**
 * Created by hursat on 27.11.2016.
 */

public class User {

    public String email;
    public String name;
    public String surname;
    public String uid;

    public User() {
        this.email = "deneme";
        this.name = "deneme2";
        this.surname = "deneme3";
    }

    public User(String email, String name, String surname) {
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    public User(String email, String name, String surname, String uid) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.uid = uid;
    }

}
