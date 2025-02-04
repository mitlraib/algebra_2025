package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void registerUser(UserEntity user) {
        // בדיקה אם שם המשתמש או המייל כבר קיימים


        if (userRepository.existsByMail(user.getMail())) {
            throw new RuntimeException("המייל כבר קיים במערכת");
        }

        // שמירת המשתמש
        userRepository.save(user);
    }
}