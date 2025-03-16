
package com.ashcollege.controllers;

import com.ashcollege.service.QuestionGeneratorService;
import com.ashcollege.entities.QuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class QuestionController {

    @Autowired
    private QuestionGeneratorService questionService;

    @PostMapping("/api/createQuestion")
    public ResponseEntity<Map<String, Object>> createQuestion(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String type = request.get("type");  // קבלת סוג השאלה

        try {
            QuestionEntity question = questionService.createQuestion(type);

            if (question != null) {
                response.put("success", true);
                response.put("question", question.getQuestionText());
                response.put("answer", question.getAnswer());
                response.put("explanation", question.getExplanation());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "סוג השאלה לא מוכר");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "הייתה שגיאה ביצירת השאלה: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}