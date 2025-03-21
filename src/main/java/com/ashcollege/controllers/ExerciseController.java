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

    // מביא שאלה חדשה לפי topicId
    @GetMapping("/next")
    public ResponseEntity<?> getNextQuestion(@RequestParam int topicId, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Map<String, Object> question = exerciseService.generateQuestion(topicId);
        session.setAttribute("currentQuestion", question);

        return ResponseEntity.ok(question);
    }

    // בודק תשובה, מעלה רמה אם צריך, וסופר תרגילים/שגיאות
    @PostMapping("/answer")
    public ResponseEntity<?> checkAnswer(@RequestBody Map<String, Object> answerData, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Map<String, Object> currentQuestion = (Map<String, Object>) session.getAttribute("currentQuestion");
        if (currentQuestion == null) {
            return ResponseEntity.badRequest().body("No question in session");
        }

        int userAnswer = (int) answerData.get("answer");
        boolean isCorrect = exerciseService.checkAnswer(currentQuestion, userAnswer);

        // ספירה של תרגילים + שגיאות
        userService.incrementTotalExercises(user.getId());
        if (!isCorrect) {
            userService.incrementTotalMistakes(user.getId());
        }

        int topicId = (int) currentQuestion.get("topicId");
        Map<Integer, Integer> consecutiveMap = (Map<Integer, Integer>) session.getAttribute("consecutiveMap");
        if (consecutiveMap == null) {
            consecutiveMap = new HashMap<>();
        }
        Integer consecutive = consecutiveMap.getOrDefault(topicId, 0);

        if (isCorrect) {
            consecutive++;
        } else {
            consecutive = 0;
        }
        consecutiveMap.put(topicId, consecutive);
        session.setAttribute("consecutiveMap", consecutiveMap);

        // 5 תשובות רצופות => העלאת רמה בנושא
        String levelUpMessage = null;
        if (consecutive >= 5) {
            exerciseService.increaseUserTopicLevel(user.getId(), topicId);
            consecutiveMap.put(topicId, 0);
            session.setAttribute("consecutiveMap", consecutiveMap);
            consecutive = 0;

            int newLevel = exerciseService.getUserTopicLevel(user.getId(), topicId);
            levelUpMessage = "כל הכבוד! עלית לרמה " + newLevel + " בנושא זה!";
        }

        int newLevel = exerciseService.getUserTopicLevel(user.getId(), topicId);
        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", currentQuestion.get("correctAnswer"));
        result.put("consecutiveCorrect", consecutive);
        result.put("currentLevel", newLevel);
        if (levelUpMessage != null) {
            result.put("levelUpMessage", levelUpMessage);
        }

        return ResponseEntity.ok(result);
    }

    // שאלה רנדומלית
    @GetMapping("/next-random")
    public ResponseEntity<?> getNextRandomQuestion(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        int[] possibleTopics = {1,2,3,4,5,6,7,8};
        int chosenTopic = possibleTopics[new Random().nextInt(possibleTopics.length)];
        Map<String, Object> question = exerciseService.generateQuestion(chosenTopic);
        session.setAttribute("currentQuestion", question);

        return ResponseEntity.ok(question);
    }
}
