package com.example.service;

import com.example.model.Digest;
import com.example.model.Entries;
import com.example.model.Tag;
import com.example.model.User;
import com.example.repository.DigestRepository;
import com.example.repository.EntriesRepository;
import com.example.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DigestService {

    private final UserRepository userRepository;
    private final EntriesRepository entriesRepository;
    private final DigestRepository digestRepository;
    private final AiService aiService;

    public DigestService(UserRepository userRepository, EntriesRepository entriesRepository,
                         DigestRepository digestRepository, AiService aiService) {
        this.userRepository = userRepository;
        this.entriesRepository = entriesRepository;
        this.digestRepository = digestRepository;
        this.aiService = aiService;
    }

    public Digest generateDigest(Integer userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Fetch all entries for the given month and year
        List<Entries> monthlyEntries = entriesRepository.findAllByUserIdAndMonth(userId, year, month);

        if (monthlyEntries.isEmpty()) {
            throw new RuntimeException("No entries found for the specified month to generate a digest.");
        }

        // 2. Process the data to create a summary for the AI
        // Mood Analysis
        Map<String, Long> moodCounts = monthlyEntries.stream()
                .map(Entries::getAiDetectedMood)
                .filter(mood -> mood != null && !mood.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String moodSummary = "Mood counts: " + moodCounts.toString();

        // Key Themes Analysis
        String keyThemes = monthlyEntries.stream()
                .flatMap(entry -> entry.getTags().stream())
                .map(Tag::getName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        String themeSummary = "Top themes: " + (keyThemes.isEmpty() ? "None identified" : keyThemes);

        // Concatenate all data into a single string for the AI prompt
        String dataForAi = "Month: " + YearMonth.of(year, month).toString() + "\n" +
                moodSummary + "\n" +
                themeSummary + "\n" +
                "Key Memories:\n" +
                monthlyEntries.stream()
                        .map(entry -> "- " + entry.getTitle() + ": " + entry.getContent())
                        .collect(Collectors.joining("\n"));

        // 3. Call the AI to generate the digest content
        String digestContent = aiService.generateMonthlyDigest(dataForAi);

        // 4. Save the new Digest to the database
        Digest digest = new Digest();
        digest.setUser(user);
        digest.setYear(year);
        digest.setMonth(month);
        digest.setContent(digestContent);
        digest.setMoodSummary(moodCounts.toString()); // Store the raw counts
        digest.setKeyThemes(keyThemes);

        return digestRepository.save(digest);
    }

    @Scheduled(cron = "0 0 5 1 * ?") // Cron expression for 5 AM on the 1st of each month
    public void generateMonthlyDigestsForAllUsers() {
        System.out.println("Starting monthly digest generation job...");

        // Get the previous month and year
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        // Find all users in the system
        List<User> allUsers = userRepository.findAll();

        // Loop through each user and generate their digest for the previous month
        for (User user : allUsers) {
            try {
                System.out.println("Generating digest for user: " + user.getUsername());
                generateDigest(user.getId(), year, month);
            } catch (Exception e) {
                // Log the error but continue to the next user
                System.err.println("Failed to generate digest for user " + user.getId() + ": " + e.getMessage());
            }
        }
        System.out.println("Monthly digest generation job finished.");
    }
}