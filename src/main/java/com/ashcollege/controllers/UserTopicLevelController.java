package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user/topics-levels")
public class UserTopicLevelController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepo;

    /**
     * מחזיר את כל הנושאים + הרמה של המשתמש בכל נושא
     */
    @GetMapping
    public ResponseEntity<?> getUserTopicsLevels() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        List<UserTopicLevelEntity> list = userTopicLevelRepo.findByUserId(user.getId());
        // אולי תרצה להחזיר גם שם הנושא (topicName). כאן לצורך הדגמה, נבצע "תרגום" ידני:
        // mapping בין topicId -> שם
        Map<Integer, String> topicNames = new HashMap<>();
        topicNames.put(1, "חיבור");
        topicNames.put(2, "חיסור");
        topicNames.put(3, "כפל");
        topicNames.put(4, "חילוק");
        topicNames.put(5, "חיבור שברים");
        topicNames.put(6, "חיסור שברים");
        topicNames.put(7, "כפל שברים");
        topicNames.put(8, "חילוק שברים");

        // בניית המערך לתשובה
        List<Map<String, Object>> responseTopics = new ArrayList<>();
        for (UserTopicLevelEntity ut : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("topicId", ut.getTopicId());
            item.put("level", ut.getLevel());
            item.put("topicName", topicNames.getOrDefault(ut.getTopicId(), "נושא לא ידוע"));
            responseTopics.add(item);
        }

        // החזרה בצורה אחידה
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("topics", responseTopics);

        return ResponseEntity.ok(response);
    }

    /**
     * עדכון רמה לנושא מסוים (לדוגמה הורדה מהרמה הנוכחית)
     */
    @PutMapping
    public ResponseEntity<?> updateUserTopicLevel(@RequestBody Map<String, Integer> body) {
        // body אמור להכיל: { topicId, newLevel }
        int topicId = body.getOrDefault("topicId", 0);
        int newLevel = body.getOrDefault("newLevel", 1);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // איתור הרשומה הקיימת למשתמש, topicId
        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        if (rec == null) {
            // ניצור אחת חדשה אם רוצים
            // או נחזיר שגיאה שזה לא קיים
            return ResponseEntity.badRequest().body("No level record found for this topic");
        }

        // לוודא שהמשתמש לא מנסה להגדיל מעל הקיים, אם זה ההיגיון שלך.
        if (newLevel > rec.getLevel()) {
            return ResponseEntity.badRequest().body("אי אפשר להעלות רמה ידנית");
        }

        // לוודא שלא יהיה פחות מ-1
        if (newLevel < 1) {
            return ResponseEntity.badRequest().body("רמה לא תקינה");
        }

        // עדכון
        rec.setLevel(newLevel);
        userTopicLevelRepo.save(rec);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Level updated successfully");
        return ResponseEntity.ok(response);
    }
}
