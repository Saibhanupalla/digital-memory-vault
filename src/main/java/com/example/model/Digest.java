package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "digests")
public class Digest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month; // 1-12 for Jan-Dec

    @Column(columnDefinition = "TEXT")
    private String content; // The full AI-generated digest content (e.g., in Markdown)

    @Column(name = "mood_summary")
    private String moodSummary; // e.g., "Mostly Positive"

    @Column(name = "key_themes")
    private String keyThemes; // e.g., "Work, Family, Projects"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMoodSummary() { return moodSummary; }
    public void setMoodSummary(String moodSummary) { this.moodSummary = moodSummary; }
    public String getKeyThemes() { return keyThemes; }
    public void setKeyThemes(String keyThemes) { this.keyThemes = keyThemes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}