package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import com.ashcollege.service.ExerciseService;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user/topics-levels")
@PreAuthorize("isAuthenticated()")
public class UserTopicLevelController {

    private final UserService userService;
    private final UserTopicLevelRepository userTopicLevelRepo;
    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    public UserTopicLevelController(UserService userService,
                                    UserTopicLevelRepository userTopicLevelRepo) {
        this.userService = userService;
        this.userTopicLevelRepo = userTopicLevelRepo;
    }

    /**
     * מחזיר את תפקיד המשתמש המחובר.
     */
    @GetMapping("/role")
    public ResponseEntity<?> getUserRole() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String email = auth.getName();
        UserEntity user = userService.findByMail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("role", user.getRole()));
    }

    /**
     * מחזיר את כל הנושאים + רמת המשתמש בכל נושא.
     */
    @GetMapping
    public ResponseEntity<?> getUserTopicsLevels() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        List<UserTopicLevelEntity> list = userTopicLevelRepo.findByUserId(user.getId());
        Map<Integer, String> topicNames = Map.of(
                1, "חיבור",
                2, "חיסור",
                3, "כפל",
                4, "חילוק",
                5, "חיבור שברים",
                6, "חיסור שברים",
                7, "כפל שברים",
                8, "חילוק שברים"
        );

        List<Map<String, Object>> responseTopics = new ArrayList<>();
        for (UserTopicLevelEntity ut : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("topicId", ut.getTopicId());
            item.put("level", ut.getLevel());
            item.put("topicName", topicNames.getOrDefault(ut.getTopicId(), "נושא לא ידוע"));
            responseTopics.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "topics", responseTopics
        ));
    }

    /**
     * מעדכן רמה לנושא מסוים. מקבל JSON עם { topicId, newLevel }.
     */
    @PutMapping
    public ResponseEntity<?> updateUserTopicLevel(@RequestBody Map<String, Integer> body) {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        int topicId = body.getOrDefault("topicId", 0);
        int newLevel = body.getOrDefault("newLevel", 1);

        UserTopicLevelEntity rec = userTopicLevelRepo.findByUserIdAndTopicId(user.getId(), topicId);
        if (rec == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No level record found for this topic"));
        }
        if (newLevel > rec.getLevel()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "אי אפשר להעלות רמה ידנית"));
        }
        if (newLevel < 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "רמה לא תקינה"));
        }

        rec.setLevel(newLevel);
        userTopicLevelRepo.save(rec);
        exerciseService.updateGeneralLevel(user.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Level updated successfully"
        ));
    }
}
