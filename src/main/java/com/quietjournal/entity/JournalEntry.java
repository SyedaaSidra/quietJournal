package com.quietjournal.entity;

import com.quietjournal.model.Mood;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mood mood;

    @ElementCollection
    @CollectionTable(name = "journal_images", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "image_url")
    private java.util.List<String> images;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDate entryDate;

    // 🔗 Relationship: many journal entries belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.entryDate == null) {
            this.entryDate = LocalDate.now();
        }
    }
}
