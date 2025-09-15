package com.quietjournal.controller;

import com.quietjournal.dto.JournalEntryDto;
import com.quietjournal.dto.JournalEntryResponseDto;
import com.quietjournal.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    // Create new entry
    @PostMapping
    public ResponseEntity<JournalEntryResponseDto> createEntry(@Valid @RequestBody JournalEntryDto dto) {
        JournalEntryResponseDto saved = journalEntryService.createEntry(dto);
        return ResponseEntity.ok(saved);
    }

    // Get all entries (for the authenticated user)
    @GetMapping
    public ResponseEntity<List<JournalEntryResponseDto>> getAllEntries() {
        return ResponseEntity.ok(journalEntryService.getAllEntries());
    }

    // Get entry by ID
    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryResponseDto> getEntryById(@PathVariable String id) {
        return journalEntryService.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update entry
    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryResponseDto> updateEntry(
            @PathVariable String id,
            @Valid @RequestBody JournalEntryDto updatedDto
    ) {
        return ResponseEntity.ok(journalEntryService.updateEntry(id, updatedDto));
    }

    // Delete entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id) {
        journalEntryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
