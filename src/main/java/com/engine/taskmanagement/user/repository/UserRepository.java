package com.engine.taskmanagement.user.repository;

import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByDeletedAtIsNull();
    List<User> findAllByDeletedAtIsNotNull();
}
