package ru.gpb.jpixelscoringbot.dto;

public record QuestionDto(
        Long id,
        String question,
        int questionNumber,
        String questionTypeCode,
        byte[] image,
        int timeInSeconds,
        String difficultyLevelCode) {
}
