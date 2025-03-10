package com.ashcollege.service;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

        // שמירת המשתמש
        //userRepository.save(user);
        System.out.println("שומר את המשתמש...");
        userRepository.save(user);
        System.out.println("המשתמש נשמר!");
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


}



