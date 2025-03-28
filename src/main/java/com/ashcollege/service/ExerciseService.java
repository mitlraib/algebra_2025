package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepo;

    private final Random rand = new Random();
    private final String[] names = {"נועה", "דני", "רוני", "יואב", "מיקה", "תמר", "איתי", "איילה"};
    private final String[] fruits = {"תפוחים", "אגסים", "תפוזים", "בננות", "שזיפים", "ענבים"};
    private final String[] templatesAdd = {
            "%s קיבל/ה %d %s, ואז קיבל/ה עוד %d. כמה יש לו/ה בסך הכול?",
            "ל%s היו %d %s. לאחר מכן קיבל/ה עוד %d. כמה יש לו/ה עכשיו?",
            "%s קיבל/ה %d %s ועוד %d נוספים. כמה יש לו/ה עכשיו?"
    };
    private final String[] templatesSubtract = {
            "%s קיבל/ה %d %s ונתן/ה %d לחבר. כמה נשארו לו/ה?",
            "ל%s היו %d %s. לאחר שנתן/ה %d מהם, כמה נשארו לו/ה?",
            "%s אסף/ה %d %s ואיבד/ה %d בדרך. כמה נותרו לו/ה?"
    };

    /**
     * מחולל שאלה בהתאם לנושא (topicId) + רמת המשתמש (רמה ללא הגבלה).
     */
    public Map<String, Object> generateQuestion(int topicId) {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("No current user found");
        }

        // שליפת רמת המשתמש לנושא
        UserTopicLevelEntity ute = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        if (ute == null) {
            ute = new UserTopicLevelEntity();
            ute.setUserId(user.getId());
            ute.setTopicId(topicId);
            ute.setLevel(1); // התחל מרמה 1
            ute.setMistakes(0); // הוספנו שדה mistakes בדוגמא
            userTopicLevelRepo.save(ute);
        }
        int currentLevel = ute.getLevel();
        logger.info("Topic {} => CurrentLevel={}", topicId, currentLevel);

        // מחולל שאלה רגילה או שבר
        Map<String, Object> question;
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
                question = generateFractionQuestion("+", currentLevel);
                break;
            case 6:
                question = generateFractionQuestion("-", currentLevel);
                break;
            case 7:
                question = generateFractionQuestion("×", currentLevel);
                break;
            case 8:
                question = generateFractionQuestion("÷", currentLevel);
                break;
            default:
                question = generateBasicArithmetic("+", currentLevel);
        }

        question.put("topicId", topicId);
        return question;
    }

    /**
     * העלאת רמה בנושא.
     */
    public void increaseUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            int oldLevel = rec.getLevel();
            rec.setLevel(oldLevel + 1);
            userTopicLevelRepo.save(rec);
            logger.info("User {} in topic {} => level up from {} to {}",
                    userId, topicId, oldLevel, rec.getLevel());
        }
    }

    /**
     * מחזיר את הרמה הנוכחית של המשתמש בנושא.
     */
    public int getUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec == null) return 1;
        return rec.getLevel();
    }

    /**
     * בודק אם התשובה נכונה ע"פ `correctAnswer`.
     */
    public boolean checkAnswer(Map<String, Object> question, int userAnswer) {
        int correct = (int) question.get("correctAnswer");
        return (userAnswer == correct);
    }

    /**
     * מגדיל את כמות ה-mistakes למשתמש בנושא מסוים.
     */
    public void incrementTopicMistakes(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setMistakes(rec.getMistakes() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    // ---------- להלן מחוללי השאלות ----------

    // חיבור/חיסור/כפל/חילוק בהתאם לרמה
    private Map<String, Object> generateBasicArithmetic(String sign, int level) {
        int maxVal = level * 5;
        if (maxVal < 5) {
            maxVal = 5;
        }

        // 50% מהשאלות יהיו מילוליות (אם מתאימות)
        if ((sign.equals("+") || sign.equals("-")) && rand.nextDouble() < 0.5) {
            return generateWordProblem(sign, level);
        }

        // שאלות רגילות
        int a = 0, b = 0, correct = 0;
        boolean valid = false;

        while (!valid) {
            a = rand.nextInt(maxVal) + 1;
            b = rand.nextInt(maxVal) + 1;

            switch (sign) {
                case "+":
                    correct = a + b;
                    valid = true;
                    break;
                case "-":
                    if (a >= b) {
                        correct = a - b;
                        valid = true;
                    }
                    break;
                case "×":
                    correct = a * b;
                    valid = true;
                    break;
                case "÷":
                    if (b != 0 && (a % b == 0)) {
                        correct = a / b;
                        valid = true;
                    }
                    break;
            }
        }

        int[] answers = new int[]{
                correct,
                correct + 1,
                Math.max(0, correct - 1),
                correct + 2
        };
        shuffleArray(answers);

        Map<String, Object> q = new HashMap<>();
        q.put("first", a);
        q.put("second", b);
        q.put("operationSign", sign);
        q.put("correctAnswer", correct);
        q.put("answers", answers);
        return q;
    }

    private Map<String, Object> generateWordProblem(String sign, int level) {
        int maxVal = Math.max(5, level * 5);
        String name = names[rand.nextInt(names.length)];
        String fruit = fruits[rand.nextInt(fruits.length)];

        int a = rand.nextInt(maxVal) + 1;
        int b = rand.nextInt(maxVal) + 1;
        int correct;

        String template;

        if (sign.equals("-")) {
            // נוודא a >= b
            while (a < b) {
                a = rand.nextInt(maxVal) + 1;
                b = rand.nextInt(maxVal) + 1;
            }
            correct = a - b;
            template = templatesSubtract[rand.nextInt(templatesSubtract.length)];
        } else {
            correct = a + b;
            template = templatesAdd[rand.nextInt(templatesAdd.length)];
        }

        String questionText = String.format(template, name, a, fruit, b);

        int[] answers = new int[]{
                correct,
                correct + 1,
                Math.max(0, correct - 1),
                correct + 2
        };
        shuffleArray(answers);

        Map<String, Object> q = new HashMap<>();
        q.put("questionText", questionText);
        q.put("operationSign", "word" ); // סימון שזה שאלה מילולית
        q.put("correctAnswer", correct);
        q.put("answers", answers);
        return q;
    }

    // שברים
    private Map<String, Object> generateFractionQuestion(String sign, int level) {
        int[] frac = createFractionPair(level); // מחזיר [num1, den1, num2, den2]
        int a = frac[0], b = frac[1];
        int c = frac[2], d = frac[3];

        // אם חיסור, לוודא a/b >= c/d:
        if (sign.equals("-")) {
            if ((long) a * d < (long) c * b) {
                return generateFractionQuestion(sign, level);
            }
        }

        int num = 0, den = 0;
        switch (sign) {
            case "+":
                if (b == d) {
                    num = a + c;
                    den = b;
                } else {
                    num = a * d + b * c;
                    den = b * d;
                }
                break;
            case "-":
                if (b == d) {
                    num = a - c;
                    den = b;
                } else {
                    num = a * d - b * c;
                    den = b * d;
                }
                break;
            case "×":
                num = a * c;
                den = b * d;
                break;
            case "÷":
                num = a * d;
                den = b * c;
                break;
        }

        if (num < 0 || den <= 0) {
            return generateFractionQuestion(sign, level);
        }

        int correctEncoded = num * 1000 + den;
        int[] answers = new int[4];
        answers[0] = correctEncoded;
        answers[1] = (num + 1) * 1000 + den;
        answers[2] = Math.max(1, num - 1) * 1000 + den;
        answers[3] = num * 1000 + Math.max(1, den + 1);
        shuffleArray(answers);

        Map<String, Object> q = new HashMap<>();
        q.put("first", a + "/" + b);
        q.put("second", c + "/" + d);
        q.put("operationSign", "frac" + sign); // סתם כדי לזהות שזה שבר
        q.put("correctAnswer", correctEncoded);
        q.put("answers", answers);

        return q;
    }

    private int[] createFractionPair(int level) {
        // רמה => משפיע על מכנים וכו'
        if (level < 1) level = 1;

        boolean sameDen = false;
        boolean differDen = false;
        int maxDen = 5;
        int forcedNum = -1;

        if (level == 1) {
            sameDen = true; maxDen = 5;
        } else if (level == 2) {
            sameDen = true; maxDen = 10;
        } else if (level == 3) {
            sameDen = true; maxDen = 20;
        } else if (level == 4) {
            differDen = true; maxDen = 5; forcedNum = 1;
        } else if (level == 5) {
            differDen = true; maxDen = 10; forcedNum = 1;
        } else {
            differDen = true;
            int offset = 5 * (level - 5);
            if (offset < 5) offset = 5;
            maxDen = offset;
            forcedNum = 0;
        }

        if (sameDen) {
            int den = rand.nextInt(maxDen - 1) + 2;
            int n1 = rand.nextInt(den) + 1;
            int n2 = rand.nextInt(den) + 1;
            return new int[]{n1, den, n2, den};
        }

        int[] f1 = createSingleFraction(maxDen, forcedNum);
        int[] f2 = createSingleFraction(maxDen, forcedNum);

        if (level <= 5) {
            while (f2[1] == f1[1]) {
                f2 = createSingleFraction(maxDen, forcedNum);
            }
        }
        return new int[]{f1[0], f1[1], f2[0], f2[1]};
    }

    private int[] createSingleFraction(int maxDen, int forcedNum) {
        int den = rand.nextInt(maxDen - 1) + 2;
        int num;
        if (forcedNum > 0) {
            num = forcedNum;
        } else if (forcedNum == 0) {
            num = rand.nextInt(5) + 1;
        } else {
            num = rand.nextInt(den) + 1;
        }
        return new int[]{num, den};
    }

    private void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}
