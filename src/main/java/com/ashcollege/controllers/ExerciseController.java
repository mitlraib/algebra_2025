package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.service.ExerciseService;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final UserService userService;
    private final ExerciseService exerciseService;

    @Autowired
    public ExerciseController(UserService userService,
                              ExerciseService exerciseService) {
        this.userService = userService;
        this.exerciseService = exerciseService;
    }

    /**
     * מביא שאלה חדשה לפי topicId.
     * השאלה תישלח ללקוח, והוא יאכסן אותה (ב־state) וישלח אותה חזרה ב־/answer.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/next")
    public ResponseEntity<Map<String, Object>> getNextQuestion(
            @RequestParam int topicId
    ) {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        try {
            System.out.println("🔁 התחלה generateQuestion: " + System.currentTimeMillis());
            Map<String, Object> question = exerciseService.generateQuestion(topicId);
            System.out.println("✅ סיום generateQuestion: " + System.currentTimeMillis());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "אירעה שגיאה ביצירת השאלה", "details", e.getMessage()));
        }
    }

    /**
     * בודק תשובה: מקבל בגוף הבקשה payload עם:
     *   - "question": המפה שקיבלת ב־/next (כולל topicId, correctAnswer וכו')
     *   - "answer": התשובה שהמשתמש הקליד
     *
     * העיבוד נשאר כפי שהגדרת: ספירת שגיאות, רצף ותוספת רמה אם צריך.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> checkAnswer(
            @RequestBody Map<String, Object> payload
    ) {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> question = (Map<String, Object>) payload.get("question");
        if (question == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "חסרה שאלה בבקשה"));
        }

        Integer userAnswer = (Integer) payload.get("answer");
        if (userAnswer == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing answer"));
        }

        boolean isCorrect = exerciseService.checkAnswer(question, userAnswer);
        int topicId = (int) question.get("topicId");

        // תמיד נעדכן נסיונות
        userService.incrementTotalExercises(user.getId());
        exerciseService.incrementAttempt(user.getId(), topicId);

        String levelUpMessage = null;

        if (isCorrect) {
            user.setCorrectStreak(user.getCorrectStreak() + 1);

            if (user.getCorrectStreak() >= 5) {
                exerciseService.increaseUserTopicLevel(user.getId(), topicId);
                user.setCorrectStreak(0); // מאפסים לאחר עלייה
                levelUpMessage = "מעולה! עלית רמה!";
            }

        } else {
            userService.incrementTotalMistakes(user.getId());
            exerciseService.incrementTopicMistakes(user.getId(), topicId);
            user.setCorrectStreak(0); // טעות => מאפסים רצף
        }

        // לשמור את המשתמש עם הרצף החדש (או המאופס)
        userService.updateUser(user);

        int newLevel = exerciseService.getUserTopicLevel(user.getId(), topicId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("isCorrect", isCorrect);
        resp.put("correctAnswer", question.get("correctAnswer"));
        resp.put("currentLevel", newLevel);

        if (levelUpMessage != null) {
            resp.put("levelUpMessage", levelUpMessage);
        }

        return ResponseEntity.ok(resp);
    }


    /**
     * פונקציה להפקת שאלה רנדומלית.
     * מחזירה רק את המפה של השאלה, ללא session.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/next-random")
    public ResponseEntity<Map<String, Object>> getNextRandomQuestion() {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        int[] possibleTopics = {1, 2, 3, 4, 5, 6, 7, 8};
        int chosenTopic = possibleTopics[new Random().nextInt(possibleTopics.length)];
        Map<String, Object> question = exerciseService.generateQuestion(chosenTopic);
        return ResponseEntity.ok(question);
    }
}
