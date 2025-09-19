package com.quietjournal.dto;

import com.quietjournal.model.Mood;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder

public class JournalEntryDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Mood is required")
    private Mood mood;

    private List<String> images;

    private String entryDate; // Optional, format YYYY-MM-DD
}
