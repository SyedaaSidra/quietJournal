package com.quietjournal.service;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.exception.UserNotFoundException;
import com.quietjournal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {


  private final UserRepository userRepository;
  public UserService(UserRepository userRepository){
    this.userRepository = userRepository;
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

   private UserResponseDto mapToDto(User user) {
    return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .build();
   }

}

