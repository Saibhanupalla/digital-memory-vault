package com.example.service;

import com.example.model.Reflection;
import com.example.repository.ReflectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReflectionService {

    private final ReflectionRepository reflectionRepository;
    public ReflectionService(ReflectionRepository reflectionRepository) {
        this.reflectionRepository = reflectionRepository;
    }

    @Transactional(readOnly = true)
    public Reflection getReflectionByEntryId(Long entryId) {
        // We will need to add a custom method to the repository for this.
        return reflectionRepository.findByEntryId(entryId)
                .orElseThrow(() -> new RuntimeException("Reflection not found for entry id: " + entryId));
    }
}
