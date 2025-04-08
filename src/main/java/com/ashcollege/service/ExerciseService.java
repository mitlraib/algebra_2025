package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*; // ✅ תוספת ל-Set, HashSet, Map וכו'

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

        UserTopicLevelEntity ute = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        if (ute == null) {
            ute = new UserTopicLevelEntity();
            ute.setUserId(user.getId());
            ute.setTopicId(topicId);
            ute.setLevel(1);
            ute.setMistakes(0);
            ute.setAttempts(0);
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
            int oldLevel = rec.getLevel();
            rec.setLevel(oldLevel + 1);
            userTopicLevelRepo.save(rec);
            logger.info("User {} in topic {} => level up from {} to {}", userId, topicId, oldLevel, rec.getLevel());
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

    public void incrementTopicMistakes(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setMistakes(rec.getMistakes() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    public void incrementAttempt(int userId, int topicId) {
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(userId, topicId);
        if (rec != null) {
            rec.setAttempts(rec.getAttempts() + 1);
            userTopicLevelRepo.save(rec);
        }
    }

    private int[] generateUniqueAnswers(int correctAnswer) {
        Set<Integer> uniqueAnswers = new HashSet<>();
        uniqueAnswers.add(correctAnswer);

        while (uniqueAnswers.size() < 4) {
            int offset = rand.nextInt(5) - 2;
            int candidate = correctAnswer + offset;
            if (candidate < 0) continue;
            uniqueAnswers.add(candidate);
        }

        int[] arr = uniqueAnswers.stream().mapToInt(Integer::intValue).toArray();
        shuffleArray(arr);
        return arr;
    }

    private Map<String, Object> generateBasicArithmetic(String sign, int level) {
        int maxVal = Math.max(level * 5, 5);
        int a = 0, b = 0, correct = 0;
        boolean valid = false;

        while (!valid) {
            a = rand.nextInt(maxVal) + 1;
            b = rand.nextInt(maxVal) + 1;

            switch (sign) {
                case "+": correct = a + b; valid = true; break;
                case "-": if (a >= b) { correct = a - b; valid = true; } break;
                case "×": correct = a * b; valid = true; break;
                case "÷": if (b != 0 && a % b == 0) { correct = a / b; valid = true; } break;
            }
        }

        int[] answers = generateUniqueAnswers(correct);
        Map<String, Object> q = new HashMap<>();

        q.put("first", Math.max(a, b));
        q.put("second", Math.min(a, b));
        q.put("operationSign", sign);
        q.put("correctAnswer", correct);
        q.put("answers", answers);

        return q;
    }

    private Map<String, Object> generateFractionQuestion(String sign, int level) {
        int[] frac = createFractionPair(level);
        int a = frac[0];
        int b = frac[1];
        int c = frac[2];
        int d = frac[3];

        if (sign.equals("-") && (a * d < c * b)) {
            return generateFractionQuestion(sign, level);
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
            case "×": num = a * c; den = b * d; break;
            case "÷": num = a * d; den = b * c; break;
        }

        if (num < 0 || den <= 0) return generateFractionQuestion(sign, level);

        int correctEncoded = num * 1000 + den;
        Set<Integer> uniqueAnswers = new HashSet<>();
        uniqueAnswers.add(correctEncoded);

        while (uniqueAnswers.size() < 4) {
            int offsetNum = rand.nextInt(5) - 2;
            int offsetDen = rand.nextInt(3);
            int newNum = Math.max(1, num + offsetNum);
            int newDen = Math.max(1, den + offsetDen);
            int encoded = newNum * 1000 + newDen;
            uniqueAnswers.add(encoded);
        }

        int[] answers = uniqueAnswers.stream().mapToInt(Integer::intValue).toArray();
        shuffleArray(answers);

        Map<String, Object> q = new HashMap<>();
        q.put("first", a + "/" + b);
        q.put("second", c + "/" + d);
        q.put("operationSign", sign);
        q.put("correctAnswer", correctEncoded);
        q.put("answers", answers);

        return q;
    }

    private int[] createFractionPair(int level) {
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
            maxDen = Math.max(offset, 5);
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
        int num = (forcedNum > 0) ? forcedNum :
                (forcedNum == 0 ? rand.nextInt(5) + 1 : rand.nextInt(den) + 1);
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
