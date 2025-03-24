package com.ashcollege.entities;

import javax.persistence.*;

@Entity
@Table(name = "my_users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String firstName;
    private String lastName;
    private String mail;
    private String password;
    private int totalExercises = 0;
    private int totalMistakes = 0;



    // רמת המשתמש בכללי
    private int level;

    // -- תוספת: role:
    private String role; // למשל "ADMIN" או "STUDENT"

    public UserEntity() {
    }

    // --- Getters & Setters ---
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMail() {
        return mail;
    }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) { this.password = password; }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) { this.level = level; }

    public int getTotalExercises() {
        return totalExercises;
    }
    public void setTotalExercises(int totalExercises) {
        this.totalExercises = totalExercises;
    }

    public int getTotalMistakes() {
        return totalMistakes;
    }
    public void setTotalMistakes(int totalMistakes) {
        this.totalMistakes = totalMistakes;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    @Override

    public String toString() {

        return "UserEntity{" +

                "mail='" + mail + '\'' +

                ", role=" + role +

                ", level=" + level +

                ", password='" + password + '\'' +

                '}';

    }

}
