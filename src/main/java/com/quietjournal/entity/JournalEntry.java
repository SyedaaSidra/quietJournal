package com.quietjournal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 1000)
    private String content;

    private String mood;  // later we’ll use Enum

    private String imageUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Each journal belongs to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
