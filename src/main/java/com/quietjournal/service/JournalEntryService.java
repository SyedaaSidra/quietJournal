package com.quietjournal.service;

import com.quietjournal.dto.JournalEntryDto;
import com.quietjournal.dto.JournalEntryResponseDto;
import com.quietjournal.entity.JournalEntry;
import com.quietjournal.entity.User;
import com.quietjournal.exception.JournalNotFoundException;
import com.quietjournal.exception.UserNotFoundException;
import com.quietjournal.repository.JournalEntryRepository;
import com.quietjournal.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository userRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserRepository userRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.userRepository = userRepository;
    }

    // Create new journal entry
    public JournalEntryResponseDto createEntry(JournalEntryDto dto) {
        // 1. Get logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Build entity
        JournalEntry entry = JournalEntry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .mood(dto.getMood())
                .entryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : LocalDate.now())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        // 3. Save
        JournalEntry saved = journalEntryRepository.save(entry);

        // 4. Map to Response DTO
        return mapToDto(saved);
    }

    public List<JournalEntryResponseDto> getAllEntries() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new JournalNotFoundException("User not found"));
        return journalEntryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<JournalEntryResponseDto> getEntryById(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new JournalNotFoundException("User not found"));
        return journalEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .map(this::mapToDto);
    }

    public JournalEntryResponseDto updateEntry(String id, JournalEntryDto dto) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new JournalNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new JournalNotFoundException("You are not authorized to update this entry");
        }
        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setMood(dto.getMood());
        entry.setEntryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : entry.getEntryDate());

        return mapToDto(journalEntryRepository.save(entry));}

    public void deleteEntry(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this entry");
        }
        journalEntryRepository.deleteById(id);
    }

    // Mapper
    private JournalEntryResponseDto mapToDto(JournalEntry entry) {
        return JournalEntryResponseDto.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .mood(entry.getMood())
                .images(entry.getImages())
                .createdAt(entry.getCreatedAt())
                .entryDate(entry.getEntryDate())
                .build();
    }
}
