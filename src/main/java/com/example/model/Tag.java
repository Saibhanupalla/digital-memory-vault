package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    // The 'mappedBy="tags"' indicates that the Entry class is the owner of the relationship
    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Entries> entries = new HashSet<>();

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Entries> getEntries() {
        return entries;
    }

    public void setEntries(Set<Entries> entries) {
        this.entries = entries;
    }
}
