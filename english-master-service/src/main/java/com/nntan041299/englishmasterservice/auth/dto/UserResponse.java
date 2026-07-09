package com.nntan041299.englishmasterservice.auth.dto;

import com.nntan041299.englishmasterservice.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private boolean active;
}
