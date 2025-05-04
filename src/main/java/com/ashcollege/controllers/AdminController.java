// AdminController.java (חדש)
package com.ashcollege.controllers;

import com.ashcollege.entities.UserTopicLevelEntity;
import com.ashcollege.repository.UserTopicLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserTopicLevelRepository userTopicLevelRepo;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/most-mistakes-topic")
    public ResponseEntity<?> getMostMistakesTopic(Authentication auth) {

    List<UserTopicLevelEntity> all = userTopicLevelRepo.findAll();
        // נמפה topicId -> sum of mistakes
        Map<Integer,Integer> sumMap = new HashMap<>();

        for (UserTopicLevelEntity ut : all) {
            int t = ut.getTopicId();
            sumMap.put(t, sumMap.getOrDefault(t,0) + ut.getMistakes());
        }

        int maxTopic = -1;
        int maxMistakes = -1;
        for (Map.Entry<Integer,Integer> e : sumMap.entrySet()) {
            if (e.getValue() > maxMistakes) {
                maxMistakes = e.getValue();
                maxTopic = e.getKey();
            }
        }

        if (maxTopic<0) {
            return ResponseEntity.ok("No mistakes found at all.");
        }

        Map<String,Object> resp = new HashMap<>();
        resp.put("topicId", maxTopic);
        resp.put("totalMistakesInTopic", maxMistakes);
        return ResponseEntity.ok(resp);
    }
}
