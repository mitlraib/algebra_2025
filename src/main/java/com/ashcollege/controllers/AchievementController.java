package com.ashcollege.controllers;

import com.ashcollege.entities.AchievementStatsEntity;
import com.ashcollege.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/{userId}")
    public ResponseEntity<AchievementStatsEntity> getAchievements(@PathVariable int userId) {

        AchievementStatsEntity stats = achievementService.getStatsForUser(userId);
        return ResponseEntity.ok(stats);
    }
}
