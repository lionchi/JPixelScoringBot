package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}
