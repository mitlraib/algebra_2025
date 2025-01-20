package com.ashcollege.entities;

public class StudentEntity extends  UserEntity {

    private int difficultyLevel;
    private String brakePoint;
//    private double grading;
//    private Time timeSpent;
//    private ExerciseHistory exerciseHistory;


    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getBrakePoint() {
        return brakePoint;
    }

    public void setBrakePoint(String brakePoint) {
        this.brakePoint = brakePoint;
    }




}
