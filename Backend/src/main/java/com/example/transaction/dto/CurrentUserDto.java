package com.example.transaction.dto;

import com.example.transaction.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CurrentUserDto {
    private String email;
    private Role role;
}

