package com.ashcollege.controllers;

import com.ashcollege.entities.StatisticsEntity;
import com.ashcollege.entities.TopicStatisticsEntity;
import com.ashcollege.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * מחזיר את הסטטיסטיקה העיקרית (סה"כ תרגילים, שגיאות וכו').
     * רק משתמשים מאומתים (Authenticated) יכולים לגשת.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<StatisticsEntity> getStatistics() {
        StatisticsEntity statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * מחזיר רשימה של סטטיסטיקות מפולחות לפי נושא.
     * רק משתמשים מאומתים (Authenticated) יכולים לגשת.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/by-topic")
    public ResponseEntity<List<TopicStatisticsEntity>> getStatisticsByTopic() {
        List<TopicStatisticsEntity> list = statisticsService.getStatisticsByTopic();
        return ResponseEntity.ok(list);
    }
}
