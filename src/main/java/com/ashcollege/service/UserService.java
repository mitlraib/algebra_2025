package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void registerUser(UserEntity user) {
        // בדיקה אם שם המשתמש או המייל כבר קיימים
        if (userRepository.existsByMail(user.getMail())) {
            throw new RuntimeException("המייל כבר קיים במערכת");
        }
        user.setLevel(1); // ברירת מחדל רמה 1
        // שמירת המשתמש
        userRepository.save(user);
    }

    // מתודה שמחפשת את המשתמש לפי המייל
    public UserEntity findByMail(String mail) {
        return userRepository.findByMail(mail);
    }

    // מתודה להשוואת הסיסמאות (בלי הצפנה)
    public boolean checkPassword(String rawPassword, String storedPassword) {
        return rawPassword.equals(storedPassword);
    }

    public void updateUser(UserEntity user) {
        userRepository.save(user);
    }

    /**
     * מחזיר את המשתמש הנוכחי (המאומת) מה-SecurityContext
     */
    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = (String) auth.getPrincipal();
            return userRepository.findByMail(email);
        }
        return null;  // במקרה של משתמש לא מחובר
    }

    public void incrementTotalExercises(int userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setTotalExercises(user.getTotalExercises() + 1);
            userRepository.save(user);
        }
    }

    public void incrementTotalMistakes(int userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setTotalMistakes(user.getTotalMistakes() + 1);
            userRepository.save(user);
        }
    }
}
