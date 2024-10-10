package ru.gpb.jpixelscoringbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.gpb.jpixelscoringbot.dto.UserDto;
import ru.gpb.jpixelscoringbot.exception.DataNotFoundException;
import ru.gpb.jpixelscoringbot.mapper.UserMapper;
import ru.gpb.jpixelscoringbot.model.User;
import ru.gpb.jpixelscoringbot.repository.UserRepository;

import java.util.Optional;

import static ru.gpb.jpixelscoringbot.config.Constants.USER_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
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
        return (getEmptyIfNull(telegramLastName) + " " + getEmptyIfNull(telegramFirstName)).trim();
    }

    private static String getEmptyIfNull(String str) {
        return Optional.ofNullable(str).orElse("");
    }

    @Override
    public User getReferenceById(Long telegramId) {
        return userRepository.getReferenceById(telegramId);
    }

    @Override
    @Cacheable(cacheNames = USER_CACHE_NAME, key = "#telegramId")
    public UserDto getByTelegramId(Long telegramId) {
        return userRepository.findById(telegramId)
                .map(userMapper::toDto)
                .orElse(null);
    }

    @Override
    @Cacheable(cacheNames = USER_CACHE_NAME, key = "#telegramId")
    public UserDto findByTelegramId(Long telegramId) {
        return userRepository.findById(telegramId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с telegram_id " + telegramId + " не найден"));
    }
}
