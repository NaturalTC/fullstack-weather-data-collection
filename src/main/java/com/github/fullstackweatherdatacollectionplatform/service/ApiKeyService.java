package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.dto.ApiKeyDTO;
import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import com.github.fullstackweatherdatacollectionplatform.repository.ApiKeyRepository;
import com.github.fullstackweatherdatacollectionplatform.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final AppUserRepository userRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, AppUserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    public ApiKeyDTO generateKey(String userEmail, String name) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long activeCount = apiKeyRepository.findByUserAndActiveTrue(user).size();
        if (activeCount >= 2) {
            throw new IllegalStateException("Maximum of 2 API keys allowed per account. Revoke an existing key first.");
        }

        String raw = UUID.randomUUID().toString().replace("-", "") +
                     UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String keyValue = "nvc_" + raw;

        ApiKey key = new ApiKey();
        key.setUser(user);
        key.setKeyValue(keyValue);
        key.setName(name != null && !name.isBlank() ? name : "Default");
        key.setActive(true);

        return ApiKeyDTO.from(apiKeyRepository.save(key));
    }

    public List<ApiKeyDTO> getUserKeys(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return apiKeyRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(ApiKeyDTO::masked)
                .toList();
    }

    public void revokeKey(Long keyId, String userEmail) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("Key not found"));
        if (!key.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        key.setActive(false);
        apiKeyRepository.save(key);
    }
}
