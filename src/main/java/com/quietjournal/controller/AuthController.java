package com.quietjournal.controller;
import com.quietjournal.dto.LoginDto;
import com.quietjournal.dto.LoginResponseDto;
import com.quietjournal.dto.UserDto;
import com.quietjournal.dto.UserResponseDto;
import com.quietjournal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final  AuthService authService;
    public  AuthController(AuthService authService) {
        this.authService = authService;
    }




    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody UserDto userDto) {
        UserResponseDto savedUser = authService.registerUser(userDto);
        return ResponseEntity.ok(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login( @Valid @RequestBody LoginDto request) {
        return ResponseEntity.ok(authService.login( request));
    }

}
