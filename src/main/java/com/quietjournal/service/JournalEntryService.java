package com.quietjournal.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quietjournal.dto.JournalEntryDto;
import com.quietjournal.dto.JournalEntryResponseDto;
import com.quietjournal.entity.JournalEntry;
import com.quietjournal.entity.User;
import com.quietjournal.exception.JournalNotFoundException;
import com.quietjournal.exception.UnauthorizedAccessException;
import com.quietjournal.exception.UserNotFoundException;
import com.quietjournal.repository.JournalEntryRepository;
import com.quietjournal.repository.UserRepository;
import com.quietjournal.util.SupabaseProperties;
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
    private final SupabaseService supabaseService;
    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserRepository userRepository, WebClient supabaseWebClient,SupabaseProperties supabaseProps,SupabaseService supabaseService) {
        this.journalEntryRepository = journalEntryRepository;
        this.userRepository = userRepository;
        this.supabaseWebClient = supabaseWebClient;
       this.supabaseProps = supabaseProps;
       this.supabaseService = supabaseService;


       System.out.println(supabaseProps.getUrl());
        System.out.println(supabaseProps.getKey());
    }

    // Create new journal entry
    public JournalEntryResponseDto createEntry(JournalEntryDto dto, MultipartFile[] files) {

        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> imageUrls = uploadImagesToSupabase(files);



        JournalEntry entry = JournalEntry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .mood(dto.getMood())
                .entryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : LocalDate.now())
                .createdAt(LocalDateTime.now())
                .user(user)
                .images(imageUrls)
                .build();


        JournalEntry saved = journalEntryRepository.save(entry);


        return mapToDto(saved);
    }

    // Get All Entries
    public List<JournalEntryResponseDto> getAllEntries() {
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return journalEntryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JournalEntryResponseDto getEntryById(String id) {
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this entry");
        }

        return mapToDto(entry);
    }


    public JournalEntryResponseDto updateEntry( String id, JournalEntryDto dto,MultipartFile[] files,List<String> keepPaths) {
        System.out.println(dto);
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to update this entry");
        }
        // Current DB paths
        List<String> dbPaths = entry.getImages() != null ? new ArrayList<>(entry.getImages()) : new ArrayList<>();

          System.out.println("keepPath"+keepPaths);

          System.out.println("dbpaths"+dbPaths);

        // Keep list from client (fallback empty list if null)
        List<String> toKeep = keepPaths != null ? keepPaths : new ArrayList<>();
    System.out.println("toKeep"+toKeep);
        // Delete unwanted images
        List<String> toDelete = dbPaths.stream()
                .filter(path -> !toKeep.contains(path))
                .toList();
        for (String path : toDelete) {
            deleteImageFromSupabase(path);
        }

         //Upload new files
        List<String> uploaded = uploadImagesToSupabase(files);
     System.out.println("uploaded"+uploaded);
        // Final images = kept + uploaded
        List<String> finalImages = new ArrayList<>();
        finalImages.addAll(toKeep);
        finalImages.addAll(uploaded);
          System.out.println("finalImages"+finalImages);
        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setMood(dto.getMood());
        entry.setEntryDate(dto.getEntryDate() != null ? LocalDate.parse(dto.getEntryDate()) : entry.getEntryDate());
        entry.setImages(finalImages);
        return mapToDto(journalEntryRepository.save(entry));}

    public void deleteEntry(String id) {
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("Journal entry not found with id " + id));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this entry");
        }
        journalEntryRepository.deleteById(id);
    }



    // ----------------- IMAGE HELPERS -----------------
    private void deleteImageFromSupabase(String path) {
        supabaseService.deleteFile(supabaseProps.getBucket(), path);
    }

    private String generateSignedUrl(String path) {
        return supabaseService.generateSignedUrl(supabaseProps.getBucket(), path, 3600);
    }

    private List<String> uploadImagesToSupabase(MultipartFile[] files) {
        return supabaseService.uploadFiles(supabaseProps.getBucket(), files);
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
                .imagePaths(entry.getImages() != null ? entry.getImages() : new ArrayList<>())
                .images(signedUrls) // return signed URLs instead of raw paths
                .createdAt(entry.getCreatedAt())
                .entryDate(entry.getEntryDate())
                .build();
    }

    private  String getUsernameFromContext(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }




}




