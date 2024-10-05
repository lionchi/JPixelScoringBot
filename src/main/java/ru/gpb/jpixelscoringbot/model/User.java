package ru.gpb.jpixelscoringbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "telegram_login", nullable = false)
    private String telegramLogin;

    @Column(name = "telegram_name", nullable = false)
    private String telegramName;

    @Column(name = "entered_name")
    private String enteredName;
}
