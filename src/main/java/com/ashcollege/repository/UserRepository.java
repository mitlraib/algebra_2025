package com.ashcollege.repository;

import com.ashcollege.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {


    boolean existsByMail(String email);

    // מתודה שמבצע חיפוש של משתמש לפי המייל
    UserEntity findByMail(String email);

}