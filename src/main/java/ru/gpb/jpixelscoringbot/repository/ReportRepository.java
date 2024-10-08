package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.Report;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByUserTelegramId(Long telegramUserId);

    void deleteAllByUserTelegramIdAndQuestionQuestionTypeCode(Long telegramUserId, String questionTypeCode);
}
