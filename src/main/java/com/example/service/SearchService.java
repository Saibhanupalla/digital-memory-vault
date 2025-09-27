package com.example.service;

import com.example.model.Entries;
import com.example.model.User;
import com.example.repository.EntriesRepository;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final AiService aiService;
    private final EntriesRepository entriesRepository;
    private final UserRepository userRepository;

    public SearchService(AiService aiService, EntriesRepository entriesRepository, UserRepository userRepository) {
        this.aiService = aiService;
        this.entriesRepository = entriesRepository;
        this.userRepository = userRepository;
    }

    public List<Entries> searchUserEntries(Integer userId, String query) {
        float[] queryEmbedding = aiService.generateEmbedding(query);
        if (queryEmbedding == null) return List.of();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Entries> allEntries = entriesRepository.findByUser(user);

        return allEntries.stream()
                .filter(entry -> entry.getEmbedding() != null)
                .map(entry -> new EntrySimilarity(entry, cosineSimilarity(queryEmbedding, entry.getEmbedding())))
                .sorted(Comparator.comparingDouble(EntrySimilarity::getSimilarity).reversed())
                .limit(10)
                .map(EntrySimilarity::getEntry)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class EntrySimilarity {
        private final Entries entry;
        private final double similarity;
        EntrySimilarity(Entries entry, double similarity) { this.entry = entry; this.similarity = similarity; }
        Entries getEntry() { return entry; }
        double getSimilarity() { return similarity; }
    }
}