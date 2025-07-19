package com.example.demo.user.profile.status;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<UserStatus, String> {
}
