package com.quietjournal.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/secure/hello")
    public String hello() {
        return "Hello from a protected endpoint!";
    }
}
