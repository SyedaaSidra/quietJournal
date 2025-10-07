package com.quietjournal.util;

import com.quietjournal.entity.User;
import com.quietjournal.repository.UserRepository;
import com.quietjournal.service.EmailService;
import com.quietjournal.service.JournalEntryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class WeeklyMoodNotifier {

    private final JournalEntryService journalEntryService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public WeeklyMoodNotifier(JournalEntryService journalEntryService,
                              UserRepository userRepository,
                              EmailService emailService) {
        this.journalEntryService = journalEntryService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * MON")
// Every Monday 9 AM
//@Scheduled(cron = "0 * * * * *")
public void sendWeeklySummaries() {
    System.out.println(">>> Running weekly summary email job at " + LocalDateTime.now());
        List<User> users = userRepository.findAll();
        for (User user : users) {
            Map<String, Long> summary = journalEntryService.getWeeklyMoodSummary(user.getId());
            emailService.sendWeeklySummary(user.getEmail(), summary);
        }
    }
}
