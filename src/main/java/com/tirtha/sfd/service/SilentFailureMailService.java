package com.tirtha.sfd.service;

import com.tirtha.sfd.model.SilentFailure;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SilentFailureMailService {

    private final JavaMailSender mailSender;

    public void sendAlert(SilentFailure failure) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("recipient@example.com"); // change to your recipient
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
 

