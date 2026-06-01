package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.dto.ApiKeyDTO;
import com.github.fullstackweatherdatacollectionplatform.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keys")
@Tag(name = "API Keys", description = "Generate and manage developer API keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a new API key for the authenticated user (max 2 active keys)")
    public ResponseEntity<?> generate(
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String name = body != null ? body.get("name") : null;
        try {
            return ResponseEntity.ok(apiKeyService.generateKey(auth.getName(), name));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "List all active API keys for the authenticated user")
    public ResponseEntity<List<ApiKeyDTO>> list(Authentication auth) {
        return ResponseEntity.ok(apiKeyService.getUserKeys(auth.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke an API key")
    public ResponseEntity<Void> revoke(@PathVariable Long id, Authentication auth) {
        apiKeyService.revokeKey(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
