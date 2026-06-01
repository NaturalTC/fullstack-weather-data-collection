package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.UpdateProfileRequest;
import com.github.fullstackweatherdatacollectionplatform.dto.UserProfileDTO;
import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import com.github.fullstackweatherdatacollectionplatform.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "View and update your account details")
public class UserProfileController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication auth) {
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserProfileDTO.from(user));
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update name and/or password")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request, Authentication auth) {
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 8 characters"));
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
        return ResponseEntity.ok(UserProfileDTO.from(user));
    }
}
