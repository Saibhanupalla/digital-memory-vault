package com.example.service;

import com.example.model.Entries;
import com.example.model.Tag;
import com.example.repository.EntriesRepository;
import com.example.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final EntriesRepository entriesRepository;
    public TagService(TagRepository tagRepository, EntriesRepository entriesRepository) {
        this.tagRepository = tagRepository;
        this.entriesRepository = entriesRepository;
    }

    @Transactional
    public Entries addTagToEntry(Long entryId, String tagName) {
        // 1. Find the entry or throw an exception if not found
        Entries entry = entriesRepository.findById(entryId).orElseThrow(
                () -> new RuntimeException("Entry with id " + entryId + " not found")
        );
        // 2. Find the tag by name. If it doesn't exist, create a new one.
        Tag tag = tagRepository.findByName(tagName).orElseGet(() -> new Tag(tagName));

        entry.getTags().add(tag);

        return entriesRepository.save(entry);

    }

    @Transactional
    public void removeTagFromEntry(Long entryId, String tagName) {

        Entries entry = entriesRepository.findById(entryId).orElseThrow(
                () -> new RuntimeException("Entry with id " + entryId + " not found")
        );

        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("Tag not found with name: " + tagName));

        entry.getTags().remove(tag);
        entriesRepository.save(entry);
    }


}
