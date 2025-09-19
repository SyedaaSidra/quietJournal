package com.quietjournal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quietjournal.dto.JournalEntryDto;
import com.quietjournal.dto.JournalEntryResponseDto;
import com.quietjournal.entity.JournalEntry;
import com.quietjournal.entity.User;
import com.quietjournal.exception.ImageUploadException;
import com.quietjournal.exception.JournalNotFoundException;
import com.quietjournal.exception.UnauthorizedAccessException;
import com.quietjournal.exception.UserNotFoundException;
import com.quietjournal.repository.JournalEntryRepository;
import com.quietjournal.repository.UserRepository;
import com.quietjournal.util.SupabaseProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {


    private final SupabaseProperties supabaseProps;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository userRepository;
    private final WebClient supabaseWebClient;
    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserRepository userRepository, WebClient supabaseWebClient,SupabaseProperties supabaseProps) {
        this.journalEntryRepository = journalEntryRepository;
        this.userRepository = userRepository;
        this.supabaseWebClient = supabaseWebClient;
       this.supabaseProps = supabaseProps;

       System.out.println(supabaseProps.getBucket());
       System.out.println(supabaseProps.getKey());
       System.out.println(supabaseProps.getUrl());
    }

    // Create new journal entry
    public JournalEntryResponseDto createEntry(JournalEntryDto dto, MultipartFile[] files) {
        // 1. Get logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> imageUrls = uploadImagesToSupabase(files);


        // 2. Build entity
        JournalEntry entry = JournalEntry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .mood(dto.getMood())
                .entryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : LocalDate.now())
                .createdAt(LocalDateTime.now())
                .user(user)
                .images(imageUrls)
                .build();

        // 3. Save
        JournalEntry saved = journalEntryRepository.save(entry);

        // 4. Map to Response DTO
        return mapToDto(saved);
    }

    public List<JournalEntryResponseDto> getAllEntries() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return journalEntryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<JournalEntryResponseDto> getEntryById(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return journalEntryRepository.findById(id)
                .filter(entry -> entry.getUser().getId().equals(user.getId()))
                .map(this::mapToDto);
    }

    public JournalEntryResponseDto updateEntry(String id, JournalEntryDto dto,MultipartFile[] files) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to update this entry");
        }
        if (files != null && files.length > 0) {
            entry.setImages(uploadImagesToSupabase(files));
        }

        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setMood(dto.getMood());
        entry.setEntryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : entry.getEntryDate());

        return mapToDto(journalEntryRepository.save(entry));}

    public void deleteEntry(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this entry");
        }
        journalEntryRepository.deleteById(id);
    }


    //  method to upload image
    // Upload a single file
    private String uploadImageToSupabase(MultipartFile file) {
        String path = UUID.randomUUID() + "-" + file.getOriginalFilename();
   try{

       MultipartBodyBuilder builder = new MultipartBodyBuilder();
       builder.part("file", file.getResource())
               .filename(Objects.requireNonNull(file.getOriginalFilename()))
               .contentType(file.getContentType() != null
                       ? MediaType.parseMediaType(file.getContentType())
                       : MediaType.APPLICATION_OCTET_STREAM);

       supabaseWebClient.post()
               .uri("/{bucket}/{path}", supabaseProps.getBucket(), path)
               .contentType(MediaType.MULTIPART_FORM_DATA)
               .bodyValue(builder.build())
               .retrieve()
               .bodyToMono(String.class)
               .block();
       return path;
       }catch (Exception ex) {
       throw new ImageUploadException("Failed to upload image " + file.getOriginalFilename(), ex);

   }
    }

    // Upload multiple files
    private List<String> uploadImagesToSupabase(MultipartFile[] files) {
        List<String> imageUrls = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                imageUrls.add(uploadImageToSupabase(file));
            }
        }
        return imageUrls;
    }


    public String generateSignedUrl(String path) {
        try {
            String response = supabaseWebClient.post()
                    .uri("/sign/{bucket}/{path}", supabaseProps.getBucket(), path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"expiresIn\":3600}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            String signedPath = jsonNode.get("signedURL").asText();

            return supabaseProps.getUrl() + "/storage/v1" + signedPath;
        } catch (Exception ex) {

            throw new ImageUploadException("Failed to generate signed URL for " + path, ex);
        }
    }





    // Mapper
    private JournalEntryResponseDto mapToDto(JournalEntry entry) {
        List<String> signedUrls = entry.getImages() != null
                ? entry.getImages().stream().map(this::generateSignedUrl).collect(Collectors.toList())
                : new ArrayList<>();

        return JournalEntryResponseDto.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .mood(entry.getMood())
                .images(signedUrls) // return signed URLs instead of raw paths
                .createdAt(entry.getCreatedAt())
                .entryDate(entry.getEntryDate())
                .build();
    }

}

