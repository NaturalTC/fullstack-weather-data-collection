package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.dto.WeatherAlertDTO;
import com.github.fullstackweatherdatacollectionplatform.model.WeatherAlert;
import com.github.fullstackweatherdatacollectionplatform.repository.WeatherAlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AlertService {

    private final WeatherAlertRepository alertRepository;

    // optional — Spring only injects this if MAIL_USERNAME is configured
    // if it's null, alerts still work but no email is sent
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public AlertService(WeatherAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    // returns every alert rule in the database — the frontend shows all of them
    public List<WeatherAlertDTO> getAll() {
        return alertRepository.findAll()
                .stream()
                .map(WeatherAlertDTO::from)
                .toList();
    }

    // saves a new alert rule — id/triggered/triggeredAt are ignored from the request, set here
    public WeatherAlertDTO create(WeatherAlertDTO dto) {
        WeatherAlert alert = new WeatherAlert(
                dto.cityName(),
                dto.metric().toUpperCase(),
                dto.operator().toUpperCase(),
                dto.threshold(),
                dto.label(),
                dto.recipientEmail()
        );
        return WeatherAlertDTO.from(alertRepository.save(alert));
    }

    // removes an alert rule by id
    public void delete(Long id) {
        alertRepository.deleteById(id);
    }

    // resets the triggered flag so the alert watches for the condition again next ingestion cycle
    public void dismiss(Long id) {
        alertRepository.findById(id).ifPresent(alert -> {
            alert.setTriggered(false);
            alert.setTriggeredAt(null);
            alertRepository.save(alert);
        });
    }

    // called by WeatherIngestionService after saving fresh data for a city
    // reads the current metric values and flips any matching alert rules to triggered=true
    public void checkAlerts(String cityName, double temperature, double feelsLike,
                            int humidity, double windSpeed, int pressure) {

        List<WeatherAlert> alerts = alertRepository.findByCityName(cityName);
        if (alerts.isEmpty()) return;

        for (WeatherAlert alert : alerts) {
            double actualValue = switch (alert.getMetric()) {
                case "TEMPERATURE"  -> temperature;
                case "FEELS_LIKE"   -> feelsLike;
                case "HUMIDITY"     -> humidity;
                case "WIND_SPEED"   -> windSpeed;
                case "PRESSURE"     -> pressure;
                default -> {
                    log.warn("Unknown metric '{}' on alert {}", alert.getMetric(), alert.getId());
                    yield Double.NaN;  // NaN — condition check below will always be false
                }
            };

            if (Double.isNaN(actualValue)) continue;

            boolean conditionMet = switch (alert.getOperator()) {
                case "ABOVE" -> actualValue > alert.getThreshold();
                case "BELOW" -> actualValue < alert.getThreshold();
                default -> false;
            };

            // only update the row if the triggered state actually changes — avoids unnecessary DB writes
            if (conditionMet && !alert.isTriggered()) {
                alert.setTriggered(true);
                alert.setTriggeredAt(LocalDateTime.now());
                alertRepository.save(alert);
                log.info("Alert triggered: {} for {} — {} {} {} (actual: {})",
                        alert.getLabel(), cityName,
                        alert.getMetric(), alert.getOperator(), alert.getThreshold(), actualValue);
                sendAlertEmail(alert, actualValue);
            } else if (!conditionMet && alert.isTriggered()) {
                // condition is no longer met — auto-reset so it can fire again next time
                alert.setTriggered(false);
                alert.setTriggeredAt(null);
                alertRepository.save(alert);
            }
        }
    }

    // sends a plain-text email to the alert's recipient address
    // skips silently if MAIL_USERNAME is not configured so the app still runs without email set up
    private void sendAlertEmail(WeatherAlert alert, double actualValue) {
        if (mailSender == null || mailUsername.isBlank()) {
            log.warn("Email not configured — skipping notification for alert '{}'", alert.getLabel());
            return;
        }

        String metricLabel = switch (alert.getMetric()) {
            case "TEMPERATURE" -> "Temperature";
            case "FEELS_LIKE"  -> "Feels Like";
            case "HUMIDITY"    -> "Humidity";
            case "WIND_SPEED"  -> "Wind Speed";
            case "PRESSURE"    -> "Pressure";
            default            -> alert.getMetric();
        };

        String unit = switch (alert.getMetric()) {
            case "TEMPERATURE", "FEELS_LIKE" -> "°F";
            case "HUMIDITY"                  -> "%";
            case "WIND_SPEED"                -> " mph";
            case "PRESSURE"                  -> " hPa";
            default                          -> "";
        };

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(alert.getRecipientEmail());
            message.setSubject("Weather Alert: " + alert.getLabel());
            message.setText(
                    "Your alert was triggered!\n\n" +
                    "Alert:     " + alert.getLabel() + "\n" +
                    "City:      " + alert.getCityName() + "\n" +
                    "Condition: " + metricLabel + " is " + alert.getOperator().toLowerCase() +
                    " " + alert.getThreshold() + unit + "\n" +
                    "Current:   " + String.format("%.1f", actualValue) + unit + "\n\n" +
                    "Triggered at: " + alert.getTriggeredAt() + "\n\n" +
                    "— New England Weather"
            );
            mailSender.send(message);
            log.info("Alert email sent to {} for '{}'", alert.getRecipientEmail(), alert.getLabel());
        } catch (Exception e) {
            log.error("Failed to send alert email for '{}': {}", alert.getLabel(), e.getMessage());
        }
    }
}
