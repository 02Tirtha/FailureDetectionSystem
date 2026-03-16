package com.tirtha.sfd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class RegisterRequest {

    private String name;
    private String email;
    private String password;

}
