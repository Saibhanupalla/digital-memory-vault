package com.example.service;

import com.example.dto.MoodHistoryDto;
import com.example.model.Entries;
import com.example.model.User;
import com.example.repository.EntriesRepository;
import com.example.repository.ReflectionRepository;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.model.Reflection;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntriesService {

    private final EntriesRepository entriesRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final ReflectionRepository reflectionRepository;

    public EntriesService(EntriesRepository entriesRepository, UserRepository userRepository, AiService aiService, ReflectionRepository reflectionRepository) {
        this.entriesRepository = entriesRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.reflectionRepository = reflectionRepository;
    }

    public Entries createEntry(Integer userId, Entries entry) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        entry.setUser(user);

        Entries savedEntry = entriesRepository.save(entry);

        String detectedMood = aiService.analyzeMood(savedEntry.getContent());
        String summary = aiService.summarizeEntry(savedEntry.getContent());

        float[] embedding = aiService.generateEmbedding(savedEntry.getContent());
        savedEntry.setAiDetectedMood(detectedMood);
        savedEntry.setEmbedding(embedding);
        entriesRepository.save(savedEntry);

        Reflection reflection = new Reflection();
        reflection.setEntry(savedEntry);
        reflection.setAiSummary(summary);
        reflectionRepository.save(reflection);

        return savedEntry;
    }

    public List<Entries> getAllEntries() {
        return entriesRepository.findAll();
    }

    public List<Entries> getEntriesByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return entriesRepository.findByUser(user);
    }

    @Transactional
    public Optional<Entries> getEntryById(Long entryId) {
        return entriesRepository.findById(entryId);
    }

    public Entries updateEntry(Long entryId, Entries updatedEntry) {
        Entries existing = entriesRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found with id: " + entryId));

        existing.setTitle(updatedEntry.getTitle());
        existing.setContent(updatedEntry.getContent());

        return entriesRepository.save(existing);
    }

    public void deleteEntry(Long entryId) {
        entriesRepository.deleteById(entryId);
    }

    public List<MoodHistoryDto> getMoodHistory(Integer userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Entries> entries = entriesRepository.findByUser(user);

        return entries.stream()
                // Filter out any entries that haven't been analyzed yet
                .filter(entry -> entry.getAiDetectedMood() != null && !entry.getAiDetectedMood().isBlank())
                // Map each entry to a new DTO
                .map(entry -> new MoodHistoryDto(
                        entry.getCreatedAt().toLocalDate(),
                        entry.getAiDetectedMood()
                ))
                // Collect the results into a list
                .collect(Collectors.toList());

    }

    public Entries updateEntryMedia(Long entryId, String mediaUrl, String mediaType) {
        // Find the existing entry
        Entries entry = entriesRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found with id: " + entryId));

        // Set the media properties
        entry.setMediaUrl(mediaUrl);
        entry.setMediaType(mediaType.toUpperCase());

        // Save and return the updated entry
        return entriesRepository.save(entry);
    }

}
