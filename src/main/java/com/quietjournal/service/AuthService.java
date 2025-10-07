package com.quietjournal.service;
import com.quietjournal.config.JwtUtil;
import com.quietjournal.dto.LoginDto;
import com.quietjournal.dto.LoginResponseDto;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.entity.User;
import com.quietjournal.exception.DuplicateUserException;
import com.quietjournal.exception.InvalidCredentialsException;
import com.quietjournal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SupabaseService  supabaseService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,SupabaseService supabaseService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.supabaseService = supabaseService;
    }

    public UserResponseDto registerUser(UserDto userDto) {

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateUserException("Username already taken");
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    public LoginResponseDto login(@RequestBody LoginDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        String signedAvatarUrl = null;
        if (user.getAvatarUrl() != null) {
            signedAvatarUrl = supabaseService.generateSignedUrl("journal-images", user.getAvatarUrl(),3600);
        }
        return LoginResponseDto.builder()
                .token(token)
                .user(UserResponseDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .avatarUrl(signedAvatarUrl)
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();


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
