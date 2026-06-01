package com.github.fullstackweatherdatacollectionplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // tells Spring this class contains setup/config, read it on startup
public class WebConfig implements WebMvcConfigurer {  // implements WebMvcConfigurer so we can override Spring MVC's default CORS behavior

    // pull allowed origins from properties — the :* means default to wildcard if the property isn't set
    // in production this is set to the S3 frontend URL so only that domain can call the backend
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    // Spring calls this method to register CORS rules for each route
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // allow the frontend to call all public API endpoints (weather + auth + keys)
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("*");  // required for POST — browser preflight checks Content-Type header

        // allow the frontend to call admin endpoints
        // needs .allowedHeaders("*") because HTTP Basic Auth sends credentials in the Authorization header
        // which must be explicitly permitted for cross-origin requests
        registry.addMapping("/admin/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("*");                       // allow all headers — required for the Authorization header to pass through

        // allow the frontend to call the health check endpoint — read only so only GET is needed
        registry.addMapping("/actuator/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET");
    }
}
