package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByUserAndActiveTrue(AppUser user);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.user WHERE k.keyValue = :keyValue AND k.active = true")
    Optional<ApiKey> findActiveKeyWithUser(String keyValue);
}
