package com.example.repository;

import com.example.model.Digest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigestRepository extends JpaRepository<Digest, Long> {
}