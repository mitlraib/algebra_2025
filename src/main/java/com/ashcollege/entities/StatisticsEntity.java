package com.ashcollege.entities;

public class StatisticsEntity {
    private int totalAttempts;
    private int totalMistakes;
    private double successRate;
    private int mostDifficultTopic;
    private int easiestTopic;

    public StatisticsEntity(int totalAttempts, int totalMistakes, double successRate, int mostDifficultTopic, int easiestTopic) {
        this.totalAttempts = totalAttempts;
        this.totalMistakes = totalMistakes;
        this.successRate = successRate;
        this.mostDifficultTopic = mostDifficultTopic;
        this.easiestTopic = easiestTopic;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getTotalMistakes() {
        return totalMistakes;
    }

    public void setTotalMistakes(int totalMistakes) {
        this.totalMistakes = totalMistakes;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public int getMostDifficultTopic() {
        return mostDifficultTopic;
    }

    public void setMostDifficultTopic(int mostDifficultTopic) {
        this.mostDifficultTopic = mostDifficultTopic;
    }

    public int getEasiestTopic() {
        return easiestTopic;
    }

    public void setEasiestTopic(int easiestTopic) {
        this.easiestTopic = easiestTopic;
    }
}