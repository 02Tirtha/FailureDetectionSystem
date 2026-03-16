package com.tirtha.sfd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tirtha.sfd.model.User;

public interface  UserRepository extends  JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

}
