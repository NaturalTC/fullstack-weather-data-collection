package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class UserProfileDTO {
    private String name;
    private String email;
    private String role;
    private String plan;
    private LocalDateTime createdAt;

    public static UserProfileDTO from(AppUser u) {
        return new UserProfileDTO(u.getName(), u.getEmail(), u.getRole(), u.getPlan(), u.getCreatedAt());
    }
}
