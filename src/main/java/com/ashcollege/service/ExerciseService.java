package com.ashcollege.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class ExerciseService {

    private final Random rand = new Random();

    /**
     * מייצר שאלה בהתאם לרמת המשתמש ול-topicId.
     * מחזיר Map עם:
     *  {
     *     "first": int,
     *     "second": int,
     *     "operationSign": "+"/"-"/"×"/"÷",
     *     "correctAnswer": int,
     *     "answers": int[],
     *  }
     */
    public Map<String, Object> generateQuestion(int topicId, int userLevel) {
        // נקבע את הסימן והלוגיקה על פי topicId
        String sign = topicIdToSign(topicId);

        int first = 0, second = 0, correct = 0;
        boolean valid = false;

        while (!valid) {
            if (userLevel == 1) {
                first = rand.nextInt(9) + 1;
                second = rand.nextInt(9) + 1;
            } else {
                first = rand.nextInt(99) + 1;
                second = rand.nextInt(99) + 1;
            }

            switch (sign) {
                case "+":
                    correct = first + second;
                    break;
                case "-":
                    if (second > first) {
                        int tmp = first;
                        first = second;
                        second = tmp;
                    }
                    correct = first - second;
                    break;
                case "×":
                    correct = first * second;
                    break;
                case "÷":
                    int c = rand.nextInt(userLevel == 1 ? 3 : 9) + 1;
                    second = rand.nextInt(userLevel == 1 ? 3 : 9) + 1;
                    first = c * second;
                    correct = c;
                    break;
            }

            if (userLevel == 1) {
                if (correct <= 10 && first <= 10 && second <= 10) {
                    valid = true;
                }
            } else {
                valid = true;
            }
        }

        int[] answers = new int[] {
                correct,
                Math.max(1, correct + 1),
                Math.max(1, correct > 0 ? correct - 1 : correct + 2),
                Math.max(1, correct + 5)
        };
        shuffleArray(answers);

        Map<String, Object> question = new HashMap<>();
        question.put("first", first);
        question.put("second", second);
        question.put("operationSign", sign);
        question.put("correctAnswer", correct);
        question.put("answers", answers);

        return question;
    }

    /**
     * בודק אם userAnswer == correctAnswer
     */
    public boolean checkAnswer(Map<String, Object> question, int userAnswer) {
        int correct = (int) question.get("correctAnswer");
        return (userAnswer == correct);
    }

    private void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    private String topicIdToSign(int topicId) {
        switch (topicId) {
            case 1: return "+";
            case 2: return "-";
            case 3: return "×";
            case 4: return "÷";
        }
        return "+"; // ברירת מחדל
    }
}
