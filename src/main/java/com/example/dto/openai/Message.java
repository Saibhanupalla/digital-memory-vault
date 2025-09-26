package com.example.dto.openai;

public class Message {
    private String role;
    private String content;

    public Message() {} // Default constructor for Jackson

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
    // Getters and Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}