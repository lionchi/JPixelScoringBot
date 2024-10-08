package ru.gpb.jpixelscoringbot.service;

public interface ReportService {

    void createAndSaveReport(Long telegramUserId, Long questionId, Long answerId);

    void removePreviousReport(Long telegramUserId, String questionTypeCode);

    String getResultReportByUser(Long telegramUserId);
}
