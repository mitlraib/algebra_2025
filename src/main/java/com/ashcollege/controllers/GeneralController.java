package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GeneralController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public Object hello() {
        return "Hello From Server";
    }

    /**
     * 📌 רישום משתמש חדש
     */
    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserEntity user) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.registerUser(user);
            response.put("success", true);
            response.put("message", "המשתמש נרשם בהצלחה");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "הייתה שגיאה במהלך הרישום: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📌 התחברות משתמש
     */
    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        String mail = loginData.get("mail");
        String password = loginData.get("password");

        System.out.println("User attempting to login: " + mail);

        try {
            UserEntity foundUser = userService.findByMail(mail);

            if (foundUser != null) {
                boolean passwordMatches = userService.checkPassword(password, foundUser.getPassword());

                if (passwordMatches) {
                    response.put("success", true);
                    response.put("message", "המשתמש התחבר בהצלחה");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "הסיסמה שגויה");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "המשתמש לא נמצא");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "הייתה שגיאה במהלך הכניסה: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
