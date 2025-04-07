package com.ashcollege.service;

import com.ashcollege.entities.StatisticsEntity;
import com.ashcollege.entities.TopicStatisticsEntity;
import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepository;

    public StatisticsEntity getStatistics() {
        // שליפת הנתונים מהדאטהבייס
        List<UserTopicLevelEntity> allData = userTopicLevelRepository.findAll();

        if (allData.isEmpty()) {
            // החזר סטטיסטיקות ברירת מחדל במקרה שאין נתונים
            return new StatisticsEntity(0, 0, 0.0, 0 , 0);
        }

        // חישוב כלליים
        int totalAttempts = 0;
        int totalMistakes = 0;

        // מפות לניסיונות וטעויות לכל נושא
        Map<Integer, Integer> topicAttempts = new HashMap<>();
        Map<Integer, Integer> topicMistakes = new HashMap<>();

        for (UserTopicLevelEntity data : allData) {
            int topicId = data.getTopicId();
            int attempts = data.getAttempts();
            int mistakes = data.getMistakes();

            totalAttempts += attempts;
            totalMistakes += mistakes;

            topicAttempts.put(topicId, topicAttempts.getOrDefault(topicId, 0) + attempts);
            topicMistakes.put(topicId, topicMistakes.getOrDefault(topicId, 0) + mistakes);
        }

        double successRate = 0;
        if (totalAttempts > 0) {
            successRate = (double)(totalAttempts - totalMistakes) / totalAttempts * 100;
        }

        // חישוב הנושא הקשה ביותר לפי אחוז ההצלחה הנמוך ביותר
        double minSuccessRate = Double.MAX_VALUE;
        int mostDifficultTopic = -1;


        // חישוב הנושא הקל ביותר לפי אחוז ההצלחה הגבוה ביותר
        double maxSuccessRate = -Double.MAX_VALUE;
        int easiestTopic = -1;

        for (Integer topicId : topicAttempts.keySet()) {
            int attempts = topicAttempts.get(topicId);
            int mistakes = topicMistakes.getOrDefault(topicId, 0);

            if (attempts > 0) {
                double topicSuccessRate = 100.0 * (attempts - mistakes) / attempts;

                // חישוב הנושא הקשה ביותר
                if (topicSuccessRate < minSuccessRate) {
                    minSuccessRate = topicSuccessRate;
                    mostDifficultTopic = topicId;
                }

                // חישוב הנושא הקל ביותר
                if (topicSuccessRate > maxSuccessRate) {
                    maxSuccessRate = topicSuccessRate;
                    easiestTopic = topicId;
                }
            }


        }

        return new StatisticsEntity(totalAttempts, totalMistakes, successRate, mostDifficultTopic , easiestTopic);
    }

    public List<TopicStatisticsEntity> getStatisticsByTopic() {
        List<UserTopicLevelEntity> allData = userTopicLevelRepository.findAll();

        if (allData.isEmpty()) {
            return Collections.emptyList(); // מחזיר רשימה ריקה
        }

        Map<Integer, TopicStatisticsEntity> topicMap = new HashMap<>();


        for (UserTopicLevelEntity data : allData) {
            int topicId = data.getTopicId();
            int attempts = data.getAttempts();
            int mistakes = data.getMistakes();

            TopicStatisticsEntity stats = topicMap.getOrDefault(topicId, new TopicStatisticsEntity(topicId, 0, 0, 0));
            stats.setTotalAttempts(stats.getTotalAttempts() + attempts);
            stats.setTotalMistakes(stats.getTotalMistakes() + mistakes);
            topicMap.put(topicId, stats);
        }

        for (TopicStatisticsEntity stats : topicMap.values()) {
            if (stats.getTotalAttempts() > 0) {
                double rate = 100.0 * (stats.getTotalAttempts() - stats.getTotalMistakes()) / stats.getTotalAttempts();
                stats.setSuccessRate(rate);
            }
        }

        return new ArrayList<>(topicMap.values());
    }
}