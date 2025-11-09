package com.substring.chat.controllers;

import com.substring.chat.entities.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
}
