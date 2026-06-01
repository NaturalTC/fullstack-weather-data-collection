package com.github.fullstackweatherdatacollectionplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String name;
}
