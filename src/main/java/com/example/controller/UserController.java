package com.example.controller;

import com.example.dto.InsightResponse;
import com.example.model.User;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping("/{userId}/insights")
    public InsightResponse getInsights(@PathVariable Integer userId) {
        String insightText = userService.generateInsightForUser(userId);
        return new InsightResponse(insightText);
    }
}
