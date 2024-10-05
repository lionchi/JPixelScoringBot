package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.QuestionType;

@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, String> {

}
