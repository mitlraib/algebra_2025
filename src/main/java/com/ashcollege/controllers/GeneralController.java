

package com.ashcollege.controllers;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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

    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginData,
                                                         HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String mail = loginData.get("mail");
        String password = loginData.get("password");

        try {
            UserEntity foundUser = userService.findByMail(mail);
            if (foundUser != null) {
                boolean passwordMatches = userService.checkPassword(password, foundUser.getPassword());

                if (passwordMatches) {
                    // כאן חשוב: ניצור אובייקט Authentication
                    // ונשמור בסשן כדי ש-Spring יזהה אותנו ב-Requests הבאים.
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    foundUser.getMail(), // מה נחשב כ-Principal
                                    null,
                                    new ArrayList<>() // או רשימת Roles
                            );

                    // נכניס אותו ל-SecurityContext:
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // נבקש אובייקט HttpSession ונשמור בו את ה-SecurityContext
                    request.getSession(true)
                            .setAttribute("SPRING_SECURITY_CONTEXT",
                                    SecurityContextHolder.getContext());

                    response.put("success", true);
                    response.put("message", "המשתמש התחבר בהצלחה");
                    // לא צריך להחזיר טוקן. ה-Session ID נשלח כ-Cookie ב-Response
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
            response.put("message", "שגיאה בכניסה: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/user")
    public ResponseEntity<Map<String, Object>> getUser(HttpServletRequest request) {
        // ה-SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            // או שנבדוק בצורה אחרת
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userMail = (String) auth.getPrincipal(); // כי שמנו את mail כ-Principal
        UserEntity user = userService.findByMail(userMail);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("mail", user.getMail());
        response.put("level", user.getLevel());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/user/update-level")
    public ResponseEntity<Map<String, Object>> updateUserLevel(@RequestBody Map<String, Integer> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(null);
        }

        String userMail = (String) auth.getPrincipal();
        UserEntity user = userService.findByMail(userMail);
        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }

        int newLevel = request.get("level");

        // לוודא שהמשתמש יכול רק להוריד רמה, לא להעלות מעבר למה שהשיג
        if (newLevel < 1 || newLevel > user.getLevel()) {
            System.out.println("⚠️ רמה לא תקינה: " + newLevel); // הדפסה לבדיקה
            return ResponseEntity.badRequest().body(null);
        }

        user.setLevel(newLevel);
        userService.updateUser(user);
        System.out.println("✅ עדכון רמה בשרת ל-" + newLevel); // בדיקה שהשרת משנה באמת

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newLevel", newLevel);
        return ResponseEntity.ok(response);
    }

}




