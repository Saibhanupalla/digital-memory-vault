package com.example.dto;

import java.time.LocalDate;

public class MoodHistoryDto {

    private LocalDate date;
    private String mood;

    public MoodHistoryDto(LocalDate date, String mood) {
        this.date = date;
        this.mood = mood;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
