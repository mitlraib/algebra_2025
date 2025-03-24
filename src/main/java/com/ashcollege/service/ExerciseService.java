// ExerciseService.java
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

    // פונקציה חדשה שמעלה את ספירת השגיאות באותו topic
    public void incrementTopicMistakes(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setMistakes(rec.getMistakes() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    // --- המשך שאר הקוד ללא שינוי מהותי, למעט המקום שנוסיף קריאה ל-incrementTopicMistakes
    // למטה אציג את כל הקובץ השלם, עם השינויים:

    public Map<String, Object> generateQuestion(int topicId) {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("No current user found");
        }

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

    // בקשה מס' 5: אם יצאו תשובות זהות, נחליף אחת מהן בתשובה אחרת
    // ניישם גם בgenerateBasicArithmetic וגם בgenerateFractionQuestion, לאחר שיוצרים את המערך
    private Map<String, Object> generateBasicArithmetic(String sign, int level) {
        int maxVal = level * 5;
        if (maxVal < 5) {
            maxVal = 5;
        }
        int a=0, b=0, correct=0;
        boolean valid = false;

        while (!valid) {
            a = rand.nextInt(maxVal) + 1;
            b = rand.nextInt(maxVal) + 1;
            switch (sign) {
                case "+": correct = a + b; valid = true; break;
                case "-": if (a >= b) { correct = a - b; valid = true;} break;
                case "×": correct = a * b; valid = true; break;
                case "÷": if (b != 0 && (a % b == 0)) { correct = a / b; valid = true;} break;
            }
        }

        int[] answers = new int[]{
                correct,
                correct+1,
                Math.max(0, correct-1),
                correct+2
        };
        shuffleArray(answers);

        // בדיקת כפילות תשובות - אם יש זהות נשנה תשובה אחת
        fixDuplicates(answers, correct);

        Map<String,Object> q = new HashMap<>();
        q.put("first", a);
        q.put("second", b);
        q.put("operationSign", sign);
        q.put("correctAnswer", correct);
        q.put("answers", answers);
        return q;
    }

    private Map<String, Object> generateFractionQuestion(String sign, int level) {
        int[] frac = createFractionPair(level);
        int a = frac[0], b = frac[1];
        int c = frac[2], d = frac[3];

        if (sign.equals("-")) {
            if ((long)a*d < (long)c*b) {
                return generateFractionQuestion(sign, level);
            }
        }

        int num=0, den=0;
        switch (sign) {
            case "+":
                if (b == d) { num=a+c; den=b; }
                else { num=a*d + b*c; den=b*d; }
                break;
            case "-":
                if (b == d) { num=a-c; den=b; }
                else { num=a*d - b*c; den=b*d; }
                break;
            case "×": num=a*c; den=b*d; break;
            case "÷": num=a*d; den=b*c; break;
        }

        if (num<0 || den<=0) {
            return generateFractionQuestion(sign, level);
        }

        int correctEncoded = num*1000 + den;
        int[] answers = new int[4];
        answers[0] = correctEncoded;
        answers[1] = (num+1)*1000 + den;
        answers[2] = Math.max(1,num-1)*1000 + den;
        answers[3] = num*1000 + Math.max(1, den+1);
        shuffleArray(answers);

        // מניעת כפילויות (בקשה מס' 5)
        fixDuplicates(answers, correctEncoded);

        Map<String,Object> q = new HashMap<>();
        q.put("first", a+"/"+b);
        q.put("second", c+"/"+d);
        q.put("operationSign", sign);
        q.put("correctAnswer", correctEncoded);
        q.put("answers", answers);
        return q;
    }

    private int[] createFractionPair(int level) {
        // (ללא שינוי מהותי למעט תוספת הערות)
        if (level<1) level=1;

        boolean sameDen=false, differDen=false;
        int maxDen=5;
        int forcedNum=-1;

        if (level==1) { sameDen=true; maxDen=5; }
        else if (level==2) { sameDen=true; maxDen=10; }
        else if (level==3) { sameDen=true; maxDen=20; }
        else if (level==4) { differDen=true; maxDen=5; forcedNum=1; }
        else if (level==5) { differDen=true; maxDen=10; forcedNum=1; }
        else {
            differDen=true;
            int offset = 5*(level-5);
            if (offset<5) offset=5;
            maxDen=offset;
            forcedNum=0;
        }

        if (sameDen) {
            int den = rand.nextInt(maxDen-1)+2;
            int n1 = rand.nextInt(den)+1;
            int n2 = rand.nextInt(den)+1;
            return new int[]{ n1, den, n2, den};
        }
        else {
            int[] f1 = createSingleFraction(maxDen, forcedNum);
            int[] f2 = createSingleFraction(maxDen, forcedNum);
            if (level<=5) {
                while (f2[1]==f1[1]) {
                    f2 = createSingleFraction(maxDen, forcedNum);
                }
            }
            return new int[]{ f1[0], f1[1], f2[0], f2[1]};
        }
    }

    private int[] createSingleFraction(int maxDen, int forcedNum) {
        int den = rand.nextInt(maxDen-1)+2;
        int num;
        if (forcedNum>0) {
            num=forcedNum;
        } else if (forcedNum==0) {
            num= rand.nextInt(5)+1;
        } else {
            num= rand.nextInt(den)+1;
        }
        return new int[]{ num, den};
    }

    private void shuffleArray(int[] arr) {
        for (int i=arr.length-1; i>0; i--) {
            int j = rand.nextInt(i+1);
            int tmp=arr[i];
            arr[i]=arr[j];
            arr[j]=tmp;
        }
    }

    // מנגנון פשוט למניעת תשובות כפולות לגמרי
    private void fixDuplicates(int[] answers, int correct) {
        for (int i=0; i<answers.length; i++) {
            for (int j=i+1; j<answers.length; j++) {
                if (answers[i] == answers[j]) {
                    // החלף תשובה אחת במשהו שאינו קיים במערך
                    // ננסה לעשות "correct+ rand..."
                    int candidate;
                    do {
                        candidate = correct + rand.nextInt(50)+1;
                    } while (existsInArray(candidate, answers));
                    answers[j] = candidate;
                }
            }
        }
    }

    private boolean existsInArray(int candidate, int[] arr) {
        for (int val : arr) {
            if (val==candidate) return true;
        }
        return false;
    }
}
