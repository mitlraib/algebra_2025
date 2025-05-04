package com.ashcollege.controllers;

import com.ashcollege.entities.AchievementStatsEntity;
import com.ashcollege.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller לטיפול ב־Achievements של משתמשים.
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @Autowired
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    /**
     * מחזיר את סטטיסטיקות ההישגים של משתמש לפי userId.
     * רק משתמשים מחוברים (Authenticated) יכולים לקרוא endpoint זה.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}")
    public ResponseEntity<AchievementStatsEntity> getAchievements(
            @PathVariable("userId") int userId
    ) {
        AchievementStatsEntity stats = achievementService.getStatsForUser(userId);
        return ResponseEntity.ok(stats);
    }
}
