package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.service.ExerciseService;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExerciseService exerciseService;

    /**
     * מביא שאלה חדשה ע"פ הנושא המבוקש ורמת המשתמש
     */
    @GetMapping("/next")
    public ResponseEntity<?> getNextQuestion(@RequestParam int topicId, HttpSession session) {
        // 1) ודא שהמשתמש מחובר (SecurityContext).
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        String mail = (String) auth.getPrincipal();
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // 2) צור שאלה חדשה דרך service
        Map<String, Object> question = exerciseService.generateQuestion(topicId, user.getLevel());

        // 3) נשמור את נתוני השאלה בסשן, כדי שנוכל לאמת תשובה מאוחר יותר.
        session.setAttribute("currentQuestion", question);

        // 4) מחזירים ללקוח אובייקט JSON עם השאלה
        return ResponseEntity.ok(question);
    }

    /**
     * מקבל תשובת משתמש על השאלה האחרונה, בודק נכונות, מעדכן רצף ועשוי להעלות רמה
     */
    @PostMapping("/answer")
    public ResponseEntity<?> checkAnswer(@RequestBody Map<String, Object> answerData, HttpSession session) {
        // 1) בדוק שהמשתמש מאומת
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        String mail = (String) auth.getPrincipal();
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // 2) שלוף את השאלה האחרונה מהסשן
        Map<String, Object> currentQuestion = (Map<String, Object>) session.getAttribute("currentQuestion");
        if (currentQuestion == null) {
            return ResponseEntity.badRequest().body("No question in session");
        }

        // 3) בדוק אם התשובה נכונה
        int userAnswer = (int) answerData.get("answer");
        boolean isCorrect = exerciseService.checkAnswer(currentQuestion, userAnswer);

        // 4) עדכן בסשן את מספר התשובות הרצופות
        Integer consecutive = (Integer) session.getAttribute("consecutiveCorrect");
        if (consecutive == null) consecutive = 0;
        if (isCorrect) {
            consecutive++;
        } else {
            consecutive = 0;
        }
        session.setAttribute("consecutiveCorrect", consecutive);

        // 5) אם הגיע ל-5 רצופות => העלאת רמה
        if (consecutive >= 6) {
            user.setLevel(user.getLevel() + 1); // העלאת רמה
            session.setAttribute("consecutiveCorrect", 0); // איפוס
            consecutive = 0;
        }

        // 6) בונים תשובה ללקוח
        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", currentQuestion.get("correctAnswer"));
        result.put("consecutiveCorrect", consecutive);
        result.put("currentLevel", user.getLevel());

        return ResponseEntity.ok(result);
    }

    /**
     * בוחר נושא רנדומלי ומחזיר שאלה מתוך אותו נושא
     */
    @GetMapping("/next-random")
    public ResponseEntity<?> getNextRandomQuestion(HttpSession session) {
        // 1) ודא שהמשתמש מחובר
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        // 2) הבא את המשתמש
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // 3) בחר topicId רנדומלי מתוך 1..8 (לדוגמה)
        int[] possibleTopics = {1, 2, 3, 4, 5, 6, 7, 8};
        int randomIndex = new Random().nextInt(possibleTopics.length);
        int chosenTopic = possibleTopics[randomIndex];

        // 4) צור שאלה חדשה
        Map<String, Object> question = exerciseService.generateQuestion(chosenTopic, user.getLevel());

        // 5) שמור אותה בסשן (כמו ב-/next)
        session.setAttribute("currentQuestion", question);

        // 6) החזר ללקוח
        return ResponseEntity.ok(question);
    }
}
