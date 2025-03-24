// UserTopicLevelEntity.java
package com.ashcollege.entities;

import javax.persistence.*;

// UserTopicLevelEntity.java
@Entity
@Table(name = "user_topic_levels")
public class UserTopicLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private int topicId;
    private int level;

    // הוספה חדשה: מספר שגיאות מצטבר באותו נושא
    private int mistakes = 0;

    public UserTopicLevelEntity() {}

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
}
