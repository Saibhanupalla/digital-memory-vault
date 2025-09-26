package com.example.service;

import com.example.dto.openai.EmbeddingRequest;
import com.example.dto.openai.EmbeddingResponse;
import com.example.dto.openai.OpenAiRequest;
import com.example.dto.openai.OpenAiResponse;
import com.example.model.Entries;
import com.pgvector.PGvector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeMood(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        String prompt = "Analyze the sentiment of the following text and return only one word: " +
                "Positive, Negative, or Neutral. Text: \"" + content + "\"";

        OpenAiRequest requestPayload = new OpenAiRequest("gpt-3.5-turbo", prompt);

        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            OpenAiResponse response = restTemplate.postForObject(OPENAI_API_URL, entity, OpenAiResponse.class);

            if (response != null && !response.getChoices().isEmpty()) {
                String result = response.getChoices().get(0).getMessage().getContent().trim();
                // Basic cleanup to ensure it's one of the expected words
                if (result.equalsIgnoreCase("Positive") || result.equalsIgnoreCase("Negative") || result.equalsIgnoreCase("Neutral")) {
                    return result;
                }
            }
        } catch (Exception e) {
            // Log the error in a real application
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            return "Error"; // Or a default value
        }

        return "Uncertain"; // Fallback if AI gives an unexpected response
    }

    public String summarizeEntry(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        String prompt = "Summarize the following diary entry in one concise sentence: \"" + content + "\"";

        OpenAiRequest requestPayload = new OpenAiRequest("gpt-3.5-turbo", prompt);
        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            OpenAiResponse response = restTemplate.postForObject(OPENAI_API_URL, entity, OpenAiResponse.class);

            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent().trim();
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API for summarization: " + e.getMessage());
            return "Could not generate summary.";
        }

        return "Summary not available.";
    }

    public String generateInsights(List<Entries> entries) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        // Format the recent entries into a single string for the prompt
        StringBuilder entriesString = new StringBuilder();
        for (Entries entry : entries) {
            entriesString.append("Date: ").append(entry.getCreatedAt().toLocalDate());
            entriesString.append(", Mood: ").append(entry.getAiDetectedMood());
            entriesString.append(", Title: ").append(entry.getTitle());
            entriesString.append(", Content: \"").append(entry.getContent()).append("\"\n\n");
        }

        String prompt = "You are a thoughtful life coach. Analyze the following diary entries and identify one interesting pattern, trend, or connection. " +
                "Provide a concise, helpful insight based on your observation. Keep it to two sentences maximum.\n\n" +
                "Here are the entries:\n" + entriesString.toString();

        OpenAiRequest requestPayload = new OpenAiRequest("gpt-3.5-turbo", prompt);
        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            OpenAiResponse response = restTemplate.postForObject(OPENAI_API_URL, entity, OpenAiResponse.class);
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent().trim();
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API for insights: " + e.getMessage());
            return "Could not generate an insight at this time.";
        }

        return "No insight available.";
    }

    private static final String OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    public PGvector generateEmbedding(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        EmbeddingRequest requestPayload = new EmbeddingRequest("text-embedding-ada-002", content);
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            EmbeddingResponse response = restTemplate.postForObject(OPENAI_EMBEDDING_URL, entity, EmbeddingResponse.class);
            if (response != null && !response.getData().isEmpty()) {
                return new PGvector(response.getData().get(0).getEmbedding());
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI Embedding API: " + e.getMessage());
        }
        return null;
    }
}