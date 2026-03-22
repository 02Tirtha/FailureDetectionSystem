package com.tirtha.sfd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.tirtha.sfd.model.SilentFailure;

@Service
public class SilentFailureMailService {

    private final JavaMailSender mailSender;
    private final String alertRecipient;

    public SilentFailureMailService(
            JavaMailSender mailSender,
            @Value("${mail_username}") String alertRecipient
    ) {
        this.mailSender = mailSender;
        this.alertRecipient = alertRecipient;
    }

    public void sendAlert(SilentFailure failure) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertRecipient);
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
            e.printStackTrace(System.err);
        }
    }
}
 
