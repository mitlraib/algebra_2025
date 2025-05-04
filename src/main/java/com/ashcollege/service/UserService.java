package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * רישום משתמש חדש: מקודד את הסיסמה, מגדיר ערכי ברירת־מחדל ושומר.
     */
    public void registerUser(UserEntity user) {
        if (userRepository.existsByMail(user.getMail())) {
            throw new RuntimeException("המייל כבר קיים במערכת");
        }
        // קידוד הסיסמה לפני שמירה
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLevel(1);
        user.setRole("STUDENT");
        userRepository.save(user);
    }

    /**
     * שליפת משתמש לפי מייל.
     */
    public UserEntity findByMail(String mail) {
        return userRepository.findByMail(mail);
    }

    /**
     * בדיקת סיסמה גולמית מול ה־hash ששמור במסד.
     */
    public boolean checkPassword(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    /**
     * עדכון נתוני המשתמש.
     */
    public void updateUser(UserEntity user) {
        userRepository.save(user);
    }

    /**
     * קבלת המשתמש הנוכחי מה־SecurityContext (בהנחה שהוא מחובר).
     */
    public UserEntity getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = (String) auth.getPrincipal();
            return userRepository.findByMail(email);
        }
        return null;
    }

    public void incrementTotalExercises(int userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTotalExercises(user.getTotalExercises() + 1);
            userRepository.save(user);
        });
    }

    public void incrementTotalMistakes(int userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTotalMistakes(user.getTotalMistakes() + 1);
            userRepository.save(user);
        });
    }
}
