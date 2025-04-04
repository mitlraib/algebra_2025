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
    private final String[] boysNames = {"לביא", "דני", "ליאם", "יואב", "חיים", "דניאל", "איתי", "אדיר"};
    private final String[] girlsNames = {"נועה", "דניאלה", "רוני", "חנה", "מיקה", "תמר", "אתי", "איילה"};
    private final String[] objects = {"מחברות", "ספרים", "עפרונות", "עטים", "יומנים", "קלסרים"};
    private final String[] buildings = {"בתים", "דירות", "מגדלים", "מגדלי לגו", "ארמונות מחול", "אוהלים"};
    private final String[] fruits = {"תפוחים", "אגסים", "תפוזים", "בננות", "שזיפים", "ענבים"};
    private final String[] templatesAdd = {
            "%s קיבל/ה %s, ואז קיבל/ה עוד %s. כמה יש לו/ה בסך הכול?",
            "ל%s היו %s. לאחר מכן קיבל/ה עוד %s. כמה יש לו/ה עכשיו?",
            "%s קיבל/ה %s ועוד %s נוספים. כמה יש לו/ה עכשיו?",
            "כמה זה %s ועוד %s?",
            "%s מצא/ה %s, ואז מצא/ה עוד %s. כמה יש לו/ה עכשיו?",
            "%s אסף/ה %s, ואחר כך הוסיפו לו/ה עוד %s. כמה יש לו/ה?",
            "%s בנה/בנתה %s, ואז בנה/בנתה עוד %s. כמה בנה/בנתה בסך הכול?",
            "%s קנה/קנתה %s, ואחר כך קנה/קנתה עוד %s. כמה קנה/קנתה?",
            "בהתחלה היו ל%s %s, ואז הגיעו עוד %s. כמה יש לו/ה עכשיו?",
            "אם %s התחיל/ה עם %s והצטרפו עוד %s, כמה יש לו/ה?"
    };

    private final String[] templatesSubtract = {
            "%s קיבל/ה %s ונתן/ה %s לחבר. כמה נשארו לו/ה?",
            "ל%s היו %s. לאחר שנתן/ה %s מהם, כמה נשארו לו/ה?",
            "%s אסף/ה %s ואיבד/ה %s בדרך. כמה נותרו לו/ה?",
            "כמה זה %s פחות %s?",
            "%s קיבל/ה %s, אבל איבד/ה %s. כמה נשארו לו/ה?",
            "ל%s היו %s, והוא/היא חילק/ה %s מהם. כמה נשארו אצלו/ה?",
            "%s התחיל/ה עם %s, ואחר כך לקחו לו/ה %s. כמה נותרו?",
            "%s קנה/קנתה %s, ומכר/ה %s. כמה נשארו?",
            "%s היה/הייתה עם %s, ואיבד/ה %s. כמה יש לו/ה עכשיו?",
            "אם ל%s היו %s והוא/היא נתן/ה %s, כמה נשארו?"
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
            ute.setMistakes(0);
            userTopicLevelRepo.save(ute);
        }
        int currentLevel = ute.getLevel();
        logger.info("Topic {} => CurrentLevel={}", topicId, currentLevel);

        // מחולל שאלה רגילה או שבר
        Map<String, Object> question;
        switch (topicId) {
            case 1: question = generateBasicArithmetic("+", currentLevel); break;
            case 2: question = generateBasicArithmetic("-", currentLevel); break;
            case 3: question = generateBasicArithmetic("×", currentLevel); break;
            case 4: question = generateBasicArithmetic("÷", currentLevel); break;
            case 5: question = generateFractionQuestion("+", currentLevel); break;
            case 6: question = generateFractionQuestion("-", currentLevel); break;
            case 7: question = generateFractionQuestion("×", currentLevel); break;
            case 8: question = generateFractionQuestion("÷", currentLevel); break;
            default: question = generateBasicArithmetic("+", currentLevel);
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

        // 50% מהשאלות יהיו מילוליות (אם מתאימות לחיבור/חיסור)
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

        String name;
        boolean isBoy;
        if (rand.nextBoolean()) {
            name = boysNames[rand.nextInt(boysNames.length)];
            isBoy = true;
        } else {
            name = girlsNames[rand.nextInt(girlsNames.length)];
            isBoy = false;
        }

        int a = rand.nextInt(maxVal) + 1;
        int b = rand.nextInt(maxVal) + 1;
        int correct;

        String template;
        if (sign.equals("-")) {
            int tries = 0;
            while (a < b && tries < 1000) {
                a = rand.nextInt(maxVal) + 1;
                b = rand.nextInt(maxVal) + 1;
                tries++;
            }
            if (tries >= 1000) {
                a = Math.max(1, maxVal);
                b = 1;
            }
            correct = a - b;
            template = templatesSubtract[rand.nextInt(templatesSubtract.length)];
        } else {
            correct = a + b;
            template = templatesAdd[rand.nextInt(templatesAdd.length)];
        }

        // החלפת לשון זכר/נקבה
        template = template
                .replace("קיבל/ה", isBoy ? "קיבל" : "קיבלה")
                .replace("נתן/ה", isBoy ? "נתן" : "נתנה")
                .replace("אסף/ה", isBoy ? "אסף" : "אספה")
                .replace("מצא/ה", isBoy ? "מצא" : "מצאה")
                .replace("התחיל/ה", isBoy ? "התחיל" : "התחילה")
                .replace("היו לו/ה", isBoy ? "היו לו" : "היו לה")
                .replace("נשארו לו/ה", isBoy ? "נשארו לו" : "נשארו לה")
                .replace("יש לו/ה", isBoy ? "יש לו" : "יש לה")
                .replace("חילק/ה", isBoy ? "חילק" : "חילקה")
                .replace("איבד/ה", isBoy ? "איבד" : "איבדה")
                .replace("לקחו לו/ה", isBoy ? "לקחו לו" : "לקחו לה")
                .replace("קנה/קנתה", isBoy ? "קנה" : "קנתה")
                .replace("בנה/בנתה", isBoy ? "בנה" : "בנתה")
                .replace("הוסיפו לו/ה", isBoy ? "הוסיפו לו" : "הוסיפו לה")
                .replace("נשארו אצלו/ה", isBoy ? "נשארו אצלו" : "נשארו אצלה")
                .replace("היה/הייתה", isBoy ? "היה" : "הייתה");

        boolean isBuildingTemplate = template.contains("בנה") || template.contains("בנתה");

        String object;
        if (isBuildingTemplate) {
            object = buildings[rand.nextInt(buildings.length)];
        } else {
            String[] combined = new String[fruits.length + objects.length];
            System.arraycopy(fruits, 0, combined, 0, fruits.length);
            System.arraycopy(objects, 0, combined, fruits.length, objects.length);
            object = combined[rand.nextInt(combined.length)];
        }

        String aText = getSingleFormWithObject(a, object, isBoy);
        String bText = getSingleFormWithObject(b, object, isBoy);
        String questionText = String.format(template, name, aText, bText);


        // לחתוך אם מופיעה כבר המילה "כמה"
        if (!template.startsWith("כמה")) {
            int indexOfQuestion = questionText.indexOf("כמה");
            if (indexOfQuestion != -1) {
                questionText = questionText.substring(0, indexOfQuestion).trim();
            }
        }


        // מוסיף את הסיומת: "כמה ... בסך הכול ..."
        String ending;
        if (questionText.contains("היו ל") || questionText.contains("בהתחלה היו") || questionText.contains("הצטרפו")) {
            ending = String.format("כמה %s בסך הכל יש עכשיו ל%s?", object, name);
        } else if (questionText.contains("אסף") || questionText.contains("אספה") ||
                questionText.contains("מצא") || questionText.contains("מצאה") ||
                questionText.contains("הוסיפו")) {
            ending = String.format("כמה %s בסך הכל יש עכשיו ל%s?", object, name);
        } else {
            String verb = extractActionVerb(questionText, isBoy);
            ending = String.format("כמה %s בסך הכל %s %s?", object, verb, name);
        }
        questionText += " " + ending;

        // יוצר תשובות
        int[] answers = {correct, correct + 1, Math.max(0, correct - 1), correct + 2};
        shuffleArray(answers);

        // לבסוף בונה map
        Map<String, Object> q = new HashMap<>();
        q.put("questionText", questionText);
        q.put("operationSign", "word");      // סימון שמדובר בשאלה מילולית
        q.put("correctAnswer", correct);
        q.put("answers", answers);

        // שמירה של הנתונים לצורך הצגה בצד הלקוח
        // כדי שתוכלי "להוסיף פתרון" לפי המתחת/מעל 20:
        q.put("wordSign", sign);   // "+" או "-"
        q.put("wordA", a);
        q.put("wordB", b);

        return q;
    }

    private String extractActionVerb(String text, boolean isBoy) {
        if (text.contains("קיבל") || text.contains("קיבלה")) return isBoy ? "קיבל" : "קיבלה";
        if (text.contains("קנה") || text.contains("קנתה")) return isBoy ? "קנה" : "קנתה";
        if (text.contains("בנה") || text.contains("בנתה")) return isBoy ? "בנה" : "בנתה";
        if (text.contains("נתן") || text.contains("נתנה")) return isBoy ? "נתן" : "נתנה";
        return isBoy ? "קיבל" : "קיבלה";
    }

    private String getSingleFormWithObject(int num, String object, boolean isBoy) {
        if (num != 1) return num + " " + object;

        String base = switch (object) {
            case "תפוחים" -> "תפוח";
            case "אגסים" -> "אגס";
            case "תפוזים" -> "תפוז";
            case "בננות" -> "בננה";
            case "שזיפים" -> "שזיף";
            case "ענבים" -> "ענב";
            case "מחברות" -> "מחברת";
            case "ספרים" -> "ספר";
            case "עפרונות" -> "עיפרון";
            case "עטים" -> "עט";
            case "יומנים" -> "יומן";
            case "קלסרים" -> "קלסר";
            case "בתים" -> "בית";
            case "דירות" -> "דירה";
            case "מגדלים" -> "מגדל";
            case "מגדלי לגו" -> "מגדל לגו";
            case "ארמונות מחול" -> "ארמון מחול";
            case "אוהלים" -> "אוהל";
            default -> object;
        };
        return isBoy ? base + " אחד" : base + " אחת";
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
        q.put("operationSign", "frac" + sign); // מזהה שזה שבר
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
            // מונע דומים
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
            num = rand.nextInt(5) + 1; // בין 1 ל-5
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
