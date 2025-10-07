package com.quietjournal.controller;

import com.quietjournal.service.JournalEntryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/mood")
public class MoodController {

    private final JournalEntryService journalEntryService;

    public MoodController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping("/weekly")
    public Map<String, Long> getWeeklyMoodSummary() {
        // Returns only logged-in user’s data
        return journalEntryService.getWeeklyMoodSummary();
    }
}

