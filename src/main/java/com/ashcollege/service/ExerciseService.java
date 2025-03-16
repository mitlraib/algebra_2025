package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import com.ashcollege.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class ExerciseService {
    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);

    private final Random rand = new Random();

    @Autowired
    private UserService userService;  // הוספת ה-UserService כאן

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepo; // ייבוא של המחלקה UserTopicLevelRepository

    /**
     * מייצר שאלה בהתאם לרמת המשתמש ול-topicId.
     * מחזיר Map עם:
     *  {
     *     "first": int,
     *     "second": int,
     *     "operationSign": "+"/"-"/"×"/"÷",
     *     "correctAnswer": int,
     *     "answers": int[]",
     *  }
     */
    public Map<String, Object> generateQuestion(int topicId, int userLevel) {

        logger.info("User Level: {}", userLevel);

        // לקבל את המשתמש הנוכחי
        UserEntity user = userService.getCurrentUser();  // שיטה שמחזירה את המשתמש הנוכחי

        // מציאת רמת המשתמש בנושא הרלוונטי
        UserTopicLevelEntity userTopicLevel = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        int currentLevel = (userTopicLevel != null) ? userTopicLevel.getLevel() : 1; // ברירת מחדל 1

        // נקבע את הסימן והלוגיקה על פי topicId
        String sign = topicIdToSign(topicId);

        int first = 0, second = 0, correct = 0;
        boolean valid = false;

        while (!valid) {
            if (currentLevel <= 1) {
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
                    int c = rand.nextInt(currentLevel <= 1 ? 3 : 9) + 1;
                    second = rand.nextInt(currentLevel <= 1 ? 3 : 9) + 1;
                    first = c * second;
                    correct = c;
                    break;
            }

            if (currentLevel <= 1) {
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
            case 1: return "+"; // חיבור
            case 2: return "-"; // חיסור
            case 3: return "×"; // כפל
            case 4: return "÷"; // חילוק
            case 5: return "fracAdd";    // חיבור שברים
            case 6: return "fracSub";    // ...
            case 7: return "fracMul";
            case 8: return "fracDiv";
        }
        return "+"; // ברירת מחדל
    }
}
