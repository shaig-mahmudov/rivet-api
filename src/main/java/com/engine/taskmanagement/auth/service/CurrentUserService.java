package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.common.exception.UnauthorizedException;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public boolean isAdmin(User user) {
        return Role.ADMIN.equals(user.getRole());
    }
}
