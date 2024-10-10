package ru.gpb.jpixelscoringbot.service;

public interface ReportService {

    void createAndSaveReport(Long telegramUserId, Long questionId, Long answerId);

    boolean existsAllByUserTelegramIdAndQuestionQuestionTypeCode(Long telegramUserId, String questionTypeCode);

    void removePreviousReport(Long telegramUserId, String questionTypeCode);

    void removePreviousReport(String telegramLogin, String questionTypeCode);

    String getResultReportByUser(Long telegramUserId);
}
