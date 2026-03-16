package com.tirtha.sfd.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tirtha.sfd.dto.LoginRequest;
import com.tirtha.sfd.dto.RegisterRequest;
import com.tirtha.sfd.model.User;
import com.tirtha.sfd.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public String register(RegisterRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        userRepository.save(user);

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if(user.isPresent() &&
           user.get().getPassword().equals(request.getPassword())) {

            return "Login Successful";
        }

        return "Invalid Email or Password";
    }
}
