package com.ashcollege.repository;

import com.ashcollege.entities.UserEntity;
import com.ashcollege.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {


    boolean existsByMail(String email);


}