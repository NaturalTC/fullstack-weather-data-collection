package com.github.fullstackweatherdatacollectionplatform.controller;

import com.github.fullstackweatherdatacollectionplatform.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@Tag(name = "Billing", description = "Stripe checkout and webhook endpoints")
public class StripeController {

    private final StripeService stripeService;

    @Value("${stripe.price.pro:price_pro_placeholder}")
    private String priceIdPro;

    @Value("${stripe.price.scale:price_scale_placeholder}")
    private String priceIdScale;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    @Operation(summary = "Create a Stripe checkout session for a plan upgrade")
    public ResponseEntity<?> createCheckout(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String plan = body.get("plan");
        if (plan == null) return ResponseEntity.badRequest().body(Map.of("error", "plan is required"));

        String priceId = switch (plan.toUpperCase()) {
            case "PRO"   -> priceIdPro;
            case "SCALE" -> priceIdScale;
            default      -> null;
        };

        if (priceId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown plan: " + plan));
        }

        try {
            String url = stripeService.createCheckoutSession(auth.getName(), priceId, plan.toUpperCase());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Checkout creation failed"));
        }
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook receiver — do not call directly")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripeService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("received");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Signature verification failed");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Webhook error");
        }
    }
}
