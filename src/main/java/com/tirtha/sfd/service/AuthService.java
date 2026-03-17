package com.tirtha.sfd.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tirtha.sfd.dto.AuthResponse;
import com.tirtha.sfd.dto.LoginRequest;
import com.tirtha.sfd.dto.RegisterRequest;
import com.tirtha.sfd.model.Role;
import com.tirtha.sfd.model.User;
import com.tirtha.sfd.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = Role.USER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            role = Role.valueOf(request.getRole().trim().toUpperCase());
        }
        user.setRole(role);

        userRepository.save(user);

        return "User Registered Successfully";
    }

    public Optional<AuthResponse> login(LoginRequest request) {

        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isPresent() &&
                passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            Role role = user.get().getRole() == null ? Role.USER : user.get().getRole();
            return Optional.of(new AuthResponse(
                "Login Successful",
                role.name(),
                user.get().getEmail()
            ));
        }

        return Optional.empty();
    }
}
