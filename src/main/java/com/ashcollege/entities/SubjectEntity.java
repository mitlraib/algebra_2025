package com.ashcollege.entities;

public class SubjectEntity extends BaseEntity{
    private String subjectName;
    private String description;
    private ExerciseEntity exercise;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExerciseEntity getExercise() {
        return exercise;
    }

    public void setExercise(ExerciseEntity exercise) {
        this.exercise = exercise;
    }

    public SubjectEntity() {
    }

    public SubjectEntity(String subjectName) {
        this.subjectName = subjectName;
    }
}
