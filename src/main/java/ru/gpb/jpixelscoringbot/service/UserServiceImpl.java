package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.gpb.jpixelscoringbot.exception.DataNotFoundException;
import ru.gpb.jpixelscoringbot.model.User;
import ru.gpb.jpixelscoringbot.repository.UserRepository;

import static ru.gpb.jpixelscoringbot.config.Constants.USER_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    @CachePut(cacheNames = USER_CACHE_NAME)
    public void createUser(
            Long telegramId, String telegramLogin, String telegramFirstName, String telegramLastName, String enteredName) {
        if (userRepository.existsByTelegramId(telegramId)) {
            return;
        }

        var user = new User();
        user.setTelegramId(telegramId);
        user.setTelegramLogin(telegramLogin);
        user.setTelegramName(getTelegramName(telegramFirstName, telegramLastName));
        user.setEnteredName(enteredName);
        userRepository.save(user);
    }

    private static String getTelegramName(String telegramFirstName, String telegramLastName) {
        return StringUtils.trimAllWhitespace((telegramLastName + " " + telegramFirstName));
    }

    @Override
    public User getReferenceById(Long telegramId) {
        return userRepository.getReferenceById(telegramId);
    }

    @Override
    @Cacheable(cacheNames = USER_CACHE_NAME, key = "#telegramId")
    public User findByTelegramId(Long telegramId) {
        return userRepository.findById(telegramId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с telegram_id " + telegramId + " не найден"));
    }
}
