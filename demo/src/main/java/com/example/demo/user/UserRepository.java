package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.webjars.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u.firstname FROM User u")
    List<String> findAllUsernames();

    Optional<User> findByEmail(String email);

    List<User> findByIsOnlineTrue();

//    User updateUser(String userId, String newName) throws NotFoundException;
}
