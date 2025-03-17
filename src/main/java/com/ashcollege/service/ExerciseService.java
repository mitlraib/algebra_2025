package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserRepository;
import com.ashcollege.repository.UserTopicLevelRepository;
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
    private UserService userService;

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepo;

    /**
     * מייצר שאלה בהתאם ל-topicId, לפי הרמה של המשתמש באותו נושא.
     * משתמשים בשדה user_topic_levels (אם אין רשומה, ניצור אותה עם level=1).
     */
    public Map<String, Object> generateQuestion(int topicId) {
        // 1) מוצאים את המשתמש הנוכחי
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("No current user found");
        }

        // 2) שליפת רמה לנושא (או יצירת רשומת רמה 1 אם לא קיימת)
        UserTopicLevelEntity userTopicLevel = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        if (userTopicLevel == null) {
            userTopicLevel = new UserTopicLevelEntity();
            userTopicLevel.setUserId(user.getId());
            userTopicLevel.setTopicId(topicId);
            userTopicLevel.setLevel(1);
            userTopicLevelRepo.save(userTopicLevel);
        }
        int currentLevel = userTopicLevel.getLevel();
        logger.info("Topic {} => CurrentLevel={}", topicId, currentLevel);

        // 3) ניצור שאלה בהתאם לנושא (חיבור/חיסור/כפל/חילוק/שברים)
        Map<String, Object> question = new HashMap<>();
        switch (topicId) {
            case 1:
                question = generateBasicArithmetic("+", currentLevel);
                break;
            case 2:
                question = generateBasicArithmetic("-", currentLevel);
                break;
            case 3:
                question = generateBasicArithmetic("×", currentLevel);
                break;
            case 4:
                question = generateBasicArithmetic("÷", currentLevel);
                break;
            case 5:
                question = generateFractions("+", currentLevel);
                break;
            case 6:
                question = generateFractions("-", currentLevel);
                break;
            case 7:
                question = generateFractions("×", currentLevel);
                break;
            case 8:
                question = generateFractions("÷", currentLevel);
                break;
            default:
                // ברירת מחדל, חיבור
                question = generateBasicArithmetic("+", currentLevel);
                break;
        }

        // נוסיף ל-Map גם את topicId לצורך מעקב
        question.put("topicId", topicId);

        return question;
    }

    /**
     * מעלה את רמת המשתמש רק עבור topicId נתון.
     */
    public void increaseUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setLevel(rec.getLevel() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    /**
     * שליפת הרמה הנוכחית של המשתמש בנושא
     */
    public int getUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec == null) {
            return 1;
        }
        return rec.getLevel();
    }

    /**
     * בדיקת תשובה (int userAnswer) מול correctAnswer.
     */
    public boolean checkAnswer(Map<String, Object> question, int userAnswer) {
        int correct = (int) question.get("correctAnswer");
        return (userAnswer == correct);
    }

    // --------------------- לוגיקת יצירת תרגילים רגילים (חיבור/חיסור/כפל/חילוק) ---------------------

    private Map<String, Object> generateBasicArithmetic(String sign, int level) {
        // נגריל first ו-second בהתאם לרמה
        int first = 0, second = 0, correct = 0;
        boolean valid = false;

        while (!valid) {
            if (level <= 1) {
                first = rand.nextInt(9) + 1;    // 1..9
                second = rand.nextInt(9) + 1;
            } else {
                first = rand.nextInt(99) + 1;  // 1..99
                second = rand.nextInt(99) + 1;
            }

            switch (sign) {
                case "+":
                    correct = first + second;
                    valid = true;
                    break;
                case "-":
                    // נוודא שלא נקבל תוצאה שלילית (לטובת רמה התחלתית)
                    if (first >= second) {
                        correct = first - second;
                        valid = true;
                    }
                    break;
                case "×":
                    correct = first * second;
                    valid = true;
                    break;
                case "÷":
                    // בגרסה פשוטה: second בין 1..9, נוודא שהראשון מתחלק ב-second
                    if (level <= 1) {
                        second = rand.nextInt(9) + 1;
                        first = (rand.nextInt(9) + 1) * second;  // ככה הוא מתחלק
                    } else {
                        second = rand.nextInt(9) + 1;
                        first = (rand.nextInt(15) + 1) * second;
                    }
                    correct = first / second;
                    valid = true;
                    break;
            }
        }

        // תשובות
        int[] answers = new int[]{
                correct,
                correct + 1,
                Math.max(0, correct - 1),
                correct + 2
        };
        shuffleArray(answers);

        Map<String, Object> q = new HashMap<>();
        q.put("first", first);
        q.put("second", second);
        q.put("operationSign", sign);
        q.put("correctAnswer", correct);
        q.put("answers", answers);

        return q;
    }

    // --------------------- לוגיקת יצירת תרגילי שברים (חיבור שברים וכו') ---------------------

    private Map<String, Object> generateFractions(String sign, int level) {
        // נגריל שברים במכנה בין 2..9, מונה קטן מהמכנה
        int denominator1 = rand.nextInt(8) + 2; // 2..9
        int denominator2 = rand.nextInt(8) + 2;
        int numerator1 = rand.nextInt(denominator1 - 1) + 1; // 1..(denominator1-1)
        int numerator2 = rand.nextInt(denominator2 - 1) + 1;

        // נהפוך ל-(a/b) (c/d)
        // נחשב תשובה "נכונה" כשבר, ואז נהפוך למונה שלם (לצורך השוואה) = crossMultiply
        int correctNum = 0;
        int correctDen = 0;

        switch (sign) {
            case "+":
                // a/b + c/d => (ad + bc) / bd
                correctNum = numerator1 * denominator2 + numerator2 * denominator1;
                correctDen = denominator1 * denominator2;
                break;
            case "-":
                // a/b - c/d => (ad - bc) / bd
                correctNum = numerator1 * denominator2 - numerator2 * denominator1;
                correctDen = denominator1 * denominator2;
                break;
            case "×":
                // (a/b)*(c/d) => (ac)/(bd)
                correctNum = numerator1 * numerator2;
                correctDen = denominator1 * denominator2;
                break;
            case "÷":
                // (a/b) / (c/d) => (a/b)*(d/c) = ad/bc
                correctNum = numerator1 * denominator2;
                correctDen = denominator1 * numerator2;
                break;
        }

        // נוכל לייצג את התשובה הנכונה כ"מונה שלם" correctNum, ו"מכנה שלם" correctDen
        // אבל כדי להשוות תשובות בצורה פשוטה, נהפוך int answer = correctNum * BIG + correctDen ...
        // יש דרכים אחרות. נשתמש פשוט במונה כ"correctAnswer" אם נרצה. אבל כאן לצורך הדמו:

        // אסטרטגיה: נשמור את "correctAnswer" כמספר שלם = (correctNum << 16) + correctDen
        // רק בשביל שתהיה לנו דרך לבדוק userAnswer == correctAnswer. אבל אפשר גם מפוצל.
        // כדי לא לסבך, נחשיב "correctAnswer" = correctNum * 1000 + correctDen (בתקווה שלא יעבור מכפל).
        int correctEncoded = correctNum * 1000 + correctDen;

        // התצוגה: "1/2 + 1/4"
        String frac1 = numerator1 + "/" + denominator1;
        String frac2 = numerator2 + "/" + denominator2;

        // יצירת 4 תשובות, שכולן בצורה "X/Y" מקודדת => נמיר לEncoded.
        int[] answersEncoded = new int[4];
        answersEncoded[0] = correctEncoded;
        answersEncoded[1] = (correctNum + 1) * 1000 + correctDen; // קצת אקראי
        answersEncoded[2] = Math.max(1, correctNum - 1) * 1000 + correctDen;
        answersEncoded[3] = correctNum * 1000 + Math.max(1, correctDen + 1);

        shuffleArray(answersEncoded);

        // מעבירים למערך String בצורת "X/Y"
        String[] displayAnswers = new String[4];
        for (int i = 0; i < 4; i++) {
            int code = answersEncoded[i];
            int num = code / 1000;
            int den = code % 1000;
            displayAnswers[i] = num + "/" + den;
        }

        // נבנה את המפה
        Map<String, Object> q = new HashMap<>();
        q.put("first", frac1);   // למשל "1/2"
        q.put("second", frac2);  // "1/4"
        q.put("operationSign", sign);
        q.put("correctAnswer", correctEncoded);  // קידוד
        q.put("answers", answersEncoded);         // מערך int מקודד
        return q;
    }

    /**
     * מערבב את המערך במקום
     */
    private void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

}
