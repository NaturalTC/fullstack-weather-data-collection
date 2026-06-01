package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.model.AppUser;
import com.github.fullstackweatherdatacollectionplatform.repository.AppUserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final AppUserRepository userRepository;

    @Value("${stripe.secret.key:}")
    private String secretKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${app.frontend.url:http://localhost:5174}")
    private String frontendUrl;

    public StripeService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (!secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    public String createCheckoutSession(String userEmail, String priceId, String planName) throws Exception {
        if (secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured. Set STRIPE_SECRET_KEY.");
        }

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomerEmail(userEmail)
            .setSuccessUrl(frontendUrl + "/stripe/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(frontendUrl + "/stripe/cancel")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setPrice(priceId)
                .setQuantity(1L)
                .build())
            .putMetadata("userEmail", userEmail)
            .putMetadata("plan", planName)
            .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public void handleWebhook(String payload, String sigHeader) throws Exception {
        if (webhookSecret.isBlank()) return;

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Could not deserialize Stripe event"));

            String email   = session.getMetadata().get("userEmail");
            String plan    = session.getMetadata().get("plan");
            String custId  = session.getCustomer();

            if (email != null && plan != null) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    user.setPlan(plan.toUpperCase());
                    if (custId != null) user.setStripeCustomerId(custId);
                    userRepository.save(user);
                });
            }
        }
    }
}
