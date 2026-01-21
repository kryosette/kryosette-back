package com.example.demo.domain.repositories.communication.user.role;

import com.example.demo.domain.model.user.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleStudent);
}
