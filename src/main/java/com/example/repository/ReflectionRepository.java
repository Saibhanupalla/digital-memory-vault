package com.example.repository;

import com.example.model.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    Optional<Reflection> findByEntryId(Long entryId);
}