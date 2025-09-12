package com.quietjournal.service;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;


  public UserResponseDto registerUser(UserDto userDto) {

    if (userRepository.existsByUsername(userDto.getUsername())) {
      throw new RuntimeException("Username already taken");
    }

    User user = User.builder()
            .username(userDto.getUsername())
            .displayName(userDto.getDisplayName())
            .avatarUrl(userDto.getAvatarUrl())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .build();

    User savedUser = userRepository.save(user);

    return mapToDto(savedUser);
  }
  public UserResponseDto findByUsername(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return  mapToDto(user);
  }

  public UserResponseDto getUserById(String id) {
    Optional<User> user = userRepository.findById(id);
    return user.map(this::mapToDto).orElseThrow(() -> new RuntimeException("User not found"));
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

