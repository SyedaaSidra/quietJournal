package com.quietjournal.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWeeklySummary(String toEmail, Map<String, Long> moodSummary) {
        StringBuilder content = new StringBuilder("Your weekly mood summary:\n\n");

        moodSummary.forEach((mood, count) -> {
            content.append(mood).append(": ").append(count).append(" entries\n");
        });

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Weekly Mood Summary");
        message.setText(content.toString());

        mailSender.send(message);
    }
}
