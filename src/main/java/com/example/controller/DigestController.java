package com.example.controller;

import com.example.model.Digest;
import com.example.service.DigestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DigestController {

    private final DigestService digestService;

    public DigestController(DigestService digestService) {
        this.digestService = digestService;
    }

    @GetMapping("/users/{userId}/digest")
    public Digest generateDigestForUser(
            @PathVariable Integer userId,
            @RequestParam int year,
            @RequestParam int month) {
        return digestService.generateDigest(userId, year, month);
    }
}