package com.tirtha.sfd.service;

import com.tirtha.sfd.model.SilentFailure;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class SilentFailureMailService {

    private final JavaMailSender mailSender;

    @Value("${alert.mail.to:tjhaveri99@example.com}")
    private String alertTo;

    @Value("${spring.mail.username:}")
    private String alertFrom;

    /**
     * FIX: @Async ensures SMTP never blocks the calling thread or any
     * open DB transaction. Each email runs in its own background thread.
     * If called from FailureDetectionService (which is already @Async),
     * this provides a second layer of protection — but it's also safe
     * to call from any synchronous context (e.g. the scheduler).
     */
    @Async
    public void sendAlert(SilentFailure failure) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertTo);
            if (alertFrom != null && !alertFrom.isBlank()) {
                message.setFrom(alertFrom);
            }
            message.setSubject("Silent Failure Alert: " + failure.getStepName());
            message.setText(
                "Workflow ID: " + failure.getWorkflow().getId() + "\n" +
                "Step: " + failure.getStepName() + "\n" +
                "Type: " + failure.getFailureType() + "\n" +
                "Severity: " + failure.getSeverity() + "\n" +
                "Message: " + failure.getMessage() + "\n" +
                "Detected At: " + failure.getDetectedAt()
            );
            mailSender.send(message);
            System.out.println("Alert email sent for failure: " + failure.getStepName());
        } catch (Exception e) {
            System.err.println("Failed to send alert email: " + e.getMessage());
        }
    }
}