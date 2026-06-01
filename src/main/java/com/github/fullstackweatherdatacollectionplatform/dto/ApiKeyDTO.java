package com.github.fullstackweatherdatacollectionplatform.dto;

import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class ApiKeyDTO {
    private Long id;
    private String keyValue;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;

    public static ApiKeyDTO from(ApiKey k) {
        return new ApiKeyDTO(k.getId(), k.getKeyValue(), k.getName(), k.isActive(), k.getCreatedAt());
    }

    public static ApiKeyDTO masked(ApiKey k) {
        String v = k.getKeyValue();
        String display = (v != null && v.length() > 12) ? v.substring(0, 12) + "••••••••••••••••••••••••" : "nvc_••••••••";
        return new ApiKeyDTO(k.getId(), display, k.getName(), k.isActive(), k.getCreatedAt());
    }
}
