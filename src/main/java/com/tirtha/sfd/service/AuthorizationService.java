package com.tirtha.sfd.service;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tirtha.sfd.model.Role;
import com.tirtha.sfd.model.User;
import com.tirtha.sfd.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    // User must have exact role
    public void requireRole(String email, Role required) {
        User user = getUser(email);
        Role role = normalize(user.getRole());
        if (role != required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    // User can have any one role
    public void requireAnyRole(String email, Role... allowed) {
        User user = getUser(email);
        Role role = normalize(user.getRole());
        boolean ok = Arrays.stream(allowed).anyMatch(r -> r == role);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private User getUser(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Email header");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // If user role is missing then treat it as USER
    private Role normalize(Role role) {
        return role == null ? Role.USER : role;
    }
}
