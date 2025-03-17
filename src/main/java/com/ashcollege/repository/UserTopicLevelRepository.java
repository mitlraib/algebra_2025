package com.ashcollege.repository;

import com.ashcollege.entities.UserTopicLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// UserTopicLevelRepository.java
@Repository
public interface UserTopicLevelRepository extends JpaRepository<UserTopicLevelEntity, Integer> {
    // שליפה לפי userId ו-topicId
    UserTopicLevelEntity findByUserIdAndTopicId(int userId, int topicId);
    List<UserTopicLevelEntity> findByUserId(int userId);

}
