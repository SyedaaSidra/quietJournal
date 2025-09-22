package com.quietjournal.service;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.exception.UserNotFoundException;
import com.quietjournal.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

  private final UserRepository userRepository;
  private final SupabaseService supabaseService;

  public UserService(UserRepository userRepository,SupabaseService supabaseService) {
    this.userRepository = userRepository;
    this.supabaseService = supabaseService;

  }
  public UserResponseDto findByUsername(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    return  mapToDto(user);
  }

  public UserResponseDto getUserById(String id) {
    Optional<User> user = userRepository.findById(id);
    return user.map(this::mapToDto).orElseThrow(() -> new  UserNotFoundException("User not found with id: " + id));
  }
  public List<UserResponseDto> getAllUsers() {
    return userRepository.findAll()
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
  }



//
//  public UserResponseDto updateProfile(String displayName, MultipartFile avatarFile) {
//      String username=  SecurityContextHolder.getContext().getAuthentication().getName();
//
//      User user= userRepository.findByUsername(username).orElseThrow(()->new UserNotFoundException("User not found"));
//
//
//
//    if (displayName != null && !displayName.isBlank()) {
//      user.setDisplayName(displayName);
//    }
//
//    if (avatarFile != null && !avatarFile.isEmpty()) {
//      // Upload to Supabase
//      String uploadedPath = supabaseService.uploadFile("avatars", avatarFile);
//      user.setAvatarUrl(uploadedPath);
//    }
//
//    User updated = userRepository.save(user);
//    return mapToDto(updated);
//  }
public UserResponseDto updateProfile(String displayName, MultipartFile avatarFile) {
  String username = SecurityContextHolder.getContext().getAuthentication().getName();
  System.out.println("Logged-in user: " + username);

  User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new UserNotFoundException("User not found"));

  System.out.println("Found user: " + user);

  if (displayName != null && !displayName.isBlank()) {
    user.setDisplayName(displayName);
    System.out.println("Updated displayName: " + displayName);
  }

  if (avatarFile != null && !avatarFile.isEmpty()) {
    System.out.println("Uploading avatar: " + avatarFile.getOriginalFilename());
    try {
      String uploadedPath = supabaseService.uploadFile("journal-images", avatarFile);
      System.out.println("Uploaded path: " + uploadedPath);
      System.out.println("User: " + user);
      user.setAvatarUrl(uploadedPath);
    } catch (Exception e) {


      throw e; // rethrow to see 500 in Postman
    }
  }

  User updated = userRepository.save(user);
  System.out.println("Updated user: " + updated);
  System.out.println("User saved successfully: " + updated);

  return mapToDto(updated);
}






  private UserResponseDto mapToDto(User user) {

    String signedAvatarUrl = null;
    if (user.getAvatarUrl() != null) {
      signedAvatarUrl = supabaseService.generateSignedUrl("journal-images", user.getAvatarUrl(),3600);
    }
    return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .avatarUrl(signedAvatarUrl)
            .createdAt(user.getCreatedAt())
            .build();
  }

}


