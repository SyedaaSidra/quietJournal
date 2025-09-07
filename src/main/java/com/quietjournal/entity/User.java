package com.quietjournal.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID as string
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    private String displayName;

    private String avatarUrl;

    @Column(nullable = false)
    private String password; // hashed

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
