package com.quietjournal.controller;

import com.quietjournal.dto.JournalEntryDto;
import com.quietjournal.dto.JournalEntryResponseDto;
import com.quietjournal.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;




import java.util.List;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    // Create new entry
    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JournalEntryResponseDto> createEntry( @RequestPart("data") @Valid JournalEntryDto dto,
                                                                @RequestPart(value = "images", required = false) MultipartFile[] images)
     {
        JournalEntryResponseDto saved = journalEntryService.createEntry(dto,images);
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
        JournalEntryResponseDto dto = journalEntryService.getEntryById(id);
        return ResponseEntity.ok(dto);}

    // Update entry
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<JournalEntryResponseDto> updateEntry(
            @PathVariable String id,
            @RequestPart("data") @Valid JournalEntryDto updatedDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        return ResponseEntity.ok(journalEntryService.updateEntry(id, updatedDto,images));
    }

    // Delete entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id) {
        journalEntryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
