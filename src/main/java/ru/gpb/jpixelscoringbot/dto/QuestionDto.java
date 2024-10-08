package ru.gpb.jpixelscoringbot.dto;

public record QuestionDto(
        Long id,
        String question,
        int questionNumber,
        String questionTypeCode,
        int timeInSeconds,
        String difficultyLevelCode, String imageMinioPath) {
}
