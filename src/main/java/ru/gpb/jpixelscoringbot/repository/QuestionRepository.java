package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.Question;
import ru.gpb.jpixelscoringbot.model.QuestionType;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value =
            "select * from questions q where q.question_type_code = :questionTypeCode order by random() limit :limit",
            nativeQuery = true)
    List<Question> findAllByQuestionTypeOrderByRandom(String questionTypeCode, Integer limit);
}
