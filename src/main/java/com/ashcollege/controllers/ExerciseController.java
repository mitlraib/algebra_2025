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
        // בדיקה שהמשתמש מחובר
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // צור שאלה חדשה דרך service
        Map<String, Object> question = exerciseService.generateQuestion(topicId);

        // שומרים את נתוני השאלה בסשן כדי שנוכל לבדוק תשובה
        session.setAttribute("currentQuestion", question);

        return ResponseEntity.ok(question);
    }

    /**
     * מקבל תשובת משתמש, בודק נכונות, מעדכן רצף, ועשוי להעלות רמה
     */
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

        // השאלה האחרונה
        Map<String, Object> currentQuestion = (Map<String, Object>) session.getAttribute("currentQuestion");
        if (currentQuestion == null) {
            return ResponseEntity.badRequest().body("No question in session");
        }

        // האם התשובה נכונה?
        int userAnswer = (int) answerData.get("answer");
        boolean isCorrect = exerciseService.checkAnswer(currentQuestion, userAnswer);

        // שליפת topicId מתוך השאלה (שמנו אותו ב- generateQuestion)
        int topicId = (int) currentQuestion.get("topicId");

        // ננהל רצף נכון לכל נושא בנפרד, נשמור בסשן מפת (topicId -> consecutiveCount)
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

        // אם הגיע ל-5 רצופות => העלאת רמה לנושא הספציפי
        // (שים לב שכאן אנחנו מעדכנים userTopicLevel ולא את user.level)
//        if (consecutive >= 5) {
//            exerciseService.increaseUserTopicLevel(user.getId(), topicId);
//            // מאפסים את הרצף
//            consecutiveMap.put(topicId, 0);
//            session.setAttribute("consecutiveMap", consecutiveMap);
//            consecutive = 0;
//        }

        // (שים לב שכאן אנחנו מעדכנים userTopicLevel ולא את user.level)

        String levelUpMessage = null; // הודעה שתשלח למשתמש
        if (consecutive >= 5) {
            exerciseService.increaseUserTopicLevel(user.getId(), topicId);
            // מאפסים את הרצף
            consecutiveMap.put(topicId, 0);
            session.setAttribute("consecutiveMap", consecutiveMap);
            consecutive = 0;

            int newLevel = exerciseService.getUserTopicLevel(user.getId(), topicId); // שליפת הרמה החדשה
            levelUpMessage = "כל הכבוד! עלית לרמה "+ newLevel +" בנושא זה!";

        }

        // בונים תשובה ללקוח
        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", currentQuestion.get("correctAnswer"));
        result.put("consecutiveCorrect", consecutive);
        // שליפה עדכנית של הרמה לאחר עדכון
        int newLevel = exerciseService.getUserTopicLevel(user.getId(), topicId);
        result.put("currentLevel", newLevel);


        // אם יש הודעה על העלאת רמה, נוסיף אותה
        if (levelUpMessage != null) {
            result.put("levelUpMessage", levelUpMessage);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * בוחר נושא רנדומלי ומחזיר שאלה מתוך אותו נושא
     */
    @GetMapping("/next-random")
    public ResponseEntity<?> getNextRandomQuestion(HttpSession session) {
        // בדיקה שהמשתמש מחובר
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
