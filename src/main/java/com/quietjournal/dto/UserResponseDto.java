package com.quietjournal.dto;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private Instant createdAt;
}

