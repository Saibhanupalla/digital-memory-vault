package com.example.service;

import com.example.model.Entries;
import com.example.repository.EntriesRepository;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchService {

    private final AiService aiService;
    private final EntriesRepository entriesRepository;

    public SearchService(AiService aiService, EntriesRepository entriesRepository) {
        this.aiService = aiService;
        this.entriesRepository = entriesRepository;
    }

    public List<Entries> searchUserEntries(Integer userId, String query) {
        // 1. Convert the user's text query into an embedding
        PGvector queryEmbedding = aiService.generateEmbedding(query);

        if (queryEmbedding == null) {
            return List.of(); // Return empty list if embedding fails
        }

        // 2. Use the repository to find the most similar entries
        return entriesRepository.findSimilarEntries(userId, queryEmbedding);
    }
}