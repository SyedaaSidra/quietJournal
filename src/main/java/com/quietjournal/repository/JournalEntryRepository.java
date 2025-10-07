package com.quietjournal.repository;

import com.quietjournal.entity.JournalEntry;
import com.quietjournal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {

    // Find all journal entries for a specific user
    List<JournalEntry> findByUser(User user);
    List<JournalEntry> findByUserOrderByEntryDateDesc(User user);
    List<JournalEntry> findByUserId(String userId);
    @Query("SELECT DISTINCT t FROM JournalEntry e JOIN e.tags t WHERE e.user.id = :userId")
    List<String> findDistinctTagsByUserId(@Param("userId") String userId);

    List<JournalEntry> findByUserIdAndEntryDateBetween(String userId, LocalDate start, LocalDate end);

}

