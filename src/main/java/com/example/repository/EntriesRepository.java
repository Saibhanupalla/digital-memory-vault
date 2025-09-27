package com.example.repository;

import com.example.model.Entries; // I'm using "Entry" (singular) as is the convention
import com.example.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntriesRepository extends JpaRepository<Entries, Long> {

    // This is used by your EntriesService to get all entries for a user
    List<Entries> findByUser(User user);

    // This is used by your UserService to get recent entries for insights
    List<Entries> findByUser(User user, Pageable pageable);

    @Query("SELECT e FROM Entries e WHERE e.user.id = :userId AND YEAR(e.createdAt) = :year AND MONTH(e.createdAt) = :month ORDER BY e.createdAt ASC")
    List<Entries> findAllByUserIdAndMonth(@Param("userId") Integer userId, @Param("year") int year, @Param("month") int month);
}