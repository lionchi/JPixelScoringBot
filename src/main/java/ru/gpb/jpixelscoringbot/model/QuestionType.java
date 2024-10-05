package ru.gpb.jpixelscoringbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_types")
@Getter
@Setter
public class QuestionType {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @Getter
    @RequiredArgsConstructor
    public enum QuestionTypeCodeEnum {
        JAVA("Java"),
        FRONTEND("Frontend"),
        ANALYTICS("Analytics");

        private final String code;
    }
}
