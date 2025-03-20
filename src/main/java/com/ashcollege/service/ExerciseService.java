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
            ute.setLevel(1);
            userTopicLevelRepo.save(ute);
        }
        int currentLevel = ute.getLevel();
        logger.info("Topic {} => CurrentLevel={}", topicId, currentLevel);

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

    public void increaseUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setLevel(rec.getLevel() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    public int getUserTopicLevel(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec == null) return 1;
        return rec.getLevel();
    }

    public boolean checkAnswer(Map<String, Object> question, int userAnswer) {
        int correct = (int) question.get("correctAnswer");
        return (userAnswer == correct);
    }

    // --- חיבור/חיסור/כפל/חילוק רגיל בהתאם לרמה: 1..5.. וכו' ---
    private Map<String, Object> generateBasicArithmetic(String sign, int level) {
        // level: טווח = רמה*5
        int maxVal = level * 5;
        if (maxVal < 5) {
            maxVal = 5;
        }
        int a = 0, b=0, correct=0;
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
            // חוזר עד valid=true
        }

        int[] answers = new int[]{
                correct,
                correct + 1,
                Math.max(0, correct - 1),
                correct + 2
        };
        shuffleArray(answers);

        Map<String,Object> q = new HashMap<>();
        q.put("first",   Integer.valueOf(a));
        q.put("second",  Integer.valueOf(b));
        q.put("operationSign", sign);
        q.put("correctAnswer", correct);
        q.put("answers", answers);
        return q;
    }

    // --- שברים ---
    private Map<String, Object> generateFractionQuestion(String sign, int level) {
        int[] frac = createFractionPair(level); // מחזיר: [num1, den1, num2, den2]

        int a = frac[0], b = frac[1]; // a/b
        int c = frac[2], d = frac[3]; // c/d

        // אם זה חיסור, נוודא a/b >= c/d (אחרת נבצע עוד פעם):
        if (sign.equals("-")) {
            if ((long)a * d < (long)c * b) {
                // לשם פשטות אקרא שוב
                return generateFractionQuestion(sign, level);
            }
        }

        // חשב תשובה:
        int num = 0, den = 0;
        switch (sign) {
            case "+":
                if (b == d) {
                    // מכנים זהים => מחברים מונים
                    num = a + c;
                    den = b;
                } else {
                    num = a*d + b*c;
                    den = b*d;
                }
                break;
            case "-":
                if (b == d) {
                    num = a - c;
                    den = b;
                } else {
                    num = a*d - b*c;
                    den = b*d;
                }
                break;
            case "×":
                num = a * c;
                den = b * d;
                break;
            case "÷":
                // (a/b) / (c/d) => (a*d)/(b*c)
                num = a * d;
                den = b * c;
                break;
        }

        // לוודא לא שלילי ולא 0 במכנה
        if (num < 0 || den <= 0) {
            // נגריל מחדש
            return generateFractionQuestion(sign, level);
        }

        // בונים 4 תשובות מקודדות
        int correctEncoded = num*1000 + den;
        int[] answers = new int[4];
        answers[0] = correctEncoded;
        answers[1] = (num+1)*1000 + den;
        answers[2] = Math.max(1,num-1)*1000 + den;
        answers[3] = num*1000 + Math.max(1, den+1);
        shuffleArray(answers);

        // להחזיר מפה
        Map<String,Object> q = new HashMap<>();
        q.put("first", a+"/"+b);
        q.put("second", c+"/"+d);
        q.put("operationSign", sign);
        q.put("correctAnswer", correctEncoded);
        q.put("answers", answers);

        return q;
    }

    /**
     * מייצר זוג שברים בהתאם לרמה:
     * levels:
     * 1 => מכנה משותף עד 5
     * 2 => מכנה משותף עד 10
     * 3 => מכנה משותף עד 20
     * 4 => מכנים שונים עד 5, מונה=1
     * 5 => מכנים שונים עד 10, מונה=1
     * 6 => מכנים שונים עד 5, מונה עד 5
     * 7 => מכנים שונים עד 10, מונה עד 5
     * 8 => מכנים שונים עד 15, מונה עד 5
     * 9 => מכנים שונים עד 20, מונה עד 5
     * וכו' (עולה ב-5 בכל רמה מעל 5).
     *
     * מחזיר מערך באורך 4: [num1,den1,num2,den2].
     */
    private int[] createFractionPair(int level) {
        if (level < 1) {
            level = 1;
        }

        boolean sameDen = false;  // האם שני השברים עם אותו מכנה
        boolean differDen = false; // האם חייבים מכנה אחר
        int maxDen = 5;
        int forcedNum = -1; // אם != -1 => מונה=1, אם=0 => מונה רנדומלי עד 5

        if (level==1) {
            sameDen = true; maxDen = 5;
        } else if (level==2) {
            sameDen = true; maxDen = 10;
        } else if (level==3) {
            sameDen = true; maxDen = 20;
        } else if (level==4) {
            differDen = true; maxDen = 5;  forcedNum=1;
        } else if (level==5) {
            differDen = true; maxDen = 10; forcedNum=1;
        } else {
            // level>=6
            differDen = true;
            int offset = 5*(level-5); // ex: level=6 => 5, 7=>10 ...
            if (offset < 5) offset=5;
            maxDen = offset;
            forcedNum=0; // 0=> מונה רנדומלי עד 5
        }

        // ניצור שני שברים
        if (sameDen) {
            int den = rand.nextInt(maxDen-1)+2;
            int n1 = rand.nextInt(den)+1;
            int n2 = rand.nextInt(den)+1;
            return new int[]{n1, den, n2, den};
        }

        // אחרת differDen (או רמות גבוהות)
        // לשבר ראשון:
        int[] f1 = createSingleFraction(maxDen, forcedNum);
        int[] f2 = createSingleFraction(maxDen, forcedNum);

        // ייתכן שיצא אותו מכנה. אם ממש רוצים "מכנים שונים" בכוח ברמות 4..5, אפשר לבדוק ולגריל שוב.
        // אבל בשאלה שלך לא ראיתי שביקשת למנוע מכנים זהים - רק אמרת "מכנים שונים עם מונה=1".
        // אוסיף בכל זאת:
        if (level<=5) {
            // נוודא den1 != den2
            while (f2[1] == f1[1]) {
                f2 = createSingleFraction(maxDen, forcedNum);
            }
        }

        return new int[]{ f1[0], f1[1], f2[0], f2[1] };
    }

    private int[] createSingleFraction(int maxDen, int forcedNum) {
        int den = rand.nextInt(maxDen-1)+2; //2..maxDen
        int num;
        if (forcedNum>0) {
            num = forcedNum;
        } else if (forcedNum==0) {
            // מונה 1..5
            num = rand.nextInt(5)+1;
        } else {
            //forcedNum==-1 => מונה רנדומלי 1..den (או 1..maxDen?)
            // לפי הדוגמה ראינו גם 5/3, 2/3... נגריל 1..(den)
            num = rand.nextInt(den)+1;
        }
        return new int[]{num, den};
    }

    /** ערבוב מערך במקום */
    private void shuffleArray(int[] arr) {
        for (int i = arr.length-1; i>0; i--) {
            int j = rand.nextInt(i+1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}
