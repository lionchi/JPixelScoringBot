package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.DifficultyLevel;

@Repository
public interface DifficultyLevelRepository extends JpaRepository<DifficultyLevel, String> {
}
