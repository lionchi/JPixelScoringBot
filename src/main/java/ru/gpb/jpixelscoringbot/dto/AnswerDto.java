package ru.gpb.jpixelscoringbot.dto;

public record AnswerDto(
        Long id,
        String answer,
        int answerNumber,
        Long questionId,
        Integer questionNumber,
        boolean right) {
}
