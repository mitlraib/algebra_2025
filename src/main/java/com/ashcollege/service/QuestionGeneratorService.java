package com.ashcollege.service;

import com.ashcollege.entities.QuestionEntity;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class QuestionGeneratorService {

    public QuestionEntity createQuestion(String type) {
        // יצירת שאלה בהתאם לסוג שנשלח
        if ("addition".equalsIgnoreCase(type)) {
            return createAdditionQuestion();
        } else if ("subtraction".equalsIgnoreCase(type)) {
            return createSubtractionQuestion();
        } else if ("multiplication".equalsIgnoreCase(type)) {
            return createMultiplicationQuestion();
        } else if ("division".equalsIgnoreCase(type)) {
            return createDivisionQuestion();
        }
        return null; // אם סוג השאלה לא מוכר
    }

    private QuestionEntity createAdditionQuestion() {
        Random random = new Random();
        int num1 = random.nextInt(50) + 1;
        int num2 = random.nextInt(50) + 1;
        String questionText = num1 + " + " + num2;
        int answer = num1 + num2;
        String explanation = "כדי לפתור, יש לחבר את " + num1 + " ו-" + num2;
        return new QuestionEntity(questionText, answer, explanation);
    }

    private QuestionEntity createSubtractionQuestion() {
        Random random = new Random();
        int num1 = random.nextInt(50) + 1;
        int num2 = random.nextInt(50) + 1;
        String questionText = num1 + " - " + num2;
        int answer = num1 - num2;
        String explanation = "כדי לפתור, יש לחסר את " + num2 + " מ-" + num1;
        return new QuestionEntity(questionText, answer, explanation);
    }

    private QuestionEntity createMultiplicationQuestion() {
        Random random = new Random();
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        String questionText = num1 + " × " + num2;
        int answer = num1 * num2;
        String explanation = "כדי לפתור, יש להכפיל את " + num1 + " ב-" + num2;
        return new QuestionEntity(questionText, answer, explanation);
    }

    private QuestionEntity createDivisionQuestion() {
        Random random = new Random();
        int num1 = (random.nextInt(10) + 1) * 10; // מספר שמתחלק ב-10
        int num2 = random.nextInt(10) + 1;
        String questionText = num1 + " ÷ " + num2;
        int answer = num1 / num2;
        String explanation = "כדי לפתור, יש לחלק את " + num1 + " ב-" + num2;
        return new QuestionEntity(questionText, answer, explanation);
    }
}