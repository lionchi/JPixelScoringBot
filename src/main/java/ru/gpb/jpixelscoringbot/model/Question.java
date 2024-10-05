package ru.gpb.jpixelscoringbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class Question {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq")
    @SequenceGenerator(name = "question_seq", sequenceName = "question_seq", allocationSize = 1)
    private Long id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    @ManyToOne
    @JoinColumn(name = "question_type_code", nullable = false)
    private QuestionType questionType;

    @Column(name = "image")
    private byte[] image;

    @Column(name = "timer_in_seconds", nullable = false)
    private int timeInSeconds;

    @ManyToOne
    @JoinColumn(name = "difficulty_level_code", nullable = false)
    private DifficultyLevel difficultyLevel;
}
