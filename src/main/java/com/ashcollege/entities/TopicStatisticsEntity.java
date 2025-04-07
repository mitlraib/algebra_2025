package com.ashcollege.entities;

public class TopicStatisticsEntity {
    private int topicId;
    private int totalAttempts;
    private int totalMistakes;
    private double successRate;

    public TopicStatisticsEntity(int topicId, int totalAttempts, int totalMistakes, double successRate) {
        this.topicId = topicId;
        this.totalAttempts = totalAttempts;
        this.totalMistakes = totalMistakes;
        this.successRate = successRate;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
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
}