package com.github.fullstackweatherdatacollectionplatform.filter;

import com.github.fullstackweatherdatacollectionplatform.model.ApiKey;
import com.github.fullstackweatherdatacollectionplatform.repository.ApiKeyRepository;
import com.github.fullstackweatherdatacollectionplatform.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final RateLimitService rateLimitService;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, RateLimitService rateLimitService) {
        this.apiKeyRepository = apiKeyRepository;
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("X-API-Key");

        if (header != null && !header.isBlank() &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            apiKeyRepository.findActiveKeyWithUser(header).ifPresent(key -> {
                if (!rateLimitService.isAllowed(key)) {
                    try {
                        response.setStatus(429);
                        response.setContentType("application/json");
                        long limit = rateLimitService.getLimitForPlan(key.getUser().getPlan());
                        response.getWriter().write(
                            "{\"error\":\"Rate limit exceeded\",\"limit\":" + limit + ",\"plan\":\"" + key.getUser().getPlan() + "\"}"
                        );
                        return;
                    } catch (IOException ignored) {}
                }

                var auth = new UsernamePasswordAuthenticationToken(
                    key.getUser().getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            });

            // If rate limit fired, response is already committed — skip the chain
            if (response.isCommitted()) return;
        }

        chain.doFilter(request, response);
    }
}
