package com.example.controller;

import com.example.dto.MoodHistoryDto;
import com.example.dto.TagRequest;
import com.example.model.Entries;
import com.example.model.Reflection;
import com.example.model.Tag;
import com.example.service.EntriesService;
import com.example.service.FileStorageService;
import com.example.service.ReflectionService;
import com.example.service.TagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/entries")
public class EntriesController {

    private final EntriesService entryService;
    private final TagService tagService;
    private final ReflectionService reflectionService;
    private final FileStorageService fileStorageService;

    public EntriesController(EntriesService entryService , TagService tagService, ReflectionService reflectionService, FileStorageService fileStorageService) {
        this.entryService = entryService;
        this.tagService = tagService;
        this.reflectionService = reflectionService;
        this.fileStorageService = fileStorageService;
    }

    // Create entry for a specific user
    @PostMapping("/{userId}")
    public Entries createEntry(@PathVariable Integer userId, @RequestBody Entries entry) {
        return entryService.createEntry(userId, entry);
    }

    // Get all entries
    @GetMapping
    public List<Entries> getAllEntries() {
        return entryService.getAllEntries();
    }

    // Get all entries for a specific user
    @GetMapping("/user/{userId}")
    public List<Entries> getEntriesByUser(@PathVariable Integer userId) {
        return entryService.getEntriesByUserId(userId);
    }

    // Get a single entry
    @GetMapping("/{entryId}")
    public Entries getEntryById(@PathVariable Long entryId) {
        return entryService.getEntryById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found with id: " + entryId));
    }

    // Update entry
    @PutMapping("/{entryId}")
    public Entries updateEntry(@PathVariable Long entryId, @RequestBody Entries entry) {
        return entryService.updateEntry(entryId, entry);
    }

    // Delete entry
    @DeleteMapping("/{entryId}")
    public void deleteEntry(@PathVariable Long entryId) {
        entryService.deleteEntry(entryId);
    }

    @PostMapping("/{entryId}/tags")
    public Entries addTag(@PathVariable Long entryId, @RequestBody TagRequest tagRequest) {
        return tagService.addTagToEntry(entryId, tagRequest.getName());
    }

    @DeleteMapping("/{entryId}/tags/{tagName}")
    public void removeTag(@PathVariable Long entryId, @PathVariable String tagName) {
        tagService.removeTagFromEntry(entryId, tagName);
    }

    @GetMapping("/{entryId}/tags")
    public Set<Tag> getTagsForEntry(@PathVariable Long entryId) {
        // We can reuse the existing service method to find the entry
        Entries entry = entryService.getEntryById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found with id: " + entryId));

        // Return the set of tags from the entry
        return entry.getTags();
    }

    @GetMapping("/{entryId}/reflection")
    public Reflection getReflectionForEntry(@PathVariable Long entryId) {
        return reflectionService.getReflectionByEntryId(entryId);
    }

    @GetMapping("/user/{userId}/mood-history")
    public List<MoodHistoryDto> getMoodHistoryForUser(@PathVariable Integer userId) {
        return entryService.getMoodHistory(userId);
    }

    @PostMapping("/{entryId}/media")
    public Entries uploadMedia(
            @PathVariable Long entryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") String mediaType) {

        // 1. The service now returns the full, final S3 URL.
        String fileUrl = fileStorageService.storeFile(file);

        // 2. We no longer build a new URL here. We just pass the S3 URL directly to be saved.
        return entryService.updateEntryMedia(entryId, fileUrl, mediaType);
    }
}
