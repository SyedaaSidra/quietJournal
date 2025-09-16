package com.quietjournal.repository;

import com.quietjournal.entity.JournalEntry;
import com.quietjournal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {

    // Find all journal entries for a specific user
    List<JournalEntry> findByUser(User user);
    List<JournalEntry> findByUserOrderByEntryDateDesc(User user);
    List<JournalEntry> findByUserId(String userId);
}

