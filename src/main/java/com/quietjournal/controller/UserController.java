package com.quietjournal.controller;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponseDto user = userService.findByUsername(username);
        System.out.println("################"+user);


        return ResponseEntity.ok(user);
    }

    //  Get user by ID
    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    //  Get user by username
    @GetMapping("/username/{username}")
    public UserResponseDto getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    //  Get all users (later restricted to admin only)
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> updateProfile(
            @RequestParam(required = false) String displayName,
            @RequestPart(required = false) MultipartFile avatarFile
    ) {
        System.out.println(displayName);
        System.out.println(avatarFile);
        return ResponseEntity.ok(userService.updateProfile(displayName, avatarFile));
    }
}

