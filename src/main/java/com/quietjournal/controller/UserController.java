package com.quietjournal.controller;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }



    // ➡️ Get user by ID
    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    // ➡️ Get user by username
    @GetMapping("/username/{username}")
    public UserResponseDto getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    // ➡️ Get all users (later restricted to admin only)
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }
}

