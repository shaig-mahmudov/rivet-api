package com.engine.taskmanagement.user.repository;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    List<User> findAllByDeletedAtIsNull();
    List<User> findAllByDeletedAtIsNotNull();
    boolean existsByEmail(String email);
    boolean existsByRoleAndDeletedAtIsNull(Role role);
}
