package com.ashcollege.service;

import com.ashcollege.entities.AchievementStatsEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AchievementService {

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepository;

    public AchievementStatsEntity getStatsForUser(int userId) {
        List<UserTopicLevelEntity> allLevels = userTopicLevelRepository.findByUserId(userId);
        System.out.println("Found levels for user " + userId + ": " + allLevels);  // הדפסת הנתונים שנמצאו

        AchievementStatsEntity stats = new AchievementStatsEntity();

        if (allLevels == null || allLevels.isEmpty()) {
            System.out.println("No levels found for user " + userId);
            return stats;  // החזרת אובייקט ריק במקרה כזה
        }

        stats.setAddition(0);
        stats.setSubtraction(0);
        stats.setMultiplication(0);
        stats.setDivision(0);
        stats.setFractionAddition(0);
        stats.setFractionSubtraction(0);
        stats.setFractionMultiplication(0);
        stats.setFractionDivision(0);

        for (UserTopicLevelEntity level : allLevels) {
            int topic = level.getTopicId();
            int correctAnswers = level.getAttempts() - level.getMistakes();

            switch (topic) {
                case 1:
                    stats.setAddition(stats.getAddition() + correctAnswers);
                    break;
                case 2:
                    stats.setSubtraction(stats.getSubtraction() + correctAnswers);
                    break;
                case 3:
                    stats.setMultiplication(stats.getMultiplication() + correctAnswers);
                    break;
                case 4:
                    stats.setDivision(stats.getDivision() + correctAnswers);
                    break;
                case 5:
                    stats.setFractionAddition(stats.getFractionAddition() + correctAnswers);
                    break;
                case 6:
                    stats.setFractionSubtraction(stats.getFractionSubtraction() + correctAnswers);
                    break;
                case 7:
                    stats.setFractionMultiplication(stats.getFractionMultiplication() + correctAnswers);
                    break;
                case 8:
                    stats.setFractionDivision(stats.getFractionDivision() + correctAnswers);
                    break;
                default:
                    System.out.println("Unknown topic: " + topic);
                    break;
            }
        }

        System.out.println("Stats for user " + userId + ": " + stats);  // הדפסת הסטטיסטיקות בסוף
        return stats;
    }
}