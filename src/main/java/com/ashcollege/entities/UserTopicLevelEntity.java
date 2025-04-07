package com.ashcollege.entities;

import javax.persistence.*;

// UserTopicLevelEntity.java
@Entity
@Table(name = "user_topic_levels")
public class UserTopicLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;   // מזהה המשתמש
    private int topicId;  // מזהה הנושא
    private int level;    // רמת המשתמש בנושא
    private int mistakes; // ספירת טעויות באותו נושא
    private int attempts; // ספירת ניסיונות (חדש)


    public UserTopicLevelEntity() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTopicId() {
        return topicId;
    }
    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getMistakes() {
        return mistakes;
    }
    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }
    public int getAttempts() {
        return attempts;
    }
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
