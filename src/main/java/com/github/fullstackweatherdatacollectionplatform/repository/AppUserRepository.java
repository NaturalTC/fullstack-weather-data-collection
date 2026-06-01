package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<AppUser> findByPasswordResetToken(String token);

    @Query("""
    SELECT k
    FROM ApiKey k
    JOIN FETCH k.user
    WHERE k.keyValue = :key
    """)
    Optional<ApiKey> findByKeyValue(String key);
}
