package com.quietjournal.dto;
import com.quietjournal.model.Mood;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponseDto {
    private String id;
    private String title;
    private String content;
    private Mood mood;
    private List<String> images;
    private List<String> imagePaths;
    private LocalDateTime createdAt;
    private LocalDate entryDate;
}
