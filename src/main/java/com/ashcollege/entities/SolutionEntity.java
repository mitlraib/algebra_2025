package com.ashcollege.entities;

    public class SolutionEntity extends BaseEntity {
    private String finalResult;
    private String fullSolution;
    private ExerciseEntity exercise;

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getFullSolution() {
        return fullSolution;
    }

    public void setFullSolution(String fullSolution) {
        this.fullSolution = fullSolution;
    }

    public ExerciseEntity getExercise() {
        return exercise;
    }

    public void setExercise(ExerciseEntity exercise) {
        this.exercise = exercise;
    }
}
