package com.ashcollege.controllers;

import com.ashcollege.entities.StatisticsEntity;
import com.ashcollege.entities.TopicStatisticsEntity;
import com.ashcollege.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<StatisticsEntity> getStatistics() {
        StatisticsEntity statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/by-topic")
    public ResponseEntity<List<TopicStatisticsEntity>> getStatisticsByTopic() {
        return ResponseEntity.ok(statisticsService.getStatisticsByTopic());
    }
}