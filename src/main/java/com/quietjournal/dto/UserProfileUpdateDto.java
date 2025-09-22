package com.quietjournal.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateDto {
    private String displayName;
    private String avatarUrl;
}
