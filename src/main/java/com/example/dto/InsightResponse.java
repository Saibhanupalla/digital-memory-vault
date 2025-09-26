package com.example.dto;

public class InsightResponse {
    private String insight;

    public InsightResponse(String insight) {
        this.insight = insight;
    }

    // Getters and Setters
    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }
}