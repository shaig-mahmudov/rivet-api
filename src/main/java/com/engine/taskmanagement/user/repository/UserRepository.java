package com.engine.taskmanagement.user.repository;

import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
