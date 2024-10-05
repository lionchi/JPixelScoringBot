package ru.gpb.jpixelscoringbot.service;

import ru.gpb.jpixelscoringbot.model.User;

public interface UserService {

    void createUser(
            Long telegramId, String telegramLogin, String telegramFirstName, String telegramLastName, String enteredName);

    User getReferenceById (Long telegramId);

    User findByTelegramId(Long telegramId);
}
