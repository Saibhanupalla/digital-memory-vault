package com.example.service;

import com.example.model.Entries;
import com.example.model.User;
import com.example.repository.EntriesRepository;
import com.example.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntriesRepository entriesRepository;
    private final AiService aiService;

    public UserService(UserRepository userRepository, EntriesRepository entriesRepository, AiService aiService){
        this.userRepository = userRepository;
        this.entriesRepository = entriesRepository;
        this.aiService = aiService;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public String generateInsightForUser(Integer userId) {
        // Find the user to ensure they exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Fetch the 20 most recent entries for this user
        List<Entries> recentEntries = entriesRepository.findByUser(user, PageRequest.of(0, 20, Sort.by("createdAt").descending()));

        // We need a few entries to find a meaningful pattern
        if (recentEntries.size() < 3) {
            return "Log a few more memories to unlock your first insight!";
        }

        // Call the AiService to get the insight
        return aiService.generateInsights(recentEntries);
    }
}
