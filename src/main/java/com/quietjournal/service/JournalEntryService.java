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
import java.util.stream.Stream;

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
                .tags(dto.getTags() != null ? dto.getTags() : new ArrayList<>())
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
        entry.setTags(dto.getTags() != null ? dto.getTags() : new ArrayList<>());
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
                .tags(entry.getTags() != null ? entry.getTags() : new ArrayList<>())
                .createdAt(entry.getCreatedAt())
                .entryDate(entry.getEntryDate())
                .build();
    }


    public List<JournalEntryResponseDto> searchEntries(String q, List<String> tags) {
        // load user's entries then filter server-side (simple and reliable for now)
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<JournalEntry> entries = journalEntryRepository.findByUserId(user.getId());

        Stream<JournalEntry> stream = entries.stream();

        if (q != null && !q.isBlank()) {
            String qLower = q.toLowerCase();
            stream = stream.filter(e ->
                    (e.getTitle() != null && e.getTitle().toLowerCase().contains(qLower)) ||
                            (e.getContent() != null && e.getContent().toLowerCase().contains(qLower))
            );
        }

        if (tags != null && !tags.isEmpty()) {
            // require entry contain all requested tags; change to any-match if you prefer
            stream = stream.filter(e -> {
                List<String> entryTags = e.getTags() != null ? e.getTags() : Collections.emptyList();
                return tags.stream().allMatch(entryTags::contains);
            });
        }

        return stream
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private  String getUsernameFromContext(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    public List<String> getDistinctTags() {
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return journalEntryRepository.findDistinctTagsByUserId(user.getId());
    }

    // For logged-in user (frontend dashboard)
    public Map<String, Long> getWeeklyMoodSummary() {
        String username = getUsernameFromContext();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return getWeeklyMoodSummary(user.getId());
    }

    public Map<String, Long> getWeeklyMoodSummary(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(6);

        List<JournalEntry> entries = journalEntryRepository
                .findByUserIdAndEntryDateBetween(user.getId(), weekStart, now);

        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getMood().name(), // converts Mood enum to String
                        Collectors.counting()
                ));
    }





}




