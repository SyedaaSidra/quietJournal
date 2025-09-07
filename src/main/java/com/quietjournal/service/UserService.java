package com.quietjournal.service;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserResponseDto registerUser(UserDto userDto) {
    User user = User.builder()
            .username(userDto.getUsername())
            .displayName(userDto.getDisplayName())
            .avatarUrl(userDto.getAvatarUrl())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .build();

    User savedUser = userRepository.save(user);

    return UserResponseDto.builder()
            .id(savedUser.getId())
            .username(savedUser.getUsername())
            .displayName(savedUser.getDisplayName())
            .avatarUrl(savedUser.getAvatarUrl())
            .createdAt(savedUser.getCreatedAt())
            .build();
  }
}

