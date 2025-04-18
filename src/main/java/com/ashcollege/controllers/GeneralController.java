package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class GeneralController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String hello() {
        return "Hello From Server";
    }

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
            response.put("message", "שגיאה במהלך הרישום: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData,
                                                         HttpServletRequest request) {
        String mail = loginData.get("mail");
        String password = loginData.get("password");

        try {
            UserEntity foundUser = userService.findByMail(mail);
            if (foundUser == null) {
                return errorResponse("המשתמש לא נמצא", HttpStatus.UNAUTHORIZED);
            }
            if (!userService.checkPassword(password, foundUser.getPassword())) {
                return errorResponse("הסיסמה שגויה", HttpStatus.UNAUTHORIZED);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if ("ADMIN".equalsIgnoreCase(foundUser.getRole())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(foundUser.getMail(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return successResponse("המשתמש התחבר בהצלחה");

        } catch (Exception e) {
            return errorResponse("שגיאה בכניסה: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext(); // מנקה את האובייקט המאחסן את ה-Authentication
        request.getSession(false).invalidate(); // הורג את ה־Session כך שאינו תקף עוד

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "התנתקת בהצלחה!");
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/api/user")
    public ResponseEntity<Map<String, Object>> getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return errorResponse("משתמש לא מחובר", HttpStatus.UNAUTHORIZED);
        }

        String userMail = (String) auth.getPrincipal();
        UserEntity user = userService.findByMail(userMail);
        if (user == null) {
            return errorResponse("המשתמש לא נמצא", HttpStatus.NOT_FOUND);
        }

        System.out.println(user);  // הדפסת אובייקט המשתמש
        System.out.println(user.getRole()); // הדפסת תפקיד

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("mail", user.getMail());
        response.put("level", user.getLevel());
        response.put("role", user.getRole());
        response.put("totalExercises", user.getTotalExercises());
        response.put("totalMistakes", user.getTotalMistakes());
        response.put("detailedSolutions", user.isDetailedSolutions());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/user/update-level")
    public ResponseEntity<Map<String, Object>> updateUserLevel(@RequestBody Map<String, Integer> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return errorResponse("משתמש לא מחובר", HttpStatus.UNAUTHORIZED);
        }

        String userMail = (String) auth.getPrincipal();
        UserEntity user = userService.findByMail(userMail);
        if (user == null) {
            return errorResponse("המשתמש לא נמצא", HttpStatus.NOT_FOUND);
        }

        int newLevel = request.getOrDefault("level", 1);
        if (newLevel < 1 || newLevel > user.getLevel()) {
            return errorResponse("רמה לא תקינה: " + newLevel, HttpStatus.BAD_REQUEST);
        }

        user.setLevel(newLevel);
        userService.updateUser(user);

        System.out.println("✅ עדכון רמה ל-" + newLevel);
        System.out.println(user); // הדפסת אובייקט המשתמש לאחר העדכון

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newLevel", newLevel);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/user/preferences")
    public ResponseEntity<Map<String,Object>> updatePreferences(@RequestBody Map<String,Object> body){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated()) return errorResponse("משתמש לא מחובר", HttpStatus.UNAUTHORIZED);

        UserEntity user = userService.findByMail((String) auth.getPrincipal());
        if(user==null)  return errorResponse("המשתמש לא נמצא", HttpStatus.NOT_FOUND);

        Boolean detailed = (Boolean) body.get("detailedSolutions");
        if(detailed!=null) user.setDetailedSolutions(detailed);
        userService.updateUser(user);

        return successResponse("עודכן בהצלחה");
    }

}
