package com.example.demo.user.hidden_users;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HiddenUserRepository extends JpaRepository<HiddenUser, String> {
    boolean existsByUserAndHiddenUser(User user, Optional<User> hiddenUser);
    void deleteByUserAndHiddenUser(User user, Optional<User> hiddenUser);
    List<HiddenUser> findByUser(User user);
}