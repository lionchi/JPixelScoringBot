package ru.gpb.jpixelscoringbot.service;

public interface ReportService {

    void createAndSaveReport(Long telegramUserId, Long questionId, Long answerId);
}
