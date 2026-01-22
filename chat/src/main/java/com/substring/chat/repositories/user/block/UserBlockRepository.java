package com.substring.chat.repositories.user.block;

import com.substring.chat.domain.model.user.block.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
}
