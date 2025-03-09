package com.ashcollege.entities;

public class QuestionEntity {
    private String questionText;
    private int answer;
    private String explanation;

    public QuestionEntity(String questionText, int answer, String explanation) {
        this.questionText = questionText;
        this.answer = answer;
        this.explanation = explanation;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getAnswer() {
        return answer;
    }

    public String getExplanation() {
        return explanation;
    }
}