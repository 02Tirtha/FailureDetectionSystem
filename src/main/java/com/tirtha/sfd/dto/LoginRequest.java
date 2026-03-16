package com.tirtha.sfd.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class LoginRequest {
  

    private String email;
    private String password;

    // getters and setters
}

