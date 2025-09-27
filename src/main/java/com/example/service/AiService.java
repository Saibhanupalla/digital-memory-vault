package com.example.service;

import com.example.dto.openai.EmbeddingRequest;
import com.example.dto.openai.EmbeddingResponse;
import com.example.dto.openai.OpenAiRequest;
import com.example.dto.openai.OpenAiResponse;
import com.example.model.Entries;
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

    public float[] generateEmbedding(String content) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        EmbeddingRequest requestPayload = new EmbeddingRequest("text-embedding-ada-002", content);
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(requestPayload, headers);
        try {
            EmbeddingResponse response = restTemplate.postForObject(OPENAI_EMBEDDING_URL, entity, EmbeddingResponse.class);
            if (response != null && !response.getData().isEmpty()) {
                return response.getData().get(0).getEmbedding();
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI Embedding API: " + e.getMessage());
        }
        return null;
    }

    public String generateMonthlyDigest(String monthlyData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        String prompt = "You are a compassionate and insightful journal analyst. " +
                "Based on the following data from a user's diary for a specific month, " +
                "write a warm, encouraging, and reflective monthly digest. The digest should have:\n" +
                "1. A title in the format '# Life Digest for [Month Name] [Year]'.\n" +
                "2. An 'Overall Summary' section (##) providing a gentle, high-level overview of the month's events and feelings.\n" +
                "3. A 'Mood Analysis' section (##) that reflects on the mood trends without being overly clinical.\n" +
                "4. A 'Key Themes' section (##) that discusses the topics that appeared most frequently.\n" +
                "Format the entire output in Markdown.\n\n" +
                "Here is the data:\n" + monthlyData;

        // Increase max tokens for a longer response
        OpenAiRequest requestPayload = new OpenAiRequest("gpt-3.5-turbo", prompt);

        HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            OpenAiResponse response = restTemplate.postForObject(OPENAI_API_URL, entity, OpenAiResponse.class);
            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent().trim();
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API for digest: " + e.getMessage());
            return "Could not generate a digest at this time.";
        }

        return "Digest not available.";
    }
}