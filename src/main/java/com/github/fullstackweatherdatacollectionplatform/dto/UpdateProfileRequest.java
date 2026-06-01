package com.github.fullstackweatherdatacollectionplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    private String name;
    private String currentPassword;
    private String newPassword;
}
