package com.quietjournal.controller;
import com.quietjournal.config.JwtUtil;
import com.quietjournal.dto.LoginDto;
import com.quietjournal.dto.LoginResponseDto;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.repository.UserRepository;
import com.quietjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserDto userDto) {
        UserResponseDto savedUser = userService.registerUser(userDto);
        return ResponseEntity.ok(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        LoginResponseDto response = LoginResponseDto.builder()
                .token(token)
                .user(UserResponseDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
