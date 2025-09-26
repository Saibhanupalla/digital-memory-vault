package com.example.repository;

import com.example.model.Entries;
import com.example.model.User;
import com.pgvector.PGvector;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntriesRepository extends JpaRepository<Entries, Long> {

    List<Entries> findByUser(User user);

    List<Entries> findByUser(User user, Pageable pageable);

    @Query(value = "SELECT * FROM entries WHERE user_id = :userId ORDER BY embedding <=> :queryEmbedding LIMIT 10", nativeQuery = true)
    List<Entries> findSimilarEntries(Integer userId, PGvector queryEmbedding);
}

